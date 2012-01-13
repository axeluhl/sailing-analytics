package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Mile;
import com.sap.sailing.domain.base.SpeedWithConfidence;

public class KnotSpeedImpl extends AbstractSpeedWithConfidence implements SpeedWithConfidence {
    private final double knots;
    
    private final double confidence;
    
    public KnotSpeedImpl(double knots) {
        this(knots, DEFAULT_SPEED_CONFIDENCE);
    }
    
    private KnotSpeedImpl(double knots, double confidence) {
        this.knots = knots;
        this.confidence = confidence;
    }
    
    @Override
    public double getConfidence() {
        return confidence;
    }
    
    @Override
    public double getKnots() {
        return knots;
    }

    @Override
    protected KnotSpeedImpl createInstanceOfSameType(double metersPerSecond, double confidence) {
        return new KnotSpeedImpl(getKnots() * Mile.METERS_PER_SEA_MILE / 3600, confidence);
    }
}
