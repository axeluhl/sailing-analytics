package com.sap.sailing.geocoding;

import java.util.Comparator;

public interface Placemark {
    
    String getName();
    String getCountryCode();
    double getLatDeg();
    double getLngDeg();
    String getType();
    long getPopulation();
    
    float distanceFrom(double latDeg, double lngDeg);
    String getCountryName(); 
    
    /**
     * Sorts the Placemarks by Population from low to high.
     * @author Lennart Hensler (D054527)
     *
     */
    public class ByPopulation implements Comparator<Placemark> {
        @Override
        public int compare(Placemark p1, Placemark p2) {
            return new Long(p1.getPopulation()).compareTo(p2.getPopulation());
        }
    }
    
    public class ByDistance implements Comparator<Placemark> {
        @Override
        public int compare(Placemark p1, Placemark p2) {
            return 0;
        }
    }
    
}
