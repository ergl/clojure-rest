var SearchHandler = (function() {
   "use strict";

    var searchOption = {
        "event": 1,
        "users": 2
    };

    var option = searchOption.event;

    // Sets the search query to users, updates the button style
    var searchUsers = function () {
        option = searchOption.users;
        $("#search-user-button").css("background-color", "#DADADA");
        $("#search-event-button").css("background-color", "#C0C0C0");
    };

    // Sets the search query to events, updates the button style
    var searchEvents = function () {
        option = searchOption.event;
        $("#search-event-button").css("background-color", "#DADADA");
        $("#search-user-button").css("background-color", "#C0C0C0");
    };

    var searchQuery = function () {
        var query = $("#search-input");
        var queryValue = query.val().trim().split(" ")[0]; query.val('');
        if (option === searchOption.event) {
            searchEventQuery(queryValue);
        } else {
            searchUserQuery(queryValue);
        }
    };

    var searchUserQuery = function (query) {
        $.ajax({
            type: "GET",
            url: "api/users/search/" + query,
            datatype: "json",
            success: function(response) {
                for (var i = 0; i < response.length; i++) {
                    var result = {
                        username: response[i].username,
                        profileImage: response[i].profileImage
                    };

                    // TODO: Display username, pic and link to user on search pane
                    console.log(result);
                }
            }
        });
    };

    var searchEventQuery = function (query) {
        $.ajax({
            type: "GET",
            url: "api/events/search/" + query,
            datatype: "json",
            success: function(response) {
                for (var i = 0; i < response.length; i++) {
                    var result = {
                        id: response[i].eventsid,
                        title: response[i].title,
                        attending: response[i].attending,
                        latitude: response[i].latitude,
                        longitude: response[i].longitude
                    };

                    // TODO: Display title and link to event on search pane
                    console.log(result);
                }
            }
        });
    };


    return {
        searchUsers: searchUsers,
        searchEvents: searchEvents,
        searchQuery: searchQuery
    };
}());

$(function () {

    var ENTER_KEY_CODE = 13;
    $("#search-input").keyup(function (e) {
        e = e || window.event;
        if (e.which === ENTER_KEY_CODE) {
            SearchHandler.searchQuery();
        }
    });

    $("#search-button").click(function () {
        SearchHandler.searchQuery();
    });

    $("#search-event-button").click(function () {
        SearchHandler.searchEvents();
    });

    $("#search-user-button").click(function () {
        SearchHandler.searchUsers();
    });
});
