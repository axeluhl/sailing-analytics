package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.PersistenceSupport;
import com.sap.sailing.windestimation.model.store.SerializationBasedPersistenceSupport;

public abstract class AbstractRegressionModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractTrainableModel<InstanceType, T> implements TrainableRegressionModel<InstanceType, T> {

    private static final long serialVersionUID = -3283338628316L;

    public AbstractRegressionModel(T contextSpecificModelMetadata) {
        super(contextSpecificModelMetadata);
    }

    @Override
    public PersistenceSupport getPersistenceSupport() {
        return new SerializationBasedPersistenceSupport(this);
    }

}
