scout.TableFilterControlKeyStrokes = function(field) {
  scout.TableFilterControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableFilterControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.handle = function(event) {
  // set focus
  var $input = $('.control-filter', this._field.$container);
  var length = $input.val().length;

  $input.focus();
  $input[0].setSelectionRange(length, length);

};
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
//TODO nbu only draw if control-filter is active
  var $filterinput = $('.control-filter', this._field.$container);
  if ($filterinput.length &&
      !scout.KeyStrokeUtil.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.A, scout.keys.Z) &&
      !scout.KeyStrokeUtil.keyStrokesAlreadyDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9])) {
    var filterInputPosition =  $filterinput.position();
    var top = $filterinput.css('margin-top').replace("px", "");
    var left =  filterInputPosition.left + parseInt($filterinput.css('margin-left').replace("px", ""),0) + 4;
    $filterinput.beforeDiv('key-box char', 'a - z').css('left', left +'px').css('top', top +'px');
    scout.KeyStrokeUtil.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys[0], scout.keys[9]);
    scout.KeyStrokeUtil.keyStrokeRangeDrawn(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.A, scout.keys.Z);
  }
};

/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.TableFilterControlKeyStrokes.prototype.accept = function(event) {
  return event && ((event.which >= 65 && event.which <= 90) || (event.which >= 48 && event.which <= 57)) && // a-z
  event.ctrlKey===this.ctrl && event.altKey===this.alt && event.shiftKey===this.shift ;
};

