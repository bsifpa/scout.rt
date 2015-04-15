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
package org.eclipse.scout.rt.platform.inventory;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.internal.JandexClassInventory;
import org.eclipse.scout.rt.platform.inventory.internal.JandexInventoryBuilder;
import org.jboss.jandex.IndexView;

/**
 * Singleton with all the classes that were scanned in maven modules that contain /src/main/resources/META-INF/scout.xml
 */
public final class ClassInventory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClassInventory.class);
  private static IClassInventory INSTANCE;

  public static IClassInventory get() {
    return INSTANCE;
  }

  static {
    try {
      long t0 = System.nanoTime();
      JandexInventoryBuilder beanFinder = new JandexInventoryBuilder();
      beanFinder.scanAllModules();
      IndexView index = beanFinder.finish();
      long nanos = System.nanoTime() - t0;
      LOG.info("Finished preparation of jandex class inventory in {0} ms", DateUtility.formatNanos(nanos));
      INSTANCE = new JandexClassInventory(index);
    }
    catch (Throwable t) {
      throw new PlatformException("Error while building class inventory", t);
    }
  }

  private ClassInventory() {
  }
}
