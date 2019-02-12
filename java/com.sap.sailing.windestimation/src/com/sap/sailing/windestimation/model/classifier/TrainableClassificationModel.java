package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.FeatureExtraction;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;

/**
 * Specialization of {@link TrainableModel} which is used in context of classification models producing prediction for a
 * label/categorical value.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public interface TrainableClassificationModel<InstanceType, MC extends ModelContext<InstanceType>>
        extends TrainableModel<InstanceType, MC> {

    /**
     * Performs supervised training of this model from scratch using the provided labeled training data. Old model state
     * is destroyed. After training, the model becomes a state which should be persisted for later use. x[i] must be the
     * feature vector which is labeled as y[i]. The execution of this method can take a while.
     * 
     * @param x
     *            Two-dimensional array [n][m] where n is the number of instances and m is the number of input features
     *            provided by each instance.
     * @param y
     *            Single-dimensional array [n] where n is the number of instances.
     * @see FeatureExtraction
     * @see #setTrainingStats(double, double, long)
     */
    void train(double[][] x, int[] y);

    /**
     * Classifies the provided input features and gets the array with likelihoods where each likelihood represents the
     * probability of the instance with the provided features being of the category type referred by likelihood's index.
     * The likelihoods must sum up to 1.
     * 
     * @param x
     *            The provided input features
     * @return Single-dimensional array with elements between 0.0 to 1.0 each representing the likelihood for a category
     *         y where y is the elements index.
     */
    double[] classifyWithProbabilities(double[] x);

    /**
     * Classifies the provided instance considering its input features and gets the array with likelihoods where each
     * likelihood represents the probability of the provided instance being of the category type referred by
     * likelihood's index. All returned likelihoods must sum up to 1.
     * 
     * @param instance
     *            The provided instance with the input features
     * @return Single-dimensional array with elements between 0.0 to 1.0 each representing the likelihood for a category
     *         y where y is the elements index.
     */
    default double[] classifyWithProbabilities(InstanceType instance) {
        double[] x = getModelContext().getX(instance);
        return classifyWithProbabilities(x);
    }

    /**
     * Gets pre-processing configuration for this model implementation.
     */
    PreprocessingConfig getPreprocessingConfig();

}
