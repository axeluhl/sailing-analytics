package com.sap.sailing.windestimation.maneuverclassifier.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.RandomForest;

public class RandomForestManeuverClassifier extends AbstractSmileManeuverClassifier<RandomForest> {

    private static final long serialVersionUID = -3364152319152090775L;
    
    public RandomForestManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().build(), supportedManeuverTypes);
    }

    @Override
    protected RandomForest createTrainedClassifier(double[][] x, int[] y) {
        return new RandomForest(x, y, 50);
    }

}
