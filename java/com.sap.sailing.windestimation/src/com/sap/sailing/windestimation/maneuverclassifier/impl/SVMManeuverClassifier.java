package com.sap.sailing.windestimation.maneuverclassifier.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForInternalClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.GaussianKernel;

public class SVMManeuverClassifier extends AbstractSmileManeuverClassifier<SVM<double[]>> {

    private static final long serialVersionUID = -3364152319152090775L;

    public SVMManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForInternalClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected SVM<double[]> createTrainedClassifier(double[][] x, int[] y) {
        int featuresCount = x[0].length;
        double gamma = 1.0 / featuresCount;
        double sigma = Math.sqrt(0.5 / gamma);
        SVM<double[]> svm = new SVM<>(new GaussianKernel(sigma), 10.0, getSupportedManeuverTypesCount(),
                Multiclass.ONE_VS_ALL);
        svm.learn(x, y);
        svm.finish();
        svm.trainPlattScaling(x, y);
        return svm;
    }

    @Override
    public boolean hasSupportForProvidedFeatures() {
        return getBoatClass() != null;
    }

}
