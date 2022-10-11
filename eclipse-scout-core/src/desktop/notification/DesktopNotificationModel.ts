/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {NotificationModel, Status, StatusModel} from '../../index';
import {NativeNotificationVisibility} from './DesktopNotification';

export default interface DesktopNotificationModel extends NotificationModel {
  /**
   * The duration in milliseconds until the notification is automatically removed.
   * Use {@link DesktopNotification.INFINITE} to disable automatic removal.
   * Default is 5000ms.
   */
  duration?: number;

  /**
   * If true, only the native notification from the Browser will be shown.
   * The desktop notification will be kept open invisibly until the native notification is closed.
   * If you use an infinite duration (see {@link duration}, consider to close the desktop notification automatically when you don't need it anymore
   * to help the user keep its notification center clean, and to clean up the references on the desktop.
   * This may be necessary in case the user does not close it manually or the notification won't be shown at all due to missing permissions.
   */
  nativeOnly?: boolean;

  /**
   * The title displayed on the native notification.
   */
  nativeNotificationTitle?: string;

  /**
   * holds native message & native icon
   */
  nativeNotificationStatus?: Status | StatusModel;
  nativeNotificationVisibility?: NativeNotificationVisibility;
}
