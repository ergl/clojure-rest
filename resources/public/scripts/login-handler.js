var LoginHandler = (function() {
	"use strict";

	// () -> ()
	// Gets the values from the login form.
	var loginSubmit = function () {
		var user = document.getElementById('login-user-text').value;
		var pass = document.getElementById('login-pass-text').value;
		if (user && pass) {
			validateLoginCredentials(user, pass);
		} else {
			Overlays.showErrorDialog("Debes rellenar usuario y contraseña");
		}
	};

	// String String -> ()
	// Alerts the user if the user / pass combination is wrong
	// Otherwise hides the overlay and continues to the site
	// TODO: If user is a moderator, show the mod queue link maybe?
	var validateLoginCredentials = function(user, pass) {
		$.ajax({
			type: "POST",
			url: "api/auth",
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify({username: user, password: pass}),
			dataType: "json",
			success: function(response) {
				localStorage.setItem('accessToken', response.token);
				$("#login-button-text").text("LOGOUT");
				Overlays.toggleLoginOverlay();
				toggleLoggedClass();
			},
			statusCode: {
				400: function() {
					Overlays.showErrorDialog("Uh oh, something went wrong. Please try again!");
				},
				401: function() {
					Overlays.showErrorDialog("Bad username / password combination!");
				}
			}
		});
	};

	// () -> ()
	// Gets the values from the signup form.
	var signupSubmit = function () {
		var email = document.getElementById('signup-email-text').value;
		var user = document.getElementById('signup-user-text').value;
		var pass = document.getElementById('signup-pass-text').value;
		var re = /.*@.*\..*/; // test for x@x.x

		if (email && user && pass && re.test(email)) {
			validateSignupCredentials(email, user,pass);
		} else {
			Overlays.showErrorDialog("Introduce correctamente todos los datos");
		}
	};


	// String String String -> ()
	// Alerts the user if the user / email / pass combination is wrong
	// Otherwise hides the overlay and continues to the site
	// TODO: Store the username and profile pic somewhere
	var validateSignupCredentials = function (email, user, pass) {
		$.ajax({
			type: "POST",
			url: "api/users",
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify({email: email, username: user, password: pass}),
			datatyse: "json",
			success: function(response) {
				validateLoginCredentials(user, pass);
			},
			statusCode: {
				400: function() {
					Overlays.showErrorDialog("Seems like something went wrong, please try a different email / username");
				},
				500: function() {
					Overlays.showErrorDialog("Uh oh. Something went really wrong!");
				}
			}
		});
	};

	// () -> ()
	// Toggles the logged-in class, meaning that we now present a logged-in interface
	var toggleLoggedClass = function () {
		$("#container").toggleClass('logged-in');
	};

	var logout = function() {
		var authToken = localStorage.getItem('accessToken');
		$.ajax({
			type: "DELETE",
			url: "api/auth/" + authToken,
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify({token: authToken}),
			dataType: "json",
			success: function(data, textStatus, jqXHR) {
				if (jqXHR.status === 204) {
					toggleLoggedClass();
					$("#login-button-text").text("LOGIN");
					localStorage.removeItem('accessToken');
				}
			},
			statusCode: {
				403: function() {
					Overlays.showErrorDialog("You did something bad, and you know it");
				},
				404: function() {
					Overlays.showErrorDialog("Well this is embarrassing, seems you are already logged out...");
				}
			}
		});
	};


	return {
		loginSubmit: loginSubmit,
		signupSubmit: signupSubmit,
		isLogedIn: function() {
			return document.getElementById('container').classList.contains('logged-in');
		},
		logout: logout
	};
}());

$(function () {

	var ENTER_KEY_CODE = 13;

	var enterLoginFunction = function(e) {
		e = e || window.event;
		if (e.which === ENTER_KEY_CODE) {
			LoginHandler.loginSubmit();
		}
	};

	var enterSignupFunction = function(e) {
		e = e || window.event;
		if (e.which === ENTER_KEY_CODE) {
			LoginHandler.signupSubmit();
		}
	};

	$(".login-input").keyup(enterLoginFunction);


	$(".signup-input").keyup(enterSignupFunction);


	$("#login-submit-button").click(LoginHandler.loginSubmit);

	$("#signup-submit-button").click(LoginHandler.signupSubmit);
});
