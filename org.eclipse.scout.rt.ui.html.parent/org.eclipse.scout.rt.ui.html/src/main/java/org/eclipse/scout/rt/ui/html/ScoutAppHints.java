/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Helper to manage url param hints for the {@link AbstractScoutAppServlet}.
 * <p>
 * The following hints are supported:
 * <ul>
 * <li><b><code>?cache=(true|false)</code></b>: Enable/disable HTTP caching of resources.
 * <li><b><code>?compress=(true|false)</code></b>: Enable/disable GZIP compression (if client supports it).
 * <li><b><code>?minify=(true|false)</code></b>: Enable/disable "minification" of JS/CSS files.
 * <li><b><code>?debug=(true|false)</code></b>: Enable/disable all of the above flags.
 * </ul>
 * All values are <code>true</code> by default, unless the application is run in development mode.
 */
public final class ScoutAppHints {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutAppHints.class);

  private static final String URL_PARAM_DEBUG = "debug";//enables/disbles cache, compress, minify
  private static final String URL_PARAM_CACHE_HINT = "cache";
  private static final String URL_PARAM_COMPRESS_HINT = "compress";
  private static final String URL_PARAM_MINIFY_HINT = "minify";

  private static final String SESSION_ATTRIBUTE_CACHE_HINT = ScoutAppHints.class.getName() + "#cache";
  private static final String SESSION_ATTRIBUTE_COMPRESS_HINT = ScoutAppHints.class.getName() + "#compress";
  private static final String SESSION_ATTRIBUTE_MINIFY_HINT = ScoutAppHints.class.getName() + "#minify";

  private ScoutAppHints() {
    // static access only
  }

  public static void updateHints(HttpServletRequest req) {
    Boolean debug = getRequestParameterBoolean(req, URL_PARAM_DEBUG);
    if (debug != null) {
      updateHint(req, !debug.booleanValue(), SESSION_ATTRIBUTE_CACHE_HINT, SESSION_ATTRIBUTE_COMPRESS_HINT, SESSION_ATTRIBUTE_MINIFY_HINT);
    }
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_CACHE_HINT), SESSION_ATTRIBUTE_CACHE_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_COMPRESS_HINT), SESSION_ATTRIBUTE_COMPRESS_HINT);
    updateHint(req, getRequestParameterBoolean(req, URL_PARAM_MINIFY_HINT), SESSION_ATTRIBUTE_MINIFY_HINT);
  }

  private static void updateHint(HttpServletRequest req, Boolean value, String... sessionAttributeNameToStoreTo) {
    if (value == null) {
      return;
    }
    HttpSession session = req.getSession(false);
    if (session == null) {
      return;
    }
    for (String attName : sessionAttributeNameToStoreTo) {
      LOG.info("set ScoputAppHint: " + attName + "=" + value);
      session.setAttribute(attName, value);
    }
  }

  private static Boolean getRequestParameterBoolean(HttpServletRequest req, String name) {
    String s = req.getParameter(name);
    return s != null ? ("true".equals(s)) : null;
  }

  public static boolean isCacheHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_CACHE_HINT, !Platform.inDevelopmentMode());
  }

  public static boolean isCompressHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_COMPRESS_HINT, !Platform.inDevelopmentMode());
  }

  public static boolean isMinifyHint(HttpServletRequest req) {
    return calculateHint(req, SESSION_ATTRIBUTE_MINIFY_HINT, !Platform.inDevelopmentMode());
  }

  private static boolean calculateHint(HttpServletRequest req, String sessionAttr, boolean defaultValue) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return defaultValue;
    }
    Boolean hint = (Boolean) session.getAttribute(sessionAttr);
    if (hint != null) {
      return hint.booleanValue();
    }
    return defaultValue;
  }
}
