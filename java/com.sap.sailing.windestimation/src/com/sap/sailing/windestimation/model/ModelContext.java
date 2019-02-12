package com.sap.sailing.windestimation.model;

import java.io.Serializable;

import com.sap.sailing.windestimation.model.store.ModelDomainType;

public abstract class ModelContext<InstanceType> implements Serializable, FeatureExtraction<InstanceType> {

    private static final long serialVersionUID = 5069029031816423989L;
    private final ModelDomainType domainType;

    public ModelContext(ModelDomainType domainType) {
        this.domainType = domainType;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public abstract int getNumberOfPossibleTargetValues();

    public abstract String getId();

    public ModelDomainType getDomainType() {
        return domainType;
    }

}