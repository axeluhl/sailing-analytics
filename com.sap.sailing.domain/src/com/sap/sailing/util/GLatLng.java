package com.sap.sailing.util;

public class GLatLng {
    private final double lat;
    private final double lng;
    
    public GLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double lat() {
        return lat;
    }

    public double lng() {
        return lng;
    }

}
