package com.sap.sailing.windestimation.model;

import java.util.List;

/**
 * Extracts the relevant features for a {@link TrainableModel} input from the provided instance.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instance from which the relevant features will be extracted.
 */
public interface FeatureExtraction<InstanceType> {

    /**
     * Extracts the relevant features for machine learning model input from the provided instance and converts them into
     * a double vector.
     * 
     * @param instance
     *            The instance which contains the input features for a {@link TrainableModel}
     * @return Single-dimensional array where each element represents a double value of a relevant feature.
     */
    double[] getX(InstanceType instance);

    /**
     * Similar as {@link #getX(Object)}, but instead of a single instance, a list of instances is processed and a
     * multidimensional matrix is returned, where each row (first dimension) represents the feature vector of an
     * instance. This method is useful for model training where all training instances must be provided as matrix
     * representation.
     * 
     * @param instances
     *            The input instances containing the input features for model.
     * @return Two-dimensional array [n][m] where n is the number of instance and m is the number of input features
     *         provided by each instance.
     */
    default double[][] getXMatrix(List<? extends InstanceType> instances) {
        double[][] inputMatrix = new double[instances.size()][];
        int i = 0;
        for (InstanceType instance : instances) {
            inputMatrix[i++] = getX(instance);
        }
        return inputMatrix;
    }

    /**
     * Checks whether the provided instance contains all the input features which are required by this feature
     * extraction. E.g. if the feature extraction requires polar information from a maneuver instance, and the polar
     * information is not available in the maneuver instance, because it is set to null, then this method must return
     * {@code false}.
     */
    boolean isContainsAllFeatures(InstanceType instance);

    /**
     * Gets the size of the input vector/array returned by {@link #getX(Object)}.
     */
    int getNumberOfInputFeatures();

}
