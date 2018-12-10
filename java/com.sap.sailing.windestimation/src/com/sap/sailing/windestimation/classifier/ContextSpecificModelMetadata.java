package com.sap.sailing.windestimation.classifier;

import java.io.Serializable;

import com.sap.sailing.windestimation.classifier.store.ContextType;

public abstract class ContextSpecificModelMetadata<InstanceType>
        implements Serializable, FeatureExtraction<InstanceType> {

    private static final long serialVersionUID = 5069029031816423989L;
    private final ContextType contextType;

    public ContextSpecificModelMetadata(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public abstract int getNumberOfPossibleTargetValues();

    public abstract String getId();

    public ContextType getContextType() {
        return contextType;
    }

}