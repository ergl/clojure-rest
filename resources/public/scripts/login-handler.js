// All login-related functions go here

$(function (){
    $("#login-close-icon").click(function() {
        toggleLoginOverlay();
    });

    $("#login-button-wrapper").click(function() {
        toggleLoginOverlay();
    });

    $("#signup-link").click(function() {
      toggleCredentialsPane();
    });

    $("#login-link").click(function() {
        toggleCredentialsPane();
    });

    $("#login-submit-button").click(function() {
        loginSubmit();
    });

    $("#signup-submit-button").click(function() {
        signupSubmit();
    });
});

// () -> ()
// Shows the login dialog, darkens the background
function toggleLoginOverlay() {
	document.getElementById('container').classList.toggle('show-login-overlay');
	document.getElementById('login-user-text').focus();
}

// () -> ()
// Switches between the login and the signup pane inside the overlay box
function toggleCredentialsPane() {
	document.getElementById('login-overlay').classList.toggle('show-signup-pane');
	document.getElementById(
		(document.getElementById('login-overlay').classList.contains('show-signup-pane'))
		? 'signup-email-text' : 'login-user-text').focus();
}

// () -> ()
// Gets the values from the login form.
function loginSubmit() {
	var user = document.getElementById('login-user-text').value;
	var pass = document.getElementById('login-pass-text').value;
    validateLoginCredentials(user, pass);
}

// String String -> ()
// Alerts the user if the user / pass combination is wrong
// Otherwise hides the overlay and continues to the site
// TODO: If user is a moderator, show the mod queue link maybe?
function validateLoginCredentials(user, pass) {
	$.ajax({
        type: "POST",
        url: "api/auth",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({username: user, password: pass}),
        datatype: "json",
        success: function(response) {
            localStorage.setItem('accessToken', response.token);
            toggleLoginOverlay();
            toggleLoggedClass();
        },
        statusCode: {
            400: function() {
                alert("Uh oh, something went wrong. Please try again!");
            },
            401: function() {
                alert("Bad username / password combination!");
            }
        }
    })
}

// () -> ()
// Gets the values from the signup form.
function signupSubmit() {
	var email = document.getElementById('signup-email-text').value;
	var user = document.getElementById('signup-user-text').value;
	var pass = document.getElementById('signup-pass-text').value;
    validateSignupCredentials(email, user,pass);
}

// String String String -> ()
// Alerts the user if the user / email / pass combination is wrong
// Otherwise hides the overlay and continues to the site
// TODO: Store the username and profile pic somewhere
function validateSignupCredentials(email, user, pass) {
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
                alert("Seems like something went wrong, please try a different email / username");
            },
            500: function() {
                alert("Uh oh. Something went really wrong!");
            }
        }
    })
}

// () -> ()
// Toggles the logged-in class, meaning that we now present a logged-in interface
function toggleLoggedClass() {
	document.getElementById('container').classList.toggle('logged-in');
}
