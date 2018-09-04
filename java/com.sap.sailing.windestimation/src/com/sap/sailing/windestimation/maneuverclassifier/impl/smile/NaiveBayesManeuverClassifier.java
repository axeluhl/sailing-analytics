package com.sap.sailing.windestimation.maneuverclassifier.impl.smile;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.impl.MLUtil;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NaiveBayes;

public class NaiveBayesManeuverClassifier extends AbstractSmileManeuverClassifier<NaiveBayes> {

    public NaiveBayesManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().build(), supportedManeuverTypes);
    }

    @Override
    protected NaiveBayes createTrainedClassifier(double[][] x, int[] y) {
        int numberOfInputFeatures = MLUtil.getNumberOfInputFeatures(getManeuverFeatures());
        int numberOfClasses = getSupportedManeuverTypesCount();
        NaiveBayes nbc = new NaiveBayes(NaiveBayes.Model.GENERAL, numberOfClasses, numberOfInputFeatures);
        nbc.learn(x, y);
        return nbc;
    }

}
