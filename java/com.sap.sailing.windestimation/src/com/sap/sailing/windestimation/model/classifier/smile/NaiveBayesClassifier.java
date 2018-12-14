package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NaiveBayes;

public class NaiveBayesClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public NaiveBayesClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected NaiveBayes trainInternalModel(double[][] x, int[] y) {
        int numberOfInputFeatures = getContextSpecificModelMetadata().getNumberOfInputFeatures();
        int numberOfClasses = getContextSpecificModelMetadata().getNumberOfPossibleTargetValues();
        NaiveBayes nbc = new NaiveBayes(NaiveBayes.Model.MULTINOMIAL, numberOfClasses, numberOfInputFeatures);
        nbc.learn(x, y);
        return nbc;
    }

}
