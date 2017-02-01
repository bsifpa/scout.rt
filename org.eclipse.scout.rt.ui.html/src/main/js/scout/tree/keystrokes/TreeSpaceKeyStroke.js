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
scout.TreeSpaceKeyStroke = function(tree) {
  scout.TreeSpaceKeyStroke.parent.call(this);
  this.field = tree;

  this.which = [scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.TreeSpaceKeyStroke, scout.KeyStroke);

scout.TreeSpaceKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TreeSpaceKeyStroke.parent.prototype._accept.call(this, event);
  return accepted &&
    this.field.checkable &&
    this.field.selectedNodes.length;
};

scout.TreeSpaceKeyStroke.prototype.handle = function(event) {
  var selectedNodes = this.field.selectedNodes.filter(function(node) {
    return node.enabled;
  });
  // Toggle checked state to 'true', except if every node is already checked
  var checked = selectedNodes.some(function(node) {
    return !node.checked;
  });
  selectedNodes.forEach(function(node) {
    this.field.checkNode(node, checked);
  }, this);
};
