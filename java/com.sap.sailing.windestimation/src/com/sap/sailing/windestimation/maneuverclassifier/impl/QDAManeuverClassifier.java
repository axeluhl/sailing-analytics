package com.sap.sailing.windestimation.maneuverclassifier.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.QDA;

public class QDAManeuverClassifier extends AbstractSmileManeuverClassifier<QDA> {

    private static final long serialVersionUID = -3364152319152090775L;
    
    public QDAManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected QDA createTrainedClassifier(double[][] x, int[] y) {
        return new QDA(x, y);
    }

}
