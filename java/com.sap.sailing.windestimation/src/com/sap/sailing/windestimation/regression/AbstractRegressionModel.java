package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;
import com.sap.sailing.windestimation.model.store.ContextType;

public abstract class AbstractRegressionModel<InstanceType> extends AbstractTrainableModel
        implements TrainableRegressionModel<InstanceType> {

    private static final long serialVersionUID = -3283338628316L;
    private final ContextType contextType;

    public AbstractRegressionModel(ContextType contextType) {
        this.contextType = contextType;
    }

    @Override
    public ContextType getContextType() {
        return contextType;
    }

}
