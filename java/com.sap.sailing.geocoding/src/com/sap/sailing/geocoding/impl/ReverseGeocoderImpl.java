package com.sap.sailing.geocoding.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sap.sailing.geocoding.Placemark;
import com.sap.sailing.geocoding.ReverseGeocoder;

public class ReverseGeocoderImpl implements ReverseGeocoder {
    
    private final String NEARBY_PLACE_SERVICE = "http://api.geonames.org/findNearbyPlaceNameJSON?";
    private int maxRows = 500;

    @Override
    public Placemark getPlacemark(double latDeg, double lngDeg) {
        // TODO Auto-generated method stub
        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(latDeg));
        url.append("&lng=" + Double.toString(lngDeg));
        url.append("&username=" + GEONAMES_USER);
        
        return null;
    }

    @Override
    public List<Placemark> getPlacemarkNear(double latDeg, double lngDeg, float radius) {
        // TODO Auto-generated method stub
        StringBuilder url = new StringBuilder(NEARBY_PLACE_SERVICE);
        url.append("&lat=" + Double.toString(latDeg));
        url.append("&lng=" + Double.toString(lngDeg));
        url.append("&radius=" + Float.toString(radius));
        url.append("&maxRows=" + Integer.toString(maxRows));
        url.append("&username=" + GEONAMES_USER);
        
        return null;
    }

    @Override
    public Placemark getPlacemarkBest(double latDeg, double lngDeg, float radius, Comparator<Placemark> comp) {
        List<Placemark> placemarks = getPlacemarkNear(latDeg, lngDeg, radius);
        Collections.sort(placemarks, comp);
        
        return placemarks.get(0);
    }
    
}
