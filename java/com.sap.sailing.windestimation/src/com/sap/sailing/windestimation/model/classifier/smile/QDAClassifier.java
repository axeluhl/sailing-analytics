package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.QDA;

public class QDAClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public QDAClassifier(T modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected QDA trainInternalModel(double[][] x, int[] y) {
        return new QDA(x, y);
    }

}
