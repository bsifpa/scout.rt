/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TreeBox = function() {
  scout.TreeBox.parent.call(this);
  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 2;
  this._addWidgetProperties(['tree', 'filterBox']);
};
scout.inherits(scout.TreeBox, scout.ValueField);

scout.TreeBox.prototype._init = function(model) {
  scout.TreeBox.parent.prototype._init.call(this, model);
  if (this.filterBox) {
    this.filterBox.enabledComputed = true; // filter is always enabled
    this.filterBox.recomputeEnabled(true);
  }
};

scout.TreeBox.prototype._render = function() {
  this.addContainer(this.$parent, 'tree-box');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();

  this.addFieldContainer(this.$parent.makeDiv());
  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.TreeBoxLayout(this, this.tree, this.filterBox));

  if (this.tree) {
    this._renderTree();
  }
  if (this.filterBox) {
    this._renderFilterBox();
    this.tree.htmlComp.pixelBasedSizing = true;
  }
};

scout.TreeBox.prototype._renderTree = function($fieldContainer) {
  this.tree.render(this.$fieldContainer);
  this.addField(this.tree.$container);
};

scout.TreeBox.prototype._renderFilterBox = function($fieldContainer) {
  this.filterBox.render(this.$fieldContainer);
};
