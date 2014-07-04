// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MobileTable = function() {
  scout.MobileTable.parent.call(this);

  this._headerColumns = [];
};
scout.inherits(scout.MobileTable, scout.Table);

/**
 * @override
 */
scout.MobileTable.prototype.init = function(model, session) {
  //FIXME should be done by server, or should we add gui only property to control it? model may set it to true at any time later
  model.headerVisible = false;

  scout.MobileTable.parent.prototype.init.call(this, model, session);
};

/**
 * @override
 */
scout.MobileTable.prototype._createTableConfigurator = function() {
  return new scout.MobileTableConfigurator(this);
};

/**
 * @override
 */
scout.MobileTable.prototype._drawData = function(startRow) {
  this._headerColumns = this._computeHeaderColumns();

  scout.MobileTable.parent.prototype._drawData.call(this, startRow);
};

/**
 * @override
 */
scout.MobileTable.prototype._buildRowDiv = function(row, index) {
  var rowClass,
    cellContent = "",
    columns = this.columns,
    numColumnsUsed = 0,
    table = this.model,
    column, value, headerText = "";

  for (var c = 0; c < row.cells.length; c++) {
    column = this.columns[c];
    value = this.getText(c, index);

    if (c === 0) {
      cellContent += "<p>";
    }

    if (this._headerColumns.indexOf(column) >= 0) {
      headerText += value;
      numColumnsUsed++;
    } else {
      if (this._isColumnNameNecessary(columns[c])) {
        cellContent += columns[c].text + ": ";
      }
      cellContent += value;
      numColumnsUsed++;
      if (c < row.cells.length - 1) {
        cellContent += '<br/>';
      }
    }

    if (c === row.cells.length - 1) {
      cellContent += "</p>";
    }
  }
  if (headerText) {
    cellContent = '<h3>' + headerText + '</h3>' + cellContent;
  }

  rowClass = 'table-row ';
  if (numColumnsUsed === 1) {
    rowClass += 'table-row-single ';
  }
  if (this.selectedRowIds && this.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }

  return '<div id="' + row.id + '" class="' + rowClass + '">' + cellContent + '</div>';
};

scout.MobileTable.prototype._computeHeaderColumns = function() {
  var columns = this.columns,
    column,
    headerColumns = [],
    i;

  for (i = 0; i < columns.length; i++) {
    column = columns[i];

    if (column.summary) {
      headerColumns.push(column);
    }
  }

  if (headerColumns.length > 0) {
    return headerColumns;
  }

  for (i = 0; i < columns.length; i++) {
    column = columns[i];

    if (column.visible) { //FIXME also check for other criterias (checkboxcolum, see AbstractRowSummaryColumn);
      headerColumns.push(column);
      return headerColumns;
    }
  }

  return headerColumns;
};

scout.MobileTable.prototype._isColumnNameNecessary = function(column) {
  return true;
};
