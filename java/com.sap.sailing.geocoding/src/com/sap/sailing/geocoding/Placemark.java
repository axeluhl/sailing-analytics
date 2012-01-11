package com.sap.sailing.geocoding;

import java.util.Comparator;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;

public interface Placemark {
    
    String getName();
    String getCountryCode();
    Position getPosition();
    String getType();
    long getPopulation();

    Distance distanceFrom(Position p);
    Distance distanceFrom(double latDeg, double lngDeg);
    String getCountryName(); 
    
    /**
     * Sorts the Placemarks by Population from low to high.
     * @author Lennart Hensler (D054527)
     *
     */
    public class ByPopulation implements Comparator<Placemark> {
        @Override
        public int compare(Placemark p1, Placemark p2) {
            return p1.getPopulation() > p2.getPopulation() ? 1 : p1.getPopulation() < p2.getPopulation() ? -1 : 0;
//            return new Long(p1.getPopulation()).compareTo(p2.getPopulation());
        }
    }
    
    public class ByDistance implements Comparator<Placemark> {
        @Override
        public int compare(Placemark p1, Placemark p2) {
            return 0;
        }
    }
    
}
