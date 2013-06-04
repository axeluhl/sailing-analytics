package com.sap.sailing.domain.polarsheets;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class PolarFix {

    private SpeedWithBearing boatSpeed;
    private Wind wind;
    private double angleToWind;

    public PolarFix(GPSFixMoving fix, TrackedRace race) {
        boatSpeed = fix.getSpeed();
        Bearing bearing = boatSpeed.getBearing();
        Position position = fix.getPosition();
        wind = race.getWind(position, fix.getTimePoint());
        Bearing windBearing = wind.getFrom();
        angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();
    }

    public SpeedWithBearing getBoatSpeed() {
        return boatSpeed;
    }

    public Wind getWind() {
        return wind;
    }

    public double getAngleToWind() {
        return angleToWind;
    }
    
    

}
