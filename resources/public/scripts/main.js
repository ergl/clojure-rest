var PaneEnum = Object.freeze({
  "user":1,
  "event":2,
  "create":3
});

// () -> ()
// Adds a listener for the esc key to exit dialogs
function setup() {
  var ESC_KEY_CODE = 27;
  document.addEventListener('keydown', function(e) {
    e = e || window.event;
    var container = document.getElementById('container');
    if (e.keyCode == ESC_KEY_CODE) {
      if (container.classList.contains('show-login-overlay')) {
        toggleLoginOverlay();
      } else if (container.classList.contains('show-event-sidebar')) {
        togglePane(PaneEnum.event);
      } else if (container.classList.contains('show-user-sidebar')) {
        togglePane(PaneEnum.user);
      } else if (container.classList.contains('show-create-sidebar')) {
        togglePane(PaneEnum.create);
      }
    }
  }, false);
}

// PaneEnum -> ()
// Triggers the slide animation for the given pane
function togglePane(pane) {
  var foo = document.getElementById('container').classList;
  switch(pane) {
      case PaneEnum.user:
        foo.toggle('show-user-sidebar');
        break;
      case PaneEnum.create:
        foo.toggle('show-create-sidebar');
        break;
      case PaneEnum.event:
        foo.toggle('show-event-sidebar');
        break;
  }
}

// () -> ()
// Triggers the menu pane slide animation
function toggleMenu() {
  document.getElementById('container').classList.toggle('show-main-sidebar');
}
