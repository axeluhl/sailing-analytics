package com.sap.sailing.windestimation.maneuverclassifier.impl.smile;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.SVM;
import smile.math.kernel.GaussianKernel;

public class SVMManeuverClassifier extends AbstractSmileManeuverClassifier<SVM<double[]>> {

    public SVMManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected SVM<double[]> createTrainedClassifier(double[][] x, int[] y) {
        int featuresCount = x[0].length;
        double gamma = 1.0 / featuresCount;
        double sigma = Math.sqrt(0.5 / gamma);
        SVM<double[]> svm = new SVM<>(new GaussianKernel(sigma), 10.0);
        svm.learn(x, y);
        svm.finish();
        return svm;
    }

}
