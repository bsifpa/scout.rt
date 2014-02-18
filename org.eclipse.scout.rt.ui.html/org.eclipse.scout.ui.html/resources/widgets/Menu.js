// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// menu namespace and element
//

Scout.Menu = function (scout, id, x, y) {
  // remove (without animate) old menu
  $('#MenuSelect, #MenuControl').remove();

  // load model
  var menu = scout.syncAjax('drilldown_menu', id);

  // withou model, nothing to do
  if (menu.length === 0) return;

  // create 2 container, animate do not allow overflow
  var $menuSelect = $('body').appendDiv('MenuSelect')
          .css('left', x + 28).css('top', y - 3);
  var $menuControl = $('body').appendDiv('MenuControl')
          .css('left', x - 7).css('top', y - 3);

  // create menu-item and menu-button
  for (var i = 0; i < menu.length; i++) {
    if (menu[i].icon) {
      $menuSelect.appendDiv('', 'menu-button')
        .attr('data-icon', menu[i].icon)
        .attr('data-label', menu[i].label)
        .hover( function() {$('#MenuButtonsLabel').text($(this).data('label'));},
            function() {$('#MenuButtonsLabel').text('');});
    } else {
      $menuSelect.appendDiv('', 'menu-item', menu[i].label);
    }
  }

  // wrap menu-buttons and add one div for label
  $('.menu-button').wrapAll('<div id="MenuButtons"></div>');
  $('#MenuButtons').appendDiv('MenuButtonsLabel');

  // show menu on top
  var menuTop = $menuSelect.offset().top;
    menuHeight = $menuSelect.height(),
    windowHeight = $(window).height();

  if (menuTop + menuHeight > windowHeight) {
    $menuSelect.css('top', menuTop - menuHeight + 27);
  }

  // animated opening
  var w = $menuSelect.css('width');
  $menuSelect.css('width', 0).animateAVCSD('width', w);

  // every user action will close menu
  $('*').one('mousedown keydown mousewheel', removeMenu);
  function removeMenu (event) {
    $menuSelect.animateAVCSD('width', 0,
        function() {$menuControl.remove(); $menuSelect.remove(); });
    return true;
  }
};
