package com.sap.sailing.windestimation.model;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public abstract class ModelContext<InstanceType>
        implements Serializable, FeatureExtraction<InstanceType> {

    private static final long serialVersionUID = 5069029031816423989L;
    private final PersistenceContextType contextType;

    public ModelContext(PersistenceContextType contextType) {
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

    public PersistenceContextType getContextType() {
        return contextType;
    }

}