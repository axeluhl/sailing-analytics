package com.sap.sailing.gwt.ui.simulator;


public class WindControlParameters {
    public double windSpeedInKnots;
    public double windBearing;
    
    public WindControlParameters(double speedInKnots, double bearing) {
        windSpeedInKnots = speedInKnots;
        windBearing = bearing;
    }
}
