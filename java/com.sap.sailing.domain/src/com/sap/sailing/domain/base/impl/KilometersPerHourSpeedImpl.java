package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.SpeedWithConfidence;

public class KilometersPerHourSpeedImpl extends AbstractSpeedWithConfidence implements SpeedWithConfidence {
    private final double speedInKilometersPerHour;
    
    private final double confidence;

    public KilometersPerHourSpeedImpl(double speedInKilometersPerHour) {
        this.speedInKilometersPerHour = speedInKilometersPerHour;
        this.confidence = DEFAULT_SPEED_CONFIDENCE;
    }
    
    private KilometersPerHourSpeedImpl(double speedInKilometersPerHour, double confidence) {
        this.speedInKilometersPerHour = speedInKilometersPerHour;
        this.confidence = confidence;
    }
    
    @Override
    public double getConfidence() {
        return confidence;
    }

    @Override
    public double getKnots() {
        return getKilometersPerHour() * 1000. / Mile.METERS_PER_NAUTICAL_MILE;
    }

    @Override
    public double getMetersPerSecond() {
        return getKilometersPerHour() / 3.6;
    }

    @Override
    public double getKilometersPerHour() {
        return speedInKilometersPerHour;
    }

    @Override
    protected KilometersPerHourSpeedImpl createInstanceOfSameType(double metersPerSecond, double confidence) {
        return new KilometersPerHourSpeedImpl(metersPerSecond/3.6, confidence);
    }

}
