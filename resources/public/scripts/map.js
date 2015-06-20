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

	// () -> ()
	// Gets the user location and initializes the map to that location
	// Then continues to watch the user location, updating the map
	function initialize() {
		var mapOptions = {
			center: new google.maps.LatLng(40.417, -3.702),
			zoom: 12,
			disableDefaultUI: true
		};

		var mapCanvas = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

		setupMarkers(mapCanvas);

		google.maps.event.addListener(mapCanvas, 'rightclick', function(event) {
			CreateEventHandler.setupPane(event);
		});


		if (navigator.geolocation) {
			navigator.geolocation.watchPosition(function (position) {
				var location = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
				mapCanvas.panTo(location);
			});
		}
	}

	return {
		initialize: initialize()
	};
}());

google.maps.event.addDomListener(window, 'load', map.initialize);
