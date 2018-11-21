package com.sap.sailing.windestimation.classifier;

import java.io.Serializable;

public abstract class ContextSpecificModelMetadata<InstanceType>
        implements Serializable, FeatureExtraction<InstanceType> {

    private static final long serialVersionUID = 5069029031816423989L;

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public abstract int getNumberOfPossibleTargetValues();

    public abstract String getId();

}