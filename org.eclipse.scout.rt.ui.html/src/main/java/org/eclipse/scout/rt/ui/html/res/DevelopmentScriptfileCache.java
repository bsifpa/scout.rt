/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.ui.html.AbstractClasspathFileWatcher;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.ScriptfileBuildProperty;
import org.eclipse.scout.rt.ui.html.res.loader.ScriptFileLoader;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DevelopmentScriptfileCache {
  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentScriptfileCache.class);

  private final PathMatcher m_cssMatcher = FileSystems.getDefault().getPathMatcher("glob:**.css");
  private final PathMatcher m_jsMatcher = FileSystems.getDefault().getPathMatcher("glob:**.js");

  // initialized once in #init() / thread-safe [effective immutable]
  private boolean m_active;
  private AbstractClasspathFileWatcher m_scriptFileWatcher;

  // thread-safe through m_scriptLock
  private final Object m_scriptLock = new Object();
  private final Map<HttpCacheKey, HttpCacheObject> m_scriptCache = new HashMap<>();
  private final Map<HttpCacheKey, IFuture<HttpCacheObject>> m_pendingScriptFiles = new HashMap<>();
  private IFuture<Void> m_rebuildJsFuture;

  // thread-safe through m_rebuildStylesheetLock
  private final Object m_rebuildStylesheetLock = new Object();
  private IFuture<Void> m_rebuildStylesheetFuture;

  @PostConstruct
  public void init() {
    m_active = Platform.get().inDevelopmentMode() && !CONFIG.getPropertyValue(ScriptfileBuildProperty.class);
    if (!m_active) {
      return;
    }
    try {
      m_scriptFileWatcher = createScriptFileWatcher();
    }
    catch (IOException e) { //NOSONAR
      m_scriptFileWatcher = null;
      m_active = false;
      LOG.warn("Could not install watch service on classpath for less file rebuild. Use rebuild on every request!");
    }
  }

  @PreDestroy
  public void destroy() {
    try {
      m_scriptFileWatcher.destroy();
      m_scriptFileWatcher = null;
    }
    catch (IOException e) { //NOSONAR
      LOG.warn("Could not uninstall watch service on classpath for less file rebuild!");
    }
  }

  public boolean isActive() {
    return m_active;
  }

  public HttpCacheObject get(HttpCacheKey cacheKey) {
    if (!isActive()) {
      return null;
    }
    if (!acceptKey(cacheKey)) {
      return null;
    }
    HttpCacheObject result = null;
    IFuture<HttpCacheObject> future = null;
    synchronized (m_scriptLock) {
      future = m_pendingScriptFiles.get(cacheKey);
      if (future == null) {
        result = m_scriptCache.get(cacheKey);
        if (result != null) {
          return result;
        }
        future = scheduleBuildScriptFile(cacheKey);
      }
    }
    try {
      return future.awaitDoneAndGet();
    }
    catch (FutureCancelledError ex) { //NOSONAR
      // try again
      return get(cacheKey);
    }
  }

  protected boolean acceptKey(HttpCacheKey cacheKey) {
    Path path = Paths.get(cacheKey.getResourcePath());
    return m_cssMatcher.matches(path) || m_jsMatcher.matches(path);
  }

  protected void put(HttpCacheObject obj) {
    if (obj.isCachingAllowed()) {
      synchronized (m_scriptLock) {
        LOG.debug("put {} to cache.", obj.getCacheKey());
        m_scriptCache.put(obj.getCacheKey(), obj);
      }
      handleCacheChanged();
    }
  }

  protected void handleCacheChanged() {
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          Set<HttpCacheKey> keys;
          synchronized (m_scriptLock) {
            keys = new HashSet<HttpCacheKey>(m_scriptCache.keySet());
          }
          BEANS.get(DevelopmentScriptFileCacheInitialLoader.class).storeInitialScriptfiles(keys);
        }
        catch (Exception e) {
          LOG.warn("Could not store cached scriptfiles in dev mode.", e);
        }
      }
    }, Jobs.newInput().withName("development store cached script files."));
  }

  public void rebuildScripts(PathMatcher matcher) {
    synchronized (m_scriptLock) {
      for (IFuture<HttpCacheObject> feature : m_pendingScriptFiles.values()) {
        feature.cancel(true);
      }
      m_pendingScriptFiles.clear();
      for (HttpCacheKey key : m_scriptCache.keySet()) {
        if (matcher.matches(Paths.get(key.getResourcePath()))) {
          scheduleBuildScriptFile(key);
        }
      }
    }
  }

  protected IFuture<HttpCacheObject> scheduleBuildScriptFile(HttpCacheKey key) {
    synchronized (m_scriptLock) {
      IFuture<HttpCacheObject> feature = Jobs.schedule(createRecompileScriptJob(key), Jobs.newInput()
          .withName("Recompile scriptfile."));
      m_pendingScriptFiles.put(key, feature);
      return feature;
    }
  }

  protected void scheduleRebuildJsFiles() {
    LOG.debug("Rebuild all js files in development cache.");
    synchronized (m_scriptLock) {
      if (m_rebuildJsFuture != null) {
        m_rebuildJsFuture.cancel(false);
        m_rebuildJsFuture = null;
      }
      m_rebuildJsFuture = Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            synchronized (m_scriptLock) {
              if (IFuture.CURRENT.get().isCancelled()) {
                return;
              }
            }
            rebuildScripts(m_jsMatcher);
          }
          finally {
            synchronized (m_scriptLock) {
              if (IFuture.CURRENT.get() == m_rebuildJsFuture) {
                m_rebuildJsFuture = null;
              }
            }
          }
        }
      }, Jobs.newInput()
          .withName("rebuild js files.")
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartIn(300, TimeUnit.MILLISECONDS)));
    }
  }

  protected void scheduleRebuildStylesheets() {
    LOG.debug("Rebuild all stylesheets files in development cache.");
    synchronized (m_rebuildStylesheetLock) {
      if (m_rebuildStylesheetFuture != null) {
        m_rebuildStylesheetFuture.cancel(false);
        m_rebuildStylesheetFuture = null;
      }
      m_rebuildStylesheetFuture = Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            synchronized (m_rebuildStylesheetLock) {
              if (IFuture.CURRENT.get().isCancelled()) {
                return;
              }
            }
            rebuildScripts(m_cssMatcher);
          }
          finally {
            synchronized (m_rebuildStylesheetLock) {
              if (IFuture.CURRENT.get() == m_rebuildStylesheetFuture) {
                m_rebuildStylesheetFuture = null;
              }
            }
          }
        }
      }, Jobs.newInput()
          .withName("rebuild stylesheets files.")
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartIn(300, TimeUnit.MILLISECONDS)));
    }
  }

  protected AbstractClasspathFileWatcher createScriptFileWatcher() throws IOException {
    return new ScriptFileWatcher();
  }

  protected Callable<HttpCacheObject> createRecompileScriptJob(HttpCacheKey key) {
    return new RecompileScriptJob(key);
  }

  private class ScriptFileWatcher extends AbstractClasspathFileWatcher {

    private final PathMatcher m_lessMatcher = FileSystems.getDefault().getPathMatcher("glob:**.less");

    public ScriptFileWatcher() throws IOException {
      super();
    }

    @Override
    protected void execFileChanged(Path path) {
      if (m_lessMatcher.matches(path)) {
        scheduleRebuildStylesheets();
        return;
      }
      if (m_jsMatcher.matches(path)) {
        scheduleRebuildJsFiles();
      }
    }
  }

  private class RecompileScriptJob implements Callable<HttpCacheObject> {

    private final HttpCacheKey m_key;

    public RecompileScriptJob(HttpCacheKey key) {
      m_key = key;
    }

    @Override
    public HttpCacheObject call() throws Exception {
      try {
        ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), m_key.getAttribute(ScriptFileLoader.THEME_KEY), Boolean.parseBoolean(m_key.getAttribute(ScriptFileLoader.MINIFYED_KEY)));
        ScriptOutput out = builder.buildScript(m_key.getResourcePath());
        if (out == null) {
          return null;
        }
        if (IFuture.CURRENT.get().isCancelled()) {
          return null;
        }
        HttpCacheObject cacheObject = new HttpCacheObject(m_key,
            BinaryResources.create()
                .withFilename(ScriptFileLoader.translateLess(out.getPathInfo()))
                .withCharset(StandardCharsets.UTF_8)
                .withContent(out.getContent())
                .withLastModified(out.getLastModified())
                .withCachingAllowed(true)
                .withCacheMaxAge(HttpCacheControl.MAX_AGE_ONE_YEAR)
                .build());
        if (!IFuture.CURRENT.get().isCancelled()) {
          put(cacheObject);
        }
        return cacheObject;
      }
      finally {
        synchronized (m_scriptLock) {
          m_pendingScriptFiles.remove(m_key);
        }
      }
    }
  }
}