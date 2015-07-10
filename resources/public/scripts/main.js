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

	var showErrorDialog = function(message){
		var template = $("#error-template").html();
		Mustache.parse(template);
		$("#error-message").html(Mustache.render(template, {message: message}));
		$("#container").addClass("show-error-overlay");
	};

	var hideErrorDialog = function() {
		$("#container").removeClass("show-error-overlay");
	};


	return {
		toggleLoginOverlay: toggleLoginOverlay,
		toggleLoginCredentialsPane: toggleLoginCredentialsPane,
		toggleUserPane: toggleUserPane,
		toggleEventPane: toggleEventPane,
		toggleCreatePane: toggleCreatePane,
		toggleMenu: toggleMenu,
		showErrorDialog: showErrorDialog,
		hideErrorDialog: hideErrorDialog
	};
}());

$(function() {

	var ESC_KEY_CODE = 27;
	document.addEventListener('keydown', function(e) {
		e = e || window.event;
		var classList = document.getElementById('container').classList;
		if (e.keyCode == ESC_KEY_CODE) {
			if (classList.contains('show-error-overlay')) {
				Overlays.hideErrorDialog();
			} else if (classList.contains('show-login-overlay')) {
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

	if (localStorage.getItem('accessToken') !== null) {
		$("#container").toggleClass('logged-in');
		$("#login-button-text").text("LOGOUT");
	}

	$("#error-close-icon").click(function () {
		Overlays.hideErrorDialog();
	});

	$("#create-button").click(function () {
		CreateEventHandler.setupPane();
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
		if (LoginHandler.isLogedIn()) {
			LoginHandler.logout();
		} else {
			Overlays.toggleLoginOverlay();
		}
	});

	$("#signup-link").click(function() {
		Overlays.toggleLoginCredentialsPane();
	});

	$("#login-link").click(function() {
		Overlays.toggleLoginCredentialsPane();
	});

	$("#submit-event-add-file-button").click(function () {
		$("#submit-event-add-file").click();
	});

	$("#retrieve-comments").click(function() {
		var id = document.getElementById('event-template').dataset.eventId;
		ViewEventHandler.viewComments(id);
	})
});
