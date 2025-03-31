package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.NeuralNetwork;

/**
 * Multilayer Neural Network classifier with two hidden layers, each with 100 neurons and logistic sigmoid as it
 * activation function. The network is trained in 20 epochs.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public class NeuralNetworkClassifier<InstanceType, MC extends ModelContext<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, MC> {

    private static final int HIDDEN_LAYER_NEURONS_NUMBER = 100;
    private static final long serialVersionUID = -3364152319152090775L;
    private static final int EPOCHS = 20;

    public NeuralNetworkClassifier(MC modelContext) {
        super(new PreprocessingConfigBuilder().scaling().build(), modelContext);
    }

    @Override
    protected NeuralNetwork trainInternalModel(double[][] x, int[] y) {
        int k = getModelContext().getNumberOfPossibleTargetValues();
        int numberOfInputFeatures = x[0].length;
        NeuralNetwork net;
        int outputUnits = k == 2 ? 1 : k;
        int[] numUnits = new int[] { numberOfInputFeatures, HIDDEN_LAYER_NEURONS_NUMBER, HIDDEN_LAYER_NEURONS_NUMBER,
                outputUnits };
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
