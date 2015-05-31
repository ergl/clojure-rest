var map =  (function() {
	"use strict";

	var eventList = [];

	// {} -> google.maps.InfoWindow
	function makeContent(config) {
		var view = "<div class='info-window'><h2><a href='#' id='info-link' onclick='togglePane(PaneEnum.event)'>{{title}}</a></h2><p>{{attending}} user(s) are going.</p></div>";
		var infoContent = Mustache.render(view, config);
		return new google.maps.InfoWindow({
			content: infoContent
		});
	}

	// google.maps.Map -> ()
	function setupMarkers(mapObject) {
		$.ajax({
			type: "GET",
			url: "api/events",
			datatype: "json",
			success: function (response) {
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

					var config = {
						title: event.title,
						attending: event.attending
					};

					eventList.push(event);

					google.maps.event.addListener(eventMarker, 'click', function() {
						makeContent(config).open(mapObject, eventMarker);
					});
				}
			}
		});
	}

	// () -> ()
	function initialize() {
		var mapoptions = {
			center: { lat: 40.417, lng: -3.702}, // Puerta del Sol, Madrid
			zoom: 10,
			disableDefaultUI: true
		};

		var mapCanvas = new google.maps.Map(document.getElementById('map-canvas'), mapoptions);

		setupMarkers(mapCanvas);

		google.maps.event.addListener(mapCanvas, 'rightclick', function (event) {
			togglePane(PaneEnum.create);
			var latitude = event.latLng.lat();
			var longitude = event.latLng.lng();
			console.log(latitude + ', '  + longitude);
		});
	}

	return {
		initialize: initialize()
	};
}());

google.maps.event.addDomListener(window, 'load', map.initialize);
