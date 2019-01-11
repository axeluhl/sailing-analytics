package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NeuralNetwork;

public class NeuralNetworkClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;
    private static final int EPOCHS = 20;

    public NeuralNetworkClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected NeuralNetwork trainInternalModel(double[][] x, int[] y) {
        int k = getContextSpecificModelMetadata().getNumberOfPossibleTargetValues();
        int numberOfInputFeatures = x[0].length;
        NeuralNetwork net;
        int outputUnits = k == 2 ? 1 : k;
        int[] numUnits = new int[] { numberOfInputFeatures, 100, 100, outputUnits };
        if (k == 2) {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY,
                    NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID, numUnits);
        } else {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX,
                    numUnits);
        }

        for (int i = 0; i < EPOCHS; i++) {
            net.learn(x, y);
        }
        return net;
    }

    @Override
    public double[] classifyWithProbabilities(double[] x) {
        int k = getContextSpecificModelMetadata().getNumberOfPossibleTargetValues();
        if (k != 2) {
            return super.classifyWithProbabilities(x);
        }
        x = preprocessX(x);
        double[] likelihoods = new double[getContextSpecificModelMetadata().getNumberOfPossibleTargetValues()];
        double[] likelihoodsInternal = new double[1];
        internalModel.predict(x, likelihoodsInternal);
        likelihoods[0] = 1 - likelihoodsInternal[0];
        likelihoods[1] = likelihoodsInternal[0];
        return likelihoods;
    }

}
