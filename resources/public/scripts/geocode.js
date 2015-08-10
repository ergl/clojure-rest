var Utils = (function () {

    // Int, Int, function -> function
    var reverseGeocodeWrapper = function(lat, lng, callback) {
        var geocoder = new google.maps.Geocoder();
        var latlng = new google.maps.LatLng(lat, lng);
        geocoder.geocode({"latLng": latlng}, function (results, status) {
            if (status === google.maps.GeocoderStatus.OK) {
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
            if (status === google.maps.GeocoderStatus.OK) {
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


    return {
        geocode: geocodeWrapper,
        reverseGeocode: reverseGeocodeWrapper
    };
}());
