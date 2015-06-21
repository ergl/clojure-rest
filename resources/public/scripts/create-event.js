var CreateEventHandler = (function() {
	"use strict";

	var latitude = null;
	var longitude = null;

	// Int, Int, function -> function
	var reverseGeocodeWrapper = function(lat, lng, callback) {
		var geocoder = new google.maps.Geocoder();
		var latlng = new google.maps.LatLng(lat, lng);
		geocoder.geocode({"latLng": latlng}, function (results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				if (callback && typeof(callback) === 'function') {
					callback(results);
				} else {
					console.log("callback is not a function: reverseGeocode(lat, lng, callback)");
				}
			} else {
				console.log('Geocoder failuer due to:' + status);
			}
		});
	};

	// String, function -> function
	var geocodeWrapper = function(address, callback) {
		var geocoder = new google.maps.Geocoder();
		geocoder.geocode({"address": address}, function (results, status) {
			if (status == google.maps.GeocoderStatus.OK) {
				latitude = results[0].geometry.location.A;
				longitude = results[0].geometry.location.F;
				if (callback && typeof(callback) === 'function') {
					callback(latitude, longitude);
				} else {
					console.log("callback is not a function: geoCode(address, callback)");
				}
			} else {
				console.log('Geocoder failuer due to:' + status);
			}
		});
	};

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

				reverseGeocodeWrapper(latitude, longitude, function(results) {
					updateAddressInput((results[1]) ? results[1].formatted_address : "Try another address maybe?")
				});
			}

			Overlays.toggleCreatePane();
		}
	};

	// String, String, String, Int, Int, String -> ()
	var sendEvent = function(authToken, title, content, lat, lng, initialdate) {
		var coordinateString = lat + ", " + lng;

		var payload = {
			token: authToken,
			title: title,
			content: content,
			coordinates: coordinateString,
			initialdate: initialdate
		};

		console.log(payload);

		$.ajax({
			type: "POST",
			url: "api/events",
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify(payload),
			dataType: "json",
			success: function(response) {
				// TODO: Do anything else?
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
		var initialdate = moment().format('YYYY-MM-DD');

		if (!authToken) {
			Overlays.showErrorDialog("You monster! - You haven't logged in");
			return;
		}

		if (latitude && longitude) {
			sendEvent(authToken, title, content, latitude, longitude, initialdate);
		} else {
			geocodeWrapper(address, function(lat, lng) {
				sendEvent(authToken, title, content, lat, lng, initialdate);
			})
		}
	};
	
	return {
		setupPane: function(e) {
			setupPane(e)
		},
		submit: function() {
			eventSubmit();
		}
	}
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
