package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ModelContext;

/**
 * Specialization of {@link TrainableRegressorModel} which supports incremental training using
 * {@link #train(double[], double)}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public interface IncrementallyTrainableRegressorModel<InstanceType, T extends ModelContext<InstanceType>>
        extends TrainableRegressorModel<InstanceType, T> {

    /**
     * Trains the model with the provided feature vector and its corresponding target value. The old training state will
     * be enhanced/incremented by this training step.
     * 
     * @param x
     *            The feature vector
     * @param y
     *            The target value corresponding to the provided feature vector.
     */
    void train(double[] x, double y);

    @Override
    default void train(double[][] x, double[] y) {
        for (int i = 0; i < y.length; i++) {
            double[] inputs = x[i];
            double target = y[i];
            train(inputs, target);
        }
    }

}
