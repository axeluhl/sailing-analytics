package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.windestimation.model.AbstractTrainableModel;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public abstract class AbstractClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractTrainableModel<InstanceType, T> implements TrainableClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3283338628850173316L;
    private final PreprocessingConfig preprocessingConfig;

    public AbstractClassificationModel(PreprocessingConfig preprocessingConfig, T contextSpecificModelMetadata) {
        super(contextSpecificModelMetadata);
        this.preprocessingConfig = preprocessingConfig;
    }

    @Override
    public PreprocessingConfig getPreprocessingConfig() {
        return preprocessingConfig;
    }

}
