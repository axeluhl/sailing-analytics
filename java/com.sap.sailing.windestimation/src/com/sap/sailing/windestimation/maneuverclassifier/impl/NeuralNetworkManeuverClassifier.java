package com.sap.sailing.windestimation.maneuverclassifier.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.MLUtil;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NeuralNetwork;

public class NeuralNetworkManeuverClassifier extends AbstractSmileManeuverClassifier<NeuralNetwork> {

    private static final long serialVersionUID = -3364152319152090775L;

    public NeuralNetworkManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected NeuralNetwork createTrainedClassifier(double[][] x, int[] y) {
        int k = getSupportedManeuverTypesCount();
        int numberOfInputFeatures = MLUtil.getNumberOfInputFeatures(getManeuverFeatures());
        NeuralNetwork net;
        int outputUnits = k == 2 ? 1 : k;
        int[] numUnits = getBoatClass() == null ? new int[] { numberOfInputFeatures, 100, 100, outputUnits }
                : new int[] { numberOfInputFeatures, 100, 100, outputUnits };
        if (k == 2) {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY,
                    NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID, numUnits);
        } else {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX,
                    numUnits);
        }

        for (int i = 0; i < 20; i++) {
            net.learn(x, y);
        }
        return net;
    }

}
