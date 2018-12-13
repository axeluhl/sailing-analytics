package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;

public abstract class AbstractClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractTrainableModel implements TrainableClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3283338628850173316L;
    private final ModelMetadata<InstanceType, T> modelMetadata;

    public AbstractClassificationModel(ModelMetadata<InstanceType, T> modelMetadata) {
        this.modelMetadata = modelMetadata;
    }

    @Override
    public ModelMetadata<InstanceType, T> getModelMetadata() {
        return modelMetadata;
    }

}
