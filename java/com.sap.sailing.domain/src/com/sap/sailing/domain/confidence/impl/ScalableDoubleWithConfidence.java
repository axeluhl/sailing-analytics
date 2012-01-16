package com.sap.sailing.domain.confidence.impl;

import com.sap.sailing.domain.confidence.HasConfidence;

public class ScalableDoubleWithConfidence extends ScalableDouble implements HasConfidence<Double, Double> {
    private final double confidence;
    
    public ScalableDoubleWithConfidence(double d, double confidence) {
        super(d);
        this.confidence = confidence;
    }
    
    @Override
    public double getConfidence() {
        return confidence;
    }

    @Override
    public ScalableDouble getScalableValue() {
        return this;
    }
}
