package com.sap.sailing.gwt.ui.simulator;


public class WindControlParameters {
    public Double speed;
    public double windSpeedInKnots;
    public double windBearing;
    
    public WindControlParameters(double speedInKnots, double bearing) {
        speed = speedInKnots;
        windSpeedInKnots = speedInKnots;
        windBearing = bearing;
    }
}
