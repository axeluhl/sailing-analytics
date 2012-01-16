package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.confidence.HasConfidence;

public abstract class HasConfidenceImpl<ValueType, AveragesTo> implements HasConfidence<ValueType, AveragesTo> {
    private final double confidence;
    
    public HasConfidenceImpl(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }
}
