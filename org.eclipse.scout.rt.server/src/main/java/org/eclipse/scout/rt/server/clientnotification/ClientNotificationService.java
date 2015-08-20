/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;

/**
 * Service to consume notifications. Accessible from the client.
 */
public class ClientNotificationService implements IClientNotificationService {
  private final int m_blockingTimeout;
  private final int m_maxNotifications;

  public ClientNotificationService() {
    m_blockingTimeout = Assertions.assertNotNull(CONFIG.getPropertyValue(ClientNotificationProperties.MaxNotificationBlockingTimeOut.class));
    m_maxNotifications = Assertions.assertNotNull(CONFIG.getPropertyValue(ClientNotificationProperties.MaxNotificationMessages.class));
  }

  @Override
  public void registerSession(String nodeId, String sessionId, String userId) {
    BEANS.get(ClientNotificationRegistry.class).registerSession(nodeId, sessionId, userId);
  }

  @Override
  public void unregisterSession(String nodeId, String sessionId, String userId) {
    BEANS.get(ClientNotificationRegistry.class).unregisterSession(nodeId, sessionId, userId);
  }

  @Override
  public List<ClientNotificationMessage> getNotifications(String notificationNodeId) {
    return BEANS.get(ClientNotificationRegistry.class).consume(notificationNodeId, m_maxNotifications, m_blockingTimeout, TimeUnit.MILLISECONDS);
  }

}
