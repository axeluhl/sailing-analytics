package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NaiveBayes;

public class NaiveBayesClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public NaiveBayesClassifier(T modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected NaiveBayes trainInternalModel(double[][] x, int[] y) {
        int numberOfInputFeatures = getModelContext().getNumberOfInputFeatures();
        int numberOfClasses = getModelContext().getNumberOfPossibleTargetValues();
        NaiveBayes nbc = new NaiveBayes(NaiveBayes.Model.MULTINOMIAL, numberOfClasses, numberOfInputFeatures);
        nbc.learn(x, y);
        return nbc;
    }

}
