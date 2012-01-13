package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.confidence.ScalableValue;

public abstract class AbstractSpeedWithConfidence extends AbstractSpeedImpl implements SpeedWithConfidence,
        ScalableValue<SpeedWithConfidence, SpeedWithConfidence> {
    protected static final double DEFAULT_SPEED_CONFIDENCE = 0.9;

    protected abstract AbstractSpeedWithConfidence createInstanceOfSameType(double metersPerSecond, double confidence);
    
    @Override
    public double getConfidence() {
        return DEFAULT_SPEED_CONFIDENCE;
    }

    @Override
    public ScalableValue<SpeedWithConfidence, SpeedWithConfidence> multiply(double factor) {
        return createInstanceOfSameType(factor*getMetersPerSecond(), getConfidence());
    }

    @Override
    public AbstractSpeedWithConfidence add(ScalableValue<SpeedWithConfidence, SpeedWithConfidence> t) {
        return createInstanceOfSameType(getMetersPerSecond() + t.getValue().getMetersPerSecond(), (getConfidence() + t
                .getValue().getConfidence()) / 2);
    }

    @Override
    public SpeedWithConfidence divide(double divisor) {
        return createInstanceOfSameType(getMetersPerSecond()/divisor, getConfidence());
    }

    @Override
    public SpeedWithConfidence getValue() {
        return this;
    }

    @Override
    public ScalableValue<SpeedWithConfidence, SpeedWithConfidence> getScalableValue() {
        return this;
    }

}
