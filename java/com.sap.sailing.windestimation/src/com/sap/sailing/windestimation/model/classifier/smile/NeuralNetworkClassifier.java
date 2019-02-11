package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NeuralNetwork;

public class NeuralNetworkClassifier<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;
    private static final int EPOCHS = 20;

    public NeuralNetworkClassifier(T modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected NeuralNetwork trainInternalModel(double[][] x, int[] y) {
        int k = getModelContext().getNumberOfPossibleTargetValues();
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
        int k = getModelContext().getNumberOfPossibleTargetValues();
        if (k != 2) {
            return super.classifyWithProbabilities(x);
        }
        x = preprocessX(x);
        double[] likelihoods = new double[getModelContext().getNumberOfPossibleTargetValues()];
        double[] likelihoodsInternal = new double[1];
        internalModel.predict(x, likelihoodsInternal);
        likelihoods[0] = 1 - likelihoodsInternal[0];
        likelihoods[1] = likelihoodsInternal[0];
        return likelihoods;
    }

}
