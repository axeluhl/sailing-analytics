package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.impl.NauticalMileDistance;

public interface Distance {
    static final Distance NULL = new NauticalMileDistance(0);
    
    double getGeographicalMiles();

    double getSeaMiles();

    double getNauticalMiles();

    double getMeters();

    double getKilometers();

    double getCentralAngleDeg();

    double getCentralAngleRad();

    Distance scale(double factor);
    
    /**
     * Computes the (undirected) average speed one has traveled at when passing
     * this distance in the number of milliseconds specified.
     */
    Speed inTime(long milliseconds);
}
