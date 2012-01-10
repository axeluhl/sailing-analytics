package com.sap.sailing.geocoding;

public interface Placemark {
    
    String getName();
    String getCountryCode();
    double getLatDeg();
    double getLngDeg();
    String getType();
    long getPopulation();
    
    float distanceFrom(double latDeg, double lngDeg);
    String getCountryName(); 
    
}
