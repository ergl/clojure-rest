var CreateEventHandler = (function() {
	"use strict";
	
	var setupPane = function(e) {
		e = e || window.event;
		if (LoginHandler.isLogedIn()) {
			Overlays.toggleCreatePane();
			if (e) {
				var lat = e.latLng.lat();
				var lon = e.latLng.lng();
				console.log(lat + ', '  + lon);
			}
		}
	};
	
	return {
		setupPane: function(e) {
			setupPane(e)
		}
	}
}());