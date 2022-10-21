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
import {FileChooserField, PropertyChangeEvent, ValueFieldAdapter} from '../../../index';

export default class FileChooserFieldAdapter extends ValueFieldAdapter {
  declare widget: FileChooserField;

  static PROPERTIES_ORDER = ['value', 'displayText'];

  protected override _onWidgetPropertyChange(event: PropertyChangeEvent<any, FileChooserField>) {
    super._onWidgetPropertyChange(event);

    if (event.propertyName === 'value') {
      this._onValueChange(event);
    }
  }

  protected _onValueChange(event: PropertyChangeEvent<File, FileChooserField>) {
    let success = this.widget.fileInput.upload();
    if (!success) {
      this.widget.fileInput.clear();
    }
  }

  protected override _syncDisplayText(displayText: string) {
    this.widget.setDisplayText(displayText);
    // When displayText comes from the server we must not call parseAndSetValue here.
  }

  /**
   * Handle events in this order value, displayText. This allows to set a null value and set a display-text
   * anyway. Otherwise the field would be empty. Note: this order is not a perfect solution for every case,
   * but it solves the issue reported in ticket #290908.
   */
  protected override _orderPropertyNamesOnSync(newProperties: Record<string, any>): string[] {
    return Object.keys(newProperties).sort(this._createPropertySortFunc(FileChooserFieldAdapter.PROPERTIES_ORDER));
  }
}
