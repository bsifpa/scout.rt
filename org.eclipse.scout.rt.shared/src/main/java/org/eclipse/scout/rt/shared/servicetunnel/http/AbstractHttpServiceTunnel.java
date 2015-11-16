/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ICancellable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.UriUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.SharedConfigProperties.ServiceTunnelTargetUrlProperty;
import org.eclipse.scout.rt.shared.servicetunnel.AbstractServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract tunnel used to invoke a service through HTTP.
 */
public abstract class AbstractHttpServiceTunnel extends AbstractServiceTunnel {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpServiceTunnel.class);

  public static final String TOKEN_AUTH_HTTP_HEADER = "X-ScoutAccessToken";

  private IServiceTunnelContentHandler m_contentHandler;
  private final URL m_serverUrl;
  private final boolean m_active;

  public AbstractHttpServiceTunnel() {
    this(getConfiguredServerUrl());
  }

  public AbstractHttpServiceTunnel(URL url) {
    m_serverUrl = url;
    m_active = url != null;
    if (url == null) {
      LOG.warn(String.format("No target url configured. Please specify a target URL in the config.properties using property '%s'.", BEANS.get(ServiceTunnelTargetUrlProperty.class).getKey()));
    }
  }

  protected static URL getConfiguredServerUrl() {
    IConfigProperty<String> targetUrlProperty = BEANS.get(ServiceTunnelTargetUrlProperty.class);
    String url = targetUrlProperty.getValue();
    if (StringUtility.hasText(url)) {
      try {
        URL targetUrl = UriUtility.toUrl(url);
        return targetUrl;
      }
      catch (RuntimeException e) {
        throw new IllegalArgumentException("targetUrl: " + url, e);
      }
    }
    return null;
  }

  @Override
  public boolean isActive() {
    return m_active;
  }

  public URL getServerUrl() {
    return m_serverUrl;
  }

  /**
   * @param call
   *          the original call
   * @param callData
   *          the data created by the {@link IServiceTunnelContentHandler} used by this tunnel Create url connection and
   *          write post data (if required)
   * @throws IOException
   *           override this method to customize the creation of the {@link URLConnection} see
   *           {@link #addCustomHeaders(URLConnection, String)}
   */
  protected URLConnection createURLConnection(ServiceTunnelRequest call, byte[] callData) throws IOException {
    // fast check of wrong URL's for this tunnel
    if (!"http".equalsIgnoreCase(getServerUrl().getProtocol()) && !"https".equalsIgnoreCase(getServerUrl().getProtocol())) {
      throw new IOException("URL '" + getServerUrl().toString() + "' is not supported by this tunnel ('" + getClass().getName() + "').");
    }

    // configure POST with text/xml
    URLConnection urlConn = getServerUrl().openConnection();
    String contentType = "text/xml";
    urlConn.setRequestProperty("Content-type", contentType);
    urlConn.setDoOutput(true);
    urlConn.setDoInput(true);
    urlConn.setDefaultUseCaches(false);
    urlConn.setUseCaches(false);
    addCustomHeaders(urlConn, "POST", callData);
    try (OutputStream httpOut = urlConn.getOutputStream()) {
      httpOut.write(callData);
    }
    return urlConn;
  }

  /**
   * @param method
   *          GET or POST override this method to add custom HTTP headers
   */
  protected void addCustomHeaders(URLConnection urlConn, String method, byte[] callData) throws IOException {
    addSignatureHeader(urlConn, method, callData);
  }

  protected void addSignatureHeader(URLConnection urlConn, String method, byte[] callData) throws IOException {
    try {
      String token = createAuthToken(urlConn, method, callData);
      if (StringUtility.hasText(token)) {
        urlConn.setRequestProperty(TOKEN_AUTH_HTTP_HEADER, token);
      }
    }
    catch (RuntimeException e) {
      throw new IOException(e);
    }
  }

  protected String createAuthToken(URLConnection urlConn, String method, byte[] callData) {
    if (!DefaultAuthToken.isEnabled()) {
      return null;
    }

    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.getPrincipals().isEmpty()) {
      // can happen e.g. when the container is shutting down
      return null;
    }

    String userId = CollectionUtility.firstElement(subject.getPrincipals()).getName();
    DefaultAuthToken token = BEANS.get(DefaultAuthToken.class);
    token.init(userId);
    return token.toString();
  }

  /**
   * @return msgEncoder used to encode and decode a request / response to and from the binary stream. Default is the
   *         {@link BinaryServiceTunnelContentHandler} which handles binary messages
   */
  public IServiceTunnelContentHandler getContentHandler() {
    return m_contentHandler;
  }

  /**
   * @param msgEncoder
   *          that can encode and decode a request / response to and from the binary stream. Default is the
   *          {@link BinaryServiceTunnelContentHandler} which handles binary messages
   */
  public void setContentHandler(IServiceTunnelContentHandler e) {
    m_contentHandler = e;
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) {
    if (m_contentHandler == null) {
      m_contentHandler = BEANS.get(IServiceTunnelContentHandler.class);
      m_contentHandler.initialize();
    }
    return super.invokeService(serviceInterfaceClass, operation, callerArgs);
  }

  @Override
  // Method overwritten to be accessible from within @{link RemoteServiceInvocationCallable}.
  protected ServiceTunnelRequest createServiceTunnelRequest(Class serviceInterfaceClass, Method operation, Object[] args) {
    return super.createServiceTunnelRequest(serviceInterfaceClass, operation, args);
  }

  /**
   * Creates the {@link Callable} to invoke the remote service operation described by 'serviceRequest'.
   * <p>
   * To enable cancellation, the callable returned must also implement {@link ICancellable}, so that the remote
   * operation can be cancelled once the current {@link RunMonitor} gets cancelled.
   */
  protected RemoteServiceInvocationCallable createRemoteServiceInvocationCallable(ServiceTunnelRequest serviceRequest) {
    return new RemoteServiceInvocationCallable(this, serviceRequest);
  }

  @Override
  protected ServiceTunnelResponse tunnel(final ServiceTunnelRequest serviceRequest) {
    final long requestSequence = serviceRequest.getRequestSequence();

    // Create the Callable to be given to the job manager for execution.
    final RemoteServiceInvocationCallable remoteInvocationCallable = createRemoteServiceInvocationCallable(serviceRequest);

    // Create a monitor and register it as child monitor of the current monitor so that the service request is cancelled once the current monitor gets cancelled.
    // Furthermore, this monitor is given to the job manager, so that the job is cancelled as well.
    final RunMonitor monitor = BEANS.get(RunMonitor.class);
    monitor.registerCancellable(remoteInvocationCallable);
    RunMonitor.CURRENT.get().registerCancellable(monitor);

    // Invoke the service operation asynchronously (to enable cancellation) and wait until completed or cancelled.
    ServiceTunnelResponse serviceResponse;
    try {
      serviceResponse = Jobs.schedule(remoteInvocationCallable, Jobs.newInput()
          .withRunContext(createCurrentRunContext().withRunMonitor(monitor))
          .withName(createServiceRequestName(requestSequence)))
          .awaitDoneAndGet();
    }
    catch (final ProcessingException e) {
      if (e.isInterruption() && !monitor.isCancelled()) {
        monitor.cancel(true); // Ensure the monitor to be cancelled once this thread is interrupted.
      }
      serviceResponse = new ServiceTunnelResponse(e);
    }

    if (monitor.isCancelled()) {
      serviceResponse = new ServiceTunnelResponse(new InterruptedException(ScoutTexts.get("UserInterrupted"))); // Cancellation has precedence over computation result or computation error.
    }

    return serviceResponse;
  }

  /**
   * @return a copy of the current calling context to be used to invoke the remote service operation.
   */
  protected RunContext createCurrentRunContext() {
    return RunContexts.copyCurrent();
  }

  /**
   * This method is called just after the http response is received but before the http response is processed by scout.
   * This might be used to read and interpret custom http headers.
   *
   * @since 06.07.2009
   */
  protected void preprocessHttpResponse(URLConnection urlConn, ServiceTunnelRequest call, int httpCode) {
  }

  /**
   * Returns the name to decorate the thread's name which executes the service request.
   */
  protected String createServiceRequestName(long requestSequence) {
    IFuture<?> currentFuture = IFuture.CURRENT.get();
    String scheduledBy = (currentFuture != null ? currentFuture.getJobInput().getName() : Thread.currentThread().getName());
    return String.format("Remote service request [seq=%s]; scheduled by %s", requestSequence, scheduledBy);
  }
}
