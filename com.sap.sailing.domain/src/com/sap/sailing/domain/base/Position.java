package com.sap.sailing.domain.base;

public interface Position {
    double getLatRad();

    double getLngRad();

    double getLatDeg();

    double getLngDeg();

    /**
     * Central angle between this position and the other position
     */
    double getCentralAngleRad(Position p);

    double distanceInSeaMiles(Position p);

    double distanceInMeters(Position p);
}
