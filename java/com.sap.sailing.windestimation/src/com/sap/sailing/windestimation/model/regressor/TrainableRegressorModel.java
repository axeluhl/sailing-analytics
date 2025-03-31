package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.FeatureExtraction;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;

/**
 * Specialization of {@link TrainableModel} which is used in context of regression models producing prediction for a
 * continuous value.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public interface TrainableRegressorModel<InstanceType, MC extends ModelContext<InstanceType>>
        extends TrainableModel<InstanceType, MC> {

    /**
     * Performs supervised training of this model from scratch using the provided training data which contains target
     * values. Old model state is destroyed. After training, the model becomes a state which should be persisted for
     * later use. x[i] must be the feature vector which is associated with target value y[i]. The execution of this
     * method can take a while.
     * 
     * @param x
     *            Two-dimensional array [n][m] where n is the number of instances and m is the number of input features
     *            provided by each instance.
     * @param y
     *            Single-dimensional array [n] where n is the number of instances.
     * @see FeatureExtraction
     */
    void train(double[][] x, double[] y);

    /**
     * Predicts the target value for the provided instance considering its features.
     * 
     * @param instance
     *            The instance with features to predict the target value for
     * @return The predicted target value for the provided instance
     */
    default double getValue(InstanceType instance) {
        double[] x = getModelContext().getX(instance);
        return getValue(x);
    }

    /**
     * Predicts the target value for the provided feature vector.
     * 
     * @param x
     *            The feature vector of an instance
     * @return The predicted target value for the provided feature vector
     */
    double getValue(double[] x);

}
