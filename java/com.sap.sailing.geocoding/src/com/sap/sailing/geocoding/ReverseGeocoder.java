package com.sap.sailing.geocoding;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.json.simple.parser.ParseException;

import com.sap.sailing.geocoding.impl.ReverseGeocoderImpl;

public interface ReverseGeocoder {
    final ReverseGeocoder INSTANCE = new ReverseGeocoderImpl();
    final String GEONAMES_USER = "sailtracking";
    
    Placemark getPlacemark(double latDeg, double lngDeg) throws IOException, ParseException;

    List<Placemark> getPlacemarkNear(double latDeg, double lngDeg, float radius) throws IOException, ParseException;
    
    Placemark getPlacemarkBest(double latDeg, double lngDeg, float radius, Comparator<Placemark> comp) throws IOException, ParseException;
}
