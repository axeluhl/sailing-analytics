package com.sap.sailing.windestimation.maneuverclassifier.impl.smile;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.LogisticRegression;

public class LogisticRegressionManeuverClassifier extends AbstractSmileManeuverClassifier<LogisticRegression> {

    public LogisticRegressionManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().build(), supportedManeuverTypes);
    }

    @Override
    protected LogisticRegression createTrainedClassifier(double[][] x, int[] y) {
        return new LogisticRegression(x, y);
    }

}
