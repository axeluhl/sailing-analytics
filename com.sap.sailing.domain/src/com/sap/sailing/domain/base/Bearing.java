package com.sap.sailing.domain.base;

public interface Bearing {

    double getDegrees();

    double getRadians();

    /**
     * adds or subtracts 180 degrees to obtain the opposite bearing
     */
    Bearing reverse();

}
