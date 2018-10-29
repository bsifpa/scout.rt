#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
  ${simpleArtifactName}.App = function() {
  ${simpleArtifactName}.App.parent.call(this);
  this.apiUrl = '../api/';
  this.appPrefix = '${simpleArtifactName}.';
  this.desktop = null;
};
scout.inherits(${simpleArtifactName}.App, scout.App);

${simpleArtifactName}.App.prototype._createSession = function(options) {
  var session = ${simpleArtifactName}.App.parent.prototype._createSession.call(this, options);
  session.on('localeSwitch', this._onLocaleSwitch.bind(this));
  return session;
};

${simpleArtifactName}.App.prototype._createDesktop = function(parent) {
  this.desktop = scout.create('${simpleArtifactName}.Desktop', scout.models.getModel('${simpleArtifactName}.Desktop', parent));

  var myDataOutline = this._createMyDataOutline();
  var myDataViewButton = scout.create('scout.OutlineViewButton', {
    parent: this.desktop,
    text: '${symbol_dollar}{textKey:MyDataOutlineTitle}',
    outline: myDataOutline,
    displayStyle: 'TAB',
    iconId: scout.icons.PERSON_SOLID
  });
  this.desktop._setViewButtons([myDataViewButton]);
  this.desktop.setOutline(myDataOutline);

  return this.desktop;
};

${simpleArtifactName}.App.prototype._createMyDataOutline = function() {
  var myDataOutline = scout.create('scout.Outline', {
    parent: this.desktop,
    title: '${symbol_dollar}{textKey:MyDataOutlineTitle}'
  });

  var personTablePage = scout.create('${simpleArtifactName}.PersonTablePage', {
    parent: myDataOutline
  });

  myDataOutline.insertNodes([personTablePage]);
  myDataOutline.selectNode(personTablePage);
  return myDataOutline;
};

${simpleArtifactName}.App.prototype._loadLocale = function() {
  var localeTag = scout.webstorage.getItem(sessionStorage, "locale");
  if (scout.strings.hasText(localeTag)) {
    var locale = scout.locales.get(localeTag);
    if (locale) {
      return locale;
    }
  }

  // Use the default locale
  ${symbol_dollar}.log.warn('Unsupported languageTag in settings found: ' + localeTag + '. Using navigator locale.');
  return scout.locales.getNavigatorLocale();
};

${simpleArtifactName}.App.prototype._onLocaleSwitch = function(event) {
  scout.webstorage.setItem(sessionStorage, "locale", event.locale.languageTag);
  var session = event.source;
  var busyIndicator = scout.create('BusyIndicator', {
    parent: session.desktop,
    cancellable: false,
    showTimeout: 0,
    details: session.text('LanguageSwitchLoading')
  });
  busyIndicator.render();
  setTimeout(function() {
    window.location.reload();
  }, 100);
};