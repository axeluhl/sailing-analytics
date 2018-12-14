package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.PersistenceSupport;
import com.sap.sailing.windestimation.model.store.SerializationBasedPersistenceSupport;

public abstract class AbstractRegressorModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractTrainableModel<InstanceType, T> implements TrainableRegressorModel<InstanceType, T> {

    private static final long serialVersionUID = -3283338628316L;

    public AbstractRegressorModel(T contextSpecificModelMetadata) {
        super(contextSpecificModelMetadata);
    }

    @Override
    public PersistenceSupport getPersistenceSupport() {
        return new SerializationBasedPersistenceSupport(this);
    }

}
