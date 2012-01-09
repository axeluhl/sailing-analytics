package com.sap.sailing.geocoding;

import java.util.Comparator;
import java.util.List;

import com.sap.sailing.geocoding.impl.ReverseGeocoderImpl;

public interface ReverseGeocoder {
    final ReverseGeocoder INSTANCE = new ReverseGeocoderImpl();
    final String GEONAMES_USER = "sailtrackint";
    
    Placemark getPlacemark(double latDeg, double lngDeg);

    List<Placemark> getPlacemarkNear(double latDeg, double lngDeg, float radius);
    
    Placemark getPlacemarkBest(double latDeg, double lngDeg, float radius, Comparator<Placemark> comp);
}
