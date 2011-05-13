package com.sap.sailing.domain.base;

public interface Speed {
    /**
     * @return bearing in degrees where 0 is north, 90 is east, 180 is south, 270 is west
     */
    double getBearingDeg();
    double getBearingRad();
    double getKnots();
    double getMetersPerSecond();
    double getKilometersPerHour();
}
