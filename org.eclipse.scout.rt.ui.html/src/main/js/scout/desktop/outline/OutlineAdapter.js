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
scout.OutlineAdapter = function() {
  scout.OutlineAdapter.parent.call(this);
  this._nodeIdToRowMap = {};
  this._detailTableRowInitHandler = this._onDetailTableRowInit.bind(this);
};
scout.inherits(scout.OutlineAdapter, scout.TreeAdapter);

scout.OutlineAdapter.prototype._init = function(model) {
  scout.OutlineAdapter.parent.prototype._init.call(this, model);
};

/**
 * We must call onWidgetPageInit because this adapter cannot process the 'pageInit' event
 * while the widget is initialized, since the listener is not attached until the widget
 * is created completely.
 */
scout.OutlineAdapter.prototype._postCreateWidget = function() {
  var outline = this.widget;
  outline._visitNodes(outline.nodes, this._onWidgetPageInit.bind(this));
};

scout.OutlineAdapter.prototype._onPageChanged = function(event) {
  var page;
  if (event.nodeId) {
    page = this.widget._nodeById(event.nodeId);

    page.detailFormVisible = event.detailFormVisible;
    page.setDetailForm(this.session.getOrCreateWidget(event.detailForm, this.widget));

    if (page.detailTable) {
      this._destroyDetailTable(page);
    }

    page.detailTableVisible = event.detailTableVisible;
    page.setDetailTable(this.session.getOrCreateWidget(event.detailTable, this.widget));

    if (page.detailTable) {
      this._initDetailTable(page);
    }
  } else {
    this.widget.defaultDetailForm = this.session.getOrCreateWidget(event.detailForm, this.widget);
  }

  this.widget.pageChanged(page);
};

scout.OutlineAdapter.prototype._onWidgetEvent = function(event) {
  if (event.type === 'pageInit') {
    this._onWidgetPageInit(event.page);
  } else {
    scout.OutlineAdapter.parent.prototype._onWidgetEvent.call(this, event);
  }
};

scout.OutlineAdapter.prototype.onModelAction = function(event) {
  if (event.type === 'pageChanged') {
    this._onPageChanged(event);
  } else {
    scout.OutlineAdapter.parent.prototype.onModelAction.call(this, event);
  }
};

scout.OutlineAdapter.prototype._onWidgetPageInit = function(page) {
  if (page.detailTable) {
    this._initDetailTable(page);
  }
  this._linkNodeWithRowLater(page);
};

scout.OutlineAdapter.prototype._initDetailTable = function(page) {
  // link already existing rows now
  page.detailTable.rows.forEach(this._linkNodeWithRow.bind(this));
  // rows which are inserted later are linked by _onDetailTableRowInit
  page.detailTable.on('rowInit', this._detailTableRowInitHandler);
};

scout.OutlineAdapter.prototype._destroyDetailTable = function(page) {
  this._nodeIdToRowMap = {};
  page.detailTable.rows.forEach(this._unlinkNodeWithRow.bind(this));
  page.detailTable.off('rowInit', this._detailTableRowInitHandler);
};

scout.OutlineAdapter.prototype._linkNodeWithRow = function(row) {
  scout.assertParameter('row', row);
  var node,
    nodeId = row.nodeId;

  if (nodeId === undefined) {
    // nodeId is undefined if no node exists for that row (e.g. happens if the page containing the row is a leaf page)
    return;
  }

  node = this.widget.nodesMap[nodeId];
  if (node) {
    node.linkWithRow(row);
  } else {
    // Prepare for linking later because node has not been inserted yet
    // see: #_linkNodeWithRowLater
    this._nodeIdToRowMap[nodeId] = row;
  }
};

scout.OutlineAdapter.prototype._unlinkNodeWithRow = function(row) {
  var node = this.widget.nodesMap[row.nodeId];
  if (node) {
    node.unlinkWithRow(row);
  }
};

scout.OutlineAdapter.prototype._onDetailTableRowInit = function(event) {
  var node,
    outline = this.widget,
    nodeId = event.row.nodeId;
  this._linkNodeWithRow(event.row);
  node = this.widget.nodesMap[nodeId];

  // If a row, which was already linked to a node, gets initialized again, re-apply the filter to make sure the node has the correct state
  if (outline.rendered && node && outline._applyFiltersForNode(node)) {
    if (node.isFilterAccepted()) {
      outline._addToVisibleFlatList(node, false);
    } else {
      outline._removeFromFlatList(node, false);
    }
  }
  if (this.widget.isSelectedNode(node) && !this.widget.detailContent) {
    // Table row detail could not be created because the link from page to row was missing at the time the node got selected -> do it now
    this.widget.updateDetailContent();
  }
};

/**
 * Link node with row, if it hasn't been linked yet.
 */
scout.OutlineAdapter.prototype._linkNodeWithRowLater = function(page) {
  if (!page.parentNode || !page.parentNode.detailTable) {
    return;
  }
  if (!this._nodeIdToRowMap.hasOwnProperty(page.id)) {
    return;
  }
  var row = this._nodeIdToRowMap[page.id];
  page.linkWithRow(row);
  delete this._nodeIdToRowMap[page.id];
};

/**
 * Static method to modify the prototype of scout.Outline.
 */
scout.OutlineAdapter.modifyOutlinePrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  scout.objects.replacePrototypeFunction(scout.Outline, '_computeDetailContent', scout.OutlineAdapter._computeDetailContentRemote, true);
  scout.objects.replacePrototypeFunction(scout.Outline, 'updateDetailMenus', scout.OutlineAdapter.updateDetailMenusRemote, true);
};

/**
 * Replacement for Outline#_computeDetailContent(). 'This' points to the outline.
 */
scout.OutlineAdapter._computeDetailContentRemote = function() {
  if (!this.modelAdapter) {
    return this._computeDetailContentOrig();
  }

  var selectedPage = this.selectedNode();
  if (!selectedPage) {
    // Detail content is shown for the selected node only
    return null;
  }

  // if there is a detail form, use this
  if (selectedPage.detailForm || selectedPage.detailFormResolved) {
    // If there is a detail form -> return (and set flag to true to make updateDetailMenusRemote work)
    selectedPage.detailFormResolved = true;
    return this._computeDetailContentOrig();
  }

  // It is not known yet whether there is a detail form -> wait for the requests to be processed before showing the table row detail
  if (!this.session.areRequestsPending() && !this.session.areEventsQueued()) {
    // There are no requests pending -> return (and set flag to true to make updateDetailMenusRemote work)
    selectedPage.detailFormResolved = true;
    return this._computeDetailContentOrig();
  }

  // Wait for the requests to complete
  this.session.listen().done(function(selectedPage) {
    if (selectedPage.detailFormResolved) {
      // No need to update detail content again if resolved is true
      return;
    }
    // Make sure the next time the page is selected it returns immediately and does not wait for requests to be completed
    selectedPage.detailFormResolved = true;
    this.updateDetailContent();
  }.bind(this, selectedPage));
};

/**
 * Replacement for Outline#updateDetailMenusRemote(). 'This' points to the outline.
 */
scout.OutlineAdapter.updateDetailMenusRemote = function() {
  if (!this.modelAdapter) {
    return this.updateDetailMenusOrig();
  }
  if (this.selectedNode() && this.selectedNode().detailFormResolved) {
    return this.updateDetailMenusOrig();
  }
};

scout.addAppListener('bootstrap', scout.OutlineAdapter.modifyOutlinePrototype);