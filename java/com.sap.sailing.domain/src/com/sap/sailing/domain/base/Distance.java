package com.sap.sailing.domain.base;

import com.sap.sailing.domain.base.impl.NauticalMileDistance;

/**
 * A distance which can be converted to various units of measure. Can be negative.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Distance extends Comparable<Distance> {
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
     * Computes the (undirected) average speed one has traveled at when passing this distance in the number of
     * milliseconds specified. Sign-sensitive, meaning, e.g., that if this distance is negative and the time is positive
     * then the resulting speed will be negative.
     */
    Speed inTime(long milliseconds);
}
