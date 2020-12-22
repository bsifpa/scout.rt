/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.action.menu;

/**
 * All possible menu types of a table menu. These menu types are used by {@link AbstractMenu#getConfiguredMenuTypes()}.
 * <p>
 * Specificity: {@link #Header}, {@link #EmptySpace}, {@link #SingleSelection}, {@link #MultiSelection}
 */
public enum TableMenuType implements IMenuType {
  /**
   * Specifies menus which are visible independent of the selection of the table.<br>
   * The menu will be disabled if the table itself is disabled. If the menu has multiple types, the most restrictive
   * type wins (e.g. a menu with type EmptySpace and SingleSelection will be disabled if a disabled row is selected).
   */
  EmptySpace,
  /**
   * Specifies menus which are visible if a single table row is selected.<br>
   * If the table row is disabled or the table itself is disabled, the menu will be disabled as well. If the menu has
   * multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will be
   * disabled if a disabled row is selected).
   */
  SingleSelection,
  /**
   * Specifies menus which are visible if multiple table rows are selected.<br>
   * If the selection contains disabled rows or the table itself is disabled, the menu will be disabled as well. If the
   * menu has multiple types, the most restrictive type wins (e.g. a menu with type EmptySpace and SingleSelection will
   * be disabled if a disabled row is selected).
   */
  MultiSelection,
  Header
}
