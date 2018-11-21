package com.sap.sailing.windestimation.classifier.smile;

import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NaiveBayes;

public class NaiveBayesManeuverClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileManeuverClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public NaiveBayesManeuverClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected NaiveBayes trainInternalModel(double[][] x, int[] y) {
        int numberOfInputFeatures = getModelMetadata().getContextSpecificModelMetadata().getNumberOfInputFeatures();
        int numberOfClasses = getModelMetadata().getContextSpecificModelMetadata().getNumberOfPossibleTargetValues();
        NaiveBayes nbc = new NaiveBayes(NaiveBayes.Model.MULTINOMIAL, numberOfClasses, numberOfInputFeatures);
        nbc.learn(x, y);
        return nbc;
    }

}
