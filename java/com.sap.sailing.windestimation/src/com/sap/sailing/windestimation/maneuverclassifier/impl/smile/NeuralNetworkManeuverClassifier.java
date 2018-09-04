package com.sap.sailing.windestimation.maneuverclassifier.impl.smile;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.impl.MLUtil;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NeuralNetwork;

public class NeuralNetworkManeuverClassifier extends AbstractSmileManeuverClassifier<NeuralNetwork> {

    public NeuralNetworkManeuverClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass,
            ManeuverTypeForClassification[] supportedManeuverTypes) {
        super(maneuverFeatures, boatClass, new PreprocessingConfigBuilder().scaling().build(), supportedManeuverTypes);
    }

    @Override
    protected NeuralNetwork createTrainedClassifier(double[][] x, int[] y) {
        int k = getSupportedManeuverTypesCount();
        int numberOfInputFeatures = MLUtil.getNumberOfInputFeatures(getManeuverFeatures());
        NeuralNetwork net;
        if (k == 2) {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY,
                    NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID, numberOfInputFeatures, 100, 100, 1);
        } else {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX,
                    100, 100, k);
        }

        for (int i = 0; i < 20; i++) {
            net.learn(x, y);
        }
        return net;
    }

}
