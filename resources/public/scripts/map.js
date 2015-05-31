// () -> ()
function initialize() {
  var mapOptions = {
    center: { lat: 40.417, lng: -3.702}, // Puerta del Sol, Madrid
    zoom: 10,
    disableDefaultUI: true
  };
  var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

  setupMarkers(map);

  // gets user coordinates on right click
  google.maps.event.addListener(map, 'rightclick', function(event) {
    togglePane(PaneEnum.create);
    var latitude = event.latLng.lat();
    var longitude = event.latLng.lng();
    console.log(latitude + ', ' + longitude);
  });
}

// google.maps.Map -> ()
// Create the events and draw them to the given map
function setupMarkers(mapObject) {
  $.ajax({
    type: "GET",
    url: "api/events",
    datatype: "json",
    success: function (response) {
      for (var i = 0; i < response.length; i++) {
        var eventMarker = new google.maps.Marker({
          position: new google.maps.LatLng(response[i].latitude, response[i].longitude),
          map: mapObject
        });
        var config = {
          title: response[i].title,
          attending: response[i].attending
        };
        google.maps.event.addListener(eventMarker, 'click', function() {
          makeContent(config).open(mapObject, eventMarker);
        });
      }
    }
  });
}

// {} -> google.maps.InfoWindow
// Make a personalized InfoWindow with the given configuration
function makeContent(config) {
  var view = "<div class='info-window'><h2><a href='#' id='info-link' onclick='togglePane(PaneEnum.event)'>{{title}}</a></h2><p>{{attending}} user(s) are going.</p></div>";
  var infoContent = Mustache.render(view, config);
  return new google.maps.InfoWindow({
    content: infoContent
  });
}

google.maps.event.addDomListener(window, 'load', initialize);
