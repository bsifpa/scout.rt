// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopViewButton = function(session, $parent, model) {
  this._$viewButton;

  session.widgetMap[model.id] = this;

  var state = '';
  if (model.selected) {
    state = 'selected';
  }
  this._$viewButton = $parent.appendDiv(model.id, 'view-item ' + state, model.text);
  this._$viewButton.on('click', '', onClick);

  var that = this;

  function onClick() {
    that._$viewButton.selectOne();
    session.send('click', $(this).attr('id'));
  }

};

scout.DesktopViewButton.prototype.onModelPropertyChange = function(event) {
  if (event.selected !== undefined) {
    if (event.selected) {
      this._$viewButton.selectOne();
    }
  }
};

scout.DesktopViewButton.prototype.onModelAction = function() {};
