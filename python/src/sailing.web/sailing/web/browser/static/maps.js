
var map;

function initializeMap() {
    var geo = new google.maps.LatLng(49.30683824737352, 8.64189809570314);
    var opts = {
      zoom: 9,
      center: geo,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById('map_wrapper'), opts);

    /* make sure to center map correctly */
    map.setCenter(geo);
    map.setZoom(map.getZoom());
}

function showBuoys() {
    GET('readWaypoints', {}, function(data) {
        for (b in data) {
            buoy = data[b];
            map.addMarkerWithLabel(buoy.lat, buoy.lng, buoy.name, null);
        }

        map.fit2Markers();
    });
}

function showTracksFor(timepoint, eventname, racename) {
    map.clearMarkers();
    showBuoys(eventname, racename);

    GET('readFixes', {eventname: eventname, racename: racename, timepoint: timepoint}, function(data) {
        for (t in data) {
            position = data[t];
            map.addMarker(position.lat, position.lng);
        }
    });
}

function toggleMarker(title) {
    var markers = new Array(); var cm;
    for (markerpos in map.markers) {
        marker = map.markers[markerpos]
        if (marker != undefined)
            if (marker.getTitle() == title) {
                marker.setMap(null);
                cm = marker;
            } else {
                markers.push(marker);
            }
    }

    if (cm != undefined) {
        map.markers = markers;

        if (cm.label == undefined) {
            new MarkerWithLabel({
                position: cm.getPosition(),
                draggable: false,
                title: cm.getTitle(),
                map: map,
                icon: cm.getIcon(),
                labelContent: cm.getTitle(),
                labelClass: 'maplabel'
            });
        } else {
            new google.maps.Marker({
                position: cm.getPosition(),
                draggable: false,
                title: cm.getTitle(),
                map: map,
                icon: cm.getIcon(),
            });
        }
    }
}

google.maps.Map.prototype.markers = new Array();

google.maps.Map.prototype.addMarkerWithLabel = function(lat, lng, title, icon) {
    point = new google.maps.LatLng(lat, lng);

    marker = new MarkerWithLabel({
        position: point,
        draggable: false,
        title: title,
        map: map,
        icon: icon,
        labelContent: title,
        labelClass: 'maplabel'
    });

};

google.maps.Map.prototype.addMarker = function(lat, lng, title, icon) {
    point = new google.maps.LatLng(lat, lng);

    marker = new google.maps.Marker({
        position: point,
        title: title,
        map: map,
        icon: icon
    });

    marker.deferInfo(title);

};

google.maps.Map.prototype.addConnectedMarkers = function(latsource, lngsource, lattarget, lngtarget) {
    source = new google.maps.LatLng(latsource, lngsource);
    target = new google.maps.LatLng(lattarget, lngtarget);

    source_marker = new google.maps.Marker({
        position: source,
        map: map
    });

    target_marker = new google.maps.Marker({
        position: target,
        map: map,
        icon: 'http://google-maps-icons.googlecode.com/files/wind.png'
    });

    path = new google.maps.Polyline({
        path: [source_marker.getPosition(), target_marker.getPosition()],
        strokeColor: "#FF0000",
        strokeOpacity: 1.0,
        strokeWeight: 1
    });

    map.addPath(path);
};

google.maps.Map.prototype.getMarkers = function() {
    return this.markers
};

google.maps.Map.prototype.clearMarkers = function() {
    for(var i=0; i<this.markers.length; i++){
        this.markers[i].setMap(null);
    }
    this.markers = new Array();
};

google.maps.Map.prototype.paths = new Array();

google.maps.Map.prototype.addPath = function(path) {
    path.setMap(this);
    this.paths.push(path);
}

google.maps.Map.prototype.clearPaths = function() {
    for(var i=0; i<this.paths.length; i++) {
        this.paths[i].setMap(null);
    }
    this.paths = new Array();
}

google.maps.Marker.prototype._setMap = google.maps.Marker.prototype.setMap;

google.maps.Marker.prototype.setMap = function(map) {
    if (map) {
        map.markers[map.markers.length] = this;
    }
    this._setMap(map);
}

google.maps.Marker.prototype.deferInfo = function(str) {
    google.maps.event.addListener(this, 'click', function() {
        info = new google.maps.InfoWindow();
        info.setContent(str);
        info.open(map, this);
    });
}

google.maps.Marker.prototype.showInfo = function(str) {
    info = new google.maps.InfoWindow();
    info.setContent(str);
    info.open(map, this);
}

google.maps.Map.prototype.connectMarkers = function() {
    coords = [];
    for(var i=0; i<this.markers.length; i++){
        pos = this.markers[i].getPosition();
        if (pos)
            coords.push(pos);
    }

    /* make sure to center the first point */
    map.setCenter(coords[0]);

    path = new google.maps.Polyline({
        path: coords,
        strokeColor: "#FF0000",
        strokeOpacity: 1.0,
        strokeWeight: 2
    });

    map.addPath(path);
}

google.maps.Map.prototype.fit2Markers = function() {
    bounds = new google.maps.LatLngBounds();

    for(var i=0; i<this.markers.length; i++){
        pos = this.markers[i].getPosition();

        if (pos != undefined)
            bounds.extend(pos);
    }

    map.setCenter(bounds.getCenter());
    map.fitBounds(bounds);
}

google.maps.Map.prototype.randomMarker = function(title, icon) {
    var bounds = map.getBounds();
    var southWest = bounds.getSouthWest();
    var northEast = bounds.getNorthEast();
    var lngSpan = northEast.lng() - southWest.lng();
    var latSpan = northEast.lat() - southWest.lat();
    var point = new google.maps.LatLng(southWest.lat() + latSpan * Math.random(),
        southWest.lng() + lngSpan * Math.random());

    marker = new google.maps.Marker({
        position: point,
        title: title,
        map: map,
        icon: icon
    });

    marker.deferInfo(title);
}

