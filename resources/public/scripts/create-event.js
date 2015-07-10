var CreateEventHandler = (function() {
	"use strict";

	var latitude = null;
	var longitude = null;

	// String -> ()
	var updateAddressInput = function(content) {
		content = content || "";
		document.getElementById('create-event-location').value = content;
	};

	// Window.Event -> ()
	var setupPane = function(e) {
		if (LoginHandler.isLogedIn()) {
			latitude = null;
			longitude = null;
			document.getElementById('create-event-title').value = "";
			document.getElementById('create-event-description').value = "";
			updateAddressInput();

			if (e) {
				latitude = e.latLng.lat();
				longitude = e.latLng.lng();

				Utils.reverseGeocode(latitude, longitude, function(results) {
					updateAddressInput((results[1]) ? results[1].formatted_address : "Try another address maybe?")
				});
			}

			Overlays.toggleCreatePane();
		}
	};

	// String, String, String, Int, Int, String -> ()
	var sendEvent = function(authToken, title, content, lat, lng, initialDate) {
		var coordinateString = lat + ", " + lng;

		var payload = {
			token: authToken,
			title: title,
			content: content,
			coordinates: coordinateString,
			initialdate: initialDate
		};

		console.log(payload);

		$.ajax({
			type: "POST",
			url: "api/events",
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify(payload),
			dataType: "json",
			success: function(response) {
				Overlays.toggleCreatePane();
				map.reload();
				console.log(response)
			},
			statusCode: {
				400: function() {
					Overlays.showErrorDialog("Uh oh, something went wrong. Please try again!");
				},
				401: function() {
					// TODO: Overlay log in?
					Overlays.showErrorDialog("Seems like you are not logged in!");
				}
			}
		});
	};

	// () -> ()
	var eventSubmit = function() {
		var title = document.getElementById('create-event-title').value;
		var address = document.getElementById('create-event-location').value;
		var content = document.getElementById('create-event-description').value;

		if (!(title && content && address)) {
			Overlays.showErrorDialog("Please, fill all required fields");
			return;
		}

		var authToken = localStorage.getItem('accessToken');
		var initialDate = computeDateOfToday();

		if (!authToken) {
			Overlays.showErrorDialog("You monster! - You haven't logged in");
			return;
		}

		if (latitude && longitude) {
			sendEvent(authToken, title, content, latitude, longitude, initialDate);
		} else {
			Utils.geocode(address, function(lat, lng) {
				sendEvent(authToken, title, content, lat, lng, initialDate);
			});
		}
	};

	var computeDateOfToday = function() {
		var now = new Date();

		return [
			now.getFullYear().toString(),
			padWithZerosIfNecessary((now.getMonth() + 1).toString(), 2),
			padWithZerosIfNecessary(now.getDate().toString(), 2)
		].join('-');
	};

	var padWithZerosIfNecessary = function(aString, desiredLength) {
		var paddedString = aString;

		while (paddedString.length < desiredLength) {
			paddedString = '0' + paddedString;
		}

		return paddedString;
	};

	return {
		setupPane: function(e) {
			setupPane(e)
		},
		submit: function() {
			eventSubmit();
		}
	};
}());

$(function () {

	var ENTER_KEY_CODE = 13;

	var enterSubmitEvent = function(e) {
		e = e || window.event;
		if (e.which == ENTER_KEY_CODE) {
			CreateEventHandler.submit();
		}
	};

	$("#create-event-title").keyup(function(e) {
		enterSubmitEvent(e);
	});

	$("#create-event-location").keyup(function(e) {
		enterSubmitEvent(e);
	});

	$("#submit-new-event").click(function() {
		CreateEventHandler.submit();
	});
});
