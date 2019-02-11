package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.RandomForest;

public class RandomForestClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public RandomForestClassifier(T modelContext) {
        super(new PreprocessingConfigBuilder().build(), modelContext);
    }

    @Override
    protected RandomForest trainInternalModel(double[][] x, int[] y) {
        return new RandomForest(x, y, 50, getModelContext().getNumberOfInputFeatures());
    }

}
