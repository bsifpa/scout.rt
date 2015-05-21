//FIXME NBU the name is confusing because TableControl is something different. Maybe use TableNavigation? Or directly put it in TableKeyStrokeAdapter
scout.TableControlKeyStrokes = function(field) {
  scout.TableControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.handle = function(event) {
  var $newRowSelection, $prev, $next, i, rows;
  var keycode = event.which;
  var $rowsAll = this._field.$rows();
  var $rowsSelected = this._field.$selectedRows();

  if (keycode === scout.keys.SPACE) {
    $newRowSelection = $rowsSelected;
    if ($rowsSelected.length > 0) {
      var check = !$($rowsSelected[0]).data('row').checked;
      for (var j = 0; j < $rowsSelected.length; j++) {
        var row = $($rowsSelected[j]).data('row');
        this._field.checkRow(row, check);
      }
    }
  }

  // up: move up
  if (keycode === scout.keys.UP) {
    if ($rowsSelected.length > 0) {
      $newRowSelection = this._field.$prevFilteredRows($rowsSelected.first()).first();
    } else {
      $newRowSelection = $rowsAll.last();
    }
  }

  // down: move down
  if (keycode === scout.keys.DOWN) {
    if ($rowsSelected.length > 0) {
      $newRowSelection = this._field.$nextFilteredRows($rowsSelected.last()).first();
    } else {
      $newRowSelection = $rowsAll.first();
    }
  }

  // home: top of table
  if (keycode === scout.keys.HOME) {
    $newRowSelection = $rowsAll.first();
  }

  // end: bottom of table
  if (keycode === scout.keys.END) {
    $newRowSelection = $rowsAll.last();
  }

  // pgup: jump up
  if (keycode === scout.keys.PAGE_UP) {
    if ($rowsSelected.length > 0) {
      $prev = this._field.$prevFilteredRows($rowsSelected.first());
      if ($prev.length > 10) {
        $newRowSelection = $prev.eq(10);
      } else {
        $newRowSelection = $rowsAll.first();
      }
    } else {
      $newRowSelection = $rowsAll.last();
    }
  }

  // pgdn: jump down
  if (keycode === scout.keys.PAGE_DOWN) {
    if ($rowsSelected.length > 0) {
      $next = this._field.$nextFilteredRows($rowsSelected.last());
      if ($next.length > 10) {
        $newRowSelection = $next.eq(10);
      } else {
        $newRowSelection = $rowsAll.last();
      }
    } else {
      $newRowSelection = $rowsAll.first();
    }
  }

  // apply selection
  if ($newRowSelection.length > 0) {
    rows = [];
    // FIXME CGU: Handling of shift key not perfect, yet... (must remember first selected row)
    if (event.shiftKey) {
      $newRowSelection = $rowsSelected.add($newRowSelection);
    }
    for (i = 0; $newRowSelection[i] !== undefined; i++) {
      rows.push($($newRowSelection[i]).data('row'));
    }
    this._field.selectRows(rows);
    // scroll selection into view (if not visible)
    this._field.scrollTo(rows[0]);
  }

  // preventDefault() is required here, because Chrome would native scroll a scrollable DIV,
  // which would interfere with our custom scroll behavior.
  event.preventDefault();
};
/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  if (this._field.$rows.length > 0) {
    var offset = 4;
    var $allRows = this._field.$rows();
    var $firstRow = $allRows.first();
    var $lastRow = $allRows.last();
    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.HOME)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'Home', $firstRow, false, false, false);
    }

    var $rowsSelected = this._field.$selectedRows();
    var $pageUpRow;
    var $prev;
    if ($rowsSelected.length > 0) {
      $prev = this._field.$prevFilteredRows($rowsSelected.first());
      if ($prev.length > 10) {
        $pageUpRow = $prev.eq(10);
      } else {
        $pageUpRow = $allRows.first();
      }
    } else {
      $pageUpRow = $allRows.last();
    }

    if (scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.PAGE_UP)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'PgUp', $firstRow, false, false, false);
    }

    var $upRow, $downRow;
    if ($allRows.length > $rowsSelected.length) {
      if ($rowsSelected.first()[0] !== $firstRow[0] && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.UP)) {
        //take pageUpOffset when upRow is the same as PgUp otherwise take firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $upRow = this._field.$prevFilteredRows($rowsSelected.first()).first();
        } else {
          $upRow = $allRows.first();
        }
        scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↑', $upRow, false, false, false);
      }
      if ($rowsSelected.last()[0] !== $lastRow[0] && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.DOWN)) {
        //take upRowOffset when $downRow = $upRow if not take pageUpOffset when upRow is the same as PgUp otherwise take
        //firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $downRow = this._field.$nextFilteredRows($rowsSelected.last()).first();
        } else {
          $downRow = $allRows.first();
        }
        scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↓', $downRow, false, false, false);
      }
    }
    // pgdn: jump down
    var $pgDownRow;
    if ($rowsSelected.length > 0) {
      var $next = this._field.$nextFilteredRows($rowsSelected.last());
      if ($next.length > 10) {
        $pgDownRow = $next.eq(10);
      } else {
        $pgDownRow = $allRows.last();
      }
    } else {
      $pgDownRow = $allRows.first();
    }
    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.PAGE_DOWN)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'PgDn', $pgDownRow, false, false, false);
    }

    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.END)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'End', $lastRow, false, false, false);
    }
  }

};

/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.accept = function(event) {
  var elementType = document.activeElement.tagName.toLowerCase();

  if (document.activeElement.className !== 'control-filter' &&
      (elementType === 'textarea' || elementType === 'input') &&
      (!event.originalEvent || (event.originalEvent && !event.originalEvent.smartFieldEvent))) {
    return false;
  }

  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.HOME, scout.keys.END, scout.keys.PAGE_UP, scout.keys.PAGE_DOWN, scout.keys.SPACE]) >= 0 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt;
};
