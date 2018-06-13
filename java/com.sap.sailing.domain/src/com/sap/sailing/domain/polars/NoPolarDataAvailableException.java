package com.sap.sailing.domain.polars;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class NoPolarDataAvailableException extends Exception {

    private static final long serialVersionUID = -6939576877174310618L;

    private final BoatClass boatClass;
    private final Speed windSpeed;
    private final Bearing bearingToTheWind;

    public NoPolarDataAvailableException(BoatClass boatClass, Speed windSpeed, Bearing bearingToTheWind) {
        this.boatClass = boatClass;
        this.windSpeed = windSpeed;
        this.bearingToTheWind = bearingToTheWind;
    }

    @Override
    public String getMessage() {
        return String
                .format("There was no polar data available for boat class '%s' for an angle to the wind of %.1f degrees and the wind speed of %.1f knots.",
                        boatClass.getName(), bearingToTheWind.getDegrees(), windSpeed.getKnots());
    }

}
