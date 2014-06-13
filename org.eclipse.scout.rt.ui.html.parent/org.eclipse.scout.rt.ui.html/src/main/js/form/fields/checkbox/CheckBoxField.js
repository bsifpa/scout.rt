scout.CheckBoxField = function() {
  scout.CheckBoxField.parent.call(this);
  this._$checkBox;
};
scout.inherits(scout.CheckBoxField, scout.FormField);

scout.CheckBoxField.prototype._render = function($parent) {
  this.$container = $parent;
  this.$label = $('<label>' + this.label + '</label>');
  this.$container.append(this.$label);

  this._$errorStatus = $('<span class="status"></span>');
  this.$container.append(this._$errorStatus);

  this._$checkBox = $('<input type="checkbox" class="field" />');
  this.$container.append(this._$checkBox);

  this._$checkBox.on('click', function() {
    this.session.send('click', this.id);
  }.bind(this));
};

scout.CheckBoxField.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this._$checkBox.removeAttr('disabled');
  } else {
    this._$checkBox.attr('disabled', 'disabled');
  }
};

scout.CheckBoxField.prototype._setValue = function(value) {
  if (value) {
    this._$checkBox.attr('checked', 'checked');
  } else {
    this._$checkBox.removeAttr('checked');
  }
};

