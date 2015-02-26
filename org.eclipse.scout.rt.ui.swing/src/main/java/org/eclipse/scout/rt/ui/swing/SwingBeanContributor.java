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
package org.eclipse.scout.rt.ui.swing;

import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanContributor;
import org.eclipse.scout.rt.platform.pluginxml.internal.PluginXmlParser;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldExtensions;
import org.eclipse.scout.rt.ui.swing.extension.FormFieldsPluginXmlVisitor;

/**
 *
 */
public class SwingBeanContributor implements IBeanContributor {

  @Override
  public void contributeBeans(IBeanContext context) {
    context.registerClass(FormFieldExtensions.class);
    PluginXmlParser.get().visit(new FormFieldsPluginXmlVisitor(context));
  }

}
