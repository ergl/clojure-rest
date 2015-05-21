// All login-related functions go here

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
// Alerts the user if the user / pass combination is wrong, otherwise hides the overlay and continues to the site
function loginSubmit() {
  var user = document.getElementById('login-user-text').value;
  var pass = document.getElementById('login-pass-text').value;
  if (validateLoginCredentials(user, pass)) {
    if (user === 'mod' && pass === 'mod') {
      window.open('mod-panel.html', '_blank');
    }
    toggleLoginOverlay();
  } else {
    alert("Bad user/pass combination!");
  }
}

// String String -> Boolean
// Returns true if the login credentials are valid
// TODO: Actually do something
function validateLoginCredentials(user, pass) {
  return true;
}

// () -> ()
// Gets the values from the signup form.
// Alerts the user if the user / email / pass combination is wrong, otherwise hides the overlay and continues to the site
function signupSubmit() {
  var email = document.getElementById('signup-email-text').value;
  var user = document.getElementById('signup-user-text').value;
  var pass = document.getElementById('signup-pass-text').value;
  if (validateSignupCredentials(email, user, pass)) {
    toggleLoginOverlay();
  } else {
    alert("Uh, oh. Something went bad!");
  }
}

// String String String -> Boolean
// Returns true if the signup credentials are valid
// TODO: Actually do something
function validateSignupCredentials(email, user, pass) {
  return true;
}
