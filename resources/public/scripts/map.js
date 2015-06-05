var map =  (function() {
	"use strict";

	var eventList = [];
	var markerContent = "<div class='info-window'><h2><a href='#' id='info-link' onclick='togglePane(PaneEnum.event)'>{{title}}</a></h2><p>{{attending}} user(s) are going.</p></div>";

	// google.maps.Marker, google.maps.Map, google.maps.InfoWindow, String -> ()
	function makeInfoWindow(marker, map, infoWindow, content) {
		google.maps.event.addListener(marker, 'mouseover', function() {
			infoWindow.setContent(content);
			infoWindow.open(map, marker);
		});
	}

	// google.maps.Map -> ()
	function setupMarkers(mapObject) {
		$.ajax({
			type: "GET",
			url: "api/events",
			datatype: "json",
			success: function(response) {
				var infoWindow = new google.maps.InfoWindow();
				for (var i = 0; i < response.length; i++) {
					var event = {
						id: response[i].eventsid,
						title: response[i].title,
						attending: response[i].attending,
						latitude: response[i].latitude,
						longitude: response[i].longitude
					};

					var eventMarker = new google.maps.Marker({
						position: new google.maps.LatLng(event.latitude, event.longitude),
						map: mapObject
					});

					makeInfoWindow(eventMarker, mapObject, infoWindow, Mustache.render(markerContent, event));

					eventList.push(event);
				}
			}
		});
	}

	// Int, Int -> ()
	// Creates the map at the given lat and lng
	function createMap(lat, lng) {
		var mapOptions = {
			center: new google.maps.LatLng(lat, lng),
			zoom: 10,
			disableDefaultUI: true
		};

		var mapCanvas = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

		setupMarkers(mapCanvas);

		google.maps.event.addListener(mapCanvas, 'rightclick', function(event) {
			Overlays.toggleCreatePane();
			var latitude = event.latLng.lat();
			var longitude = event.latLng.lng();
			console.log(latitude + ', '  + longitude);
		});
	}

	// () -> ()
	// Gets the user location and initializes the map to that location
	function initialize() {
		if (navigator.geolocation) {
			var success = function (position) {
				createMap(position.coords.latitude, position.coords.longitude);
			};

			var error = function () {
				createMap(40.417, -3.702); // Puerta del Sol, Madrid
			};

			navigator.geolocation.getCurrentPosition(success, error);
		} else {
			createMap(40.417, -3.702); // Puerta del Sol, Madrid
		}
	}

	return {
		initialize: initialize()
	};
}());

google.maps.event.addDomListener(window, 'load', map.initialize);
