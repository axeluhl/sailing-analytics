package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Bearing;

public class KnotSpeedWithBearingImpl extends AbstractSpeedWithBearingImpl {
    private final double speedInKnots;
    
    private final double confidence;
    
    public KnotSpeedWithBearingImpl(double speedInKnots, Bearing bearing) {
        this(speedInKnots, bearing, AbstractSpeedWithConfidence.DEFAULT_SPEED_CONFIDENCE);
    }
    
    public KnotSpeedWithBearingImpl(double speedInKnots, Bearing bearing, double confidence) {
        super(bearing);
        this.speedInKnots = speedInKnots;
        this.confidence = confidence;
    }
    
    @Override
    public double getConfidence() {
        return confidence;
    }

    @Override
    public double getKnots() {
        return speedInKnots;
    }

}
