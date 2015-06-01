var Overlays = (function() {
  "use strict";

  var classList = document.getElementById('container').classList;

  var toggleUserPane = function() {
    classList.toggle('show-user-sidebar');
  };

  var toggleEventPane = function () {
    classList.toggle('show-event-sidebar');
  };

  var toggleCreatePane = function () {
    classList.toggle('show-create-sidebar');
  };

  var toggleMenu = function () {
    classList.toggle('show-main-sidebar');
  };

  var toggleLoginOverlay = function() {
    $("#container").toggleClass('show-login-overlay');
    $("#login-user-text").focus();
  };

  // Switches between the login and the signup pane inside the overlay box
  var toggleLoginCredentialsPane = function () {
    $("#login-overlay").toggleClass('show-signup-pane');
    document.getElementById(
        (document.getElementById('login-overlay').classList.contains('show-signup-pane'))
            ? 'signup-email-text' : 'login-user-text').focus();
  };

  return {
    toggleLoginOverlay: function() {toggleLoginOverlay()},
    toggleLoginCredentialsPane: function() {toggleLoginCredentialsPane()},
    toggleUserPane: function() {toggleUserPane()},
    toggleEventPane: function() {toggleEventPane()},
    toggleCreatePane: function() {toggleCreatePane()},
    toggleMenu: function() {toggleMenu()}
  }

}());

$(function() {

  var ESC_KEY_CODE = 27;
  document.addEventListener('keydown', function(e) {
    e = e || window.event;
    var classList = document.getElementById('container').classList;
    if (e.keyCode == ESC_KEY_CODE) {
      if (classList.contains('show-login-overlay')) {
        Overlays.toggleLoginOverlay();
      } else if (classList.contains('show-event-sidebar')) {
        Overlays.toggleEventPane();
      } else if (classList.contains('show-user-sidebar')) {
        Overlays.toggleUserPane();
      } else if (classList.contains('show-create-sidebar')) {
        Overlays.toggleCreatePane();
      }
    }
  }, false);

  $("#create-button").click(function () {
    Overlays.toggleCreatePane();
  });

  $("#event-close-arrow").click(function () {
    Overlays.toggleEventPane();
  });

  $("#user-close-arrow").click(function () {
    Overlays.toggleUserPane();
  });

  $("#create-close-arrow").click(function () {
    Overlays.toggleCreatePane();
  });

  $(".comment-user").click(function () {
    Overlays.toggleUserPane();
  });

  $("#main-sidebar").mouseover(function () {
    Overlays.toggleMenu();
  }).mouseout(function () {
    Overlays.toggleMenu();
  });

  $("#login-close-icon").click(function() {
    Overlays.toggleLoginOverlay();
  });

  $("#login-button-wrapper").click(function() {
    Overlays.toggleLoginOverlay();
  });

  $("#signup-link").click(function() {
    Overlays.toggleLoginCredentialsPane();
  });

  $("#login-link").click(function() {
    Overlays.toggleLoginCredentialsPane();
  });
});
