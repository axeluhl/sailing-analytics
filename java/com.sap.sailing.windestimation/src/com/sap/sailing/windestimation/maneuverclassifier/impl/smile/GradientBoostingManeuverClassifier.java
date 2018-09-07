package com.sap.sailing.windestimation.maneuverclassifier.impl.smile;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig.PreprocessingConfigBuilder;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;

import smile.classification.GradientTreeBoost;

public class GradientBoostingManeuverClassifier extends AbstractSmileManeuverClassifier<GradientTreeBoost> {

    private static final long serialVersionUID = -3364152319152090775L;
    
    public GradientBoostingManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected GradientTreeBoost createTrainedClassifier(double[][] x, int[] y) {
        return new GradientTreeBoost(x, y, 50);
    }

}
