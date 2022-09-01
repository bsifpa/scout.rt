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
import {AbstractLayout, FormTableControl, graphics, scrollbars, TabBox} from '../../index';
import $ from 'jquery';

export default class FormTableControlLayout extends AbstractLayout {
  control: FormTableControl;

  constructor(control: FormTableControl) {
    super();
    this.control = control;
  }

  override layout($container: JQuery) {
    if (!this.control.contentRendered || !this.control.form) {
      return;
    }

    let form = this.control.form,
      htmlForm = form.htmlComp,
      controlContentSize = graphics.size(this.control.tableFooter.$controlContent),
      formSize = controlContentSize.subtract(htmlForm.margins());

    htmlForm.setSize(formSize);

    // special case: when the control is opened/resized and there is not enough space, ensure that the active element is
    // visible by scrolling to it
    if (form.rootGroupBox.controls[0] instanceof TabBox) {
      let tabBox = form.rootGroupBox.controls[0];
      let tab = tabBox.selectedTab;
      let activeElement = document.activeElement as HTMLElement;
      if (tab && tab.scrollable && activeElement && tab.$body.has(activeElement)) {
        scrollbars.scrollTo(tab.$body, $(activeElement));
      }
    }
  }
}
