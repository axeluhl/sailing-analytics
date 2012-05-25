package com.sap.sailing.simulator;


public class WindControlParameters {
    /**
     * Base wind speed in knots
     */
    public Double baseSpeed;
    /**
     * Base average wind direction in degrees
     */
    public Double windBearing;
    
    public WindControlParameters(double speedInKnots, double bearing) {
        baseSpeed = speedInKnots;
        windBearing = bearing;
    }
}
