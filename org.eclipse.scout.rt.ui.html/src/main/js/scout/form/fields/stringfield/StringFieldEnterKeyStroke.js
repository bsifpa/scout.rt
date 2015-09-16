scout.StringFieldEnterKeyStroke = function(stringField) {
  scout.StringFieldEnterKeyStroke.parent.call(this);
  this.field = stringField;
  this.which = [scout.keys.ENTER];
  this.renderingHints.render = false;
  this.preventDefault = false;
};
scout.inherits(scout.StringFieldEnterKeyStroke, scout.KeyStroke);

scout.StringFieldEnterKeyStroke.prototype._applyPropagationFlags = function(event) {
  scout.StringFieldEnterKeyStroke.parent.prototype._applyPropagationFlags.call(this, event);

  this.preventInvokeAcceptInputOnActiveValueField = !event.isPropagationStopped() && document.activeElement.tagName.toLowerCase() === 'textarea';
  if (this.preventInvokeAcceptInputOnActiveValueField) {
    event.stopPropagation();
  }
};

scout.StringFieldEnterKeyStroke.prototype.handle = function(event) {
  // NOP
};
