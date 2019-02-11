package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.LDA;

public class LDAClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public LDAClassifier(T modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected LDA trainInternalModel(double[][] x, int[] y) {
        return new LDA(x, y);
    }

}
