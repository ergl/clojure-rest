// () -> ()
// Toggles the add user to event component
function toggleAddUser() {
  var elem = document.getElementById('componente-usuario');
  elem.style.display = (elem.style.display == 'none') ? 'block' : 'none';
}

// Event -> ()
// Cleans up the comment textarea
function cleanupCommentOnEnter(e) {
  var ENTER_KEY_CODE = 13;
  e = e || window.event;
  if (e.keyCode == ENTER_KEY_CODE) {
    document.getElementById('body-comment').value = "";
  }
}

// () -> ()
// Cleans up the comment textarea
function cleanupComment() {
  document.getElementById('body-comment').value = "";
}
