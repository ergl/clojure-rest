// All search widget related functions go here
var searchOption = "event";

// () -> ()
// Sets the search query to users, updates the button style
function searchUsers() {
  searchOption = "user";
  document.getElementById('search-user-button').style.backgroundColor = "#DADADA";
  document.getElementById('search-event-button').style.backgroundColor = "#C0C0C0";
}

// () -> ()
// Sets the search query to events, updates the button style
function searchEvents() {
  searchOption = "event";
  document.getElementById('search-event-button').style.backgroundColor = "#DADADA";
  document.getElementById('search-user-button').style.backgroundColor = "#C0C0C0";
}

// Event -> ()
function searchKeyPress(e) {
  var ENTER_KEY_CODE = 13;
  e = e || window.event;
  if (e.keyCode == ENTER_KEY_CODE) {
    var query = e.target.value;
    e.target.value = "";
    searchQuery(query, searchOption);
  }
}

function searchButtonPress() {
  var query = document.getElementById('search-input').value;
  searchQuery(query, searchOption);
}

// String, Function -> ()
// Prints to the console the query parameters
function searchQuery(query, type) {
  console.log(type);
  console.log(query);
}