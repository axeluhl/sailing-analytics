package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.confidence.HasConfidence;

public abstract class HasConfidenceImpl<ValueType, BaseType, RelativeTo> implements HasConfidence<ValueType, BaseType, RelativeTo> {
    private final double confidence;
    private final RelativeTo relativeTo;
    private final BaseType object;
    
    public HasConfidenceImpl(BaseType object, double confidence, RelativeTo relativeTo) {
        this.confidence = confidence;
        this.relativeTo = relativeTo;
        this.object = object;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }
    
    @Override
    public RelativeTo getRelativeTo() {
        return relativeTo;
    }
    
    @Override
    public BaseType getObject() {
        return object;
    }

}
