package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.PersistableModel;

/**
 * Machine Learning model which is used within wind estimation for classification/regression purposes. Implementation
 * must be thread-safe.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 * @see AbstractTrainableModel
 */
public interface TrainableModel<InstanceType, MC extends ModelContext<InstanceType>>
        extends PersistableModel<InstanceType, MC> {

    /**
     * Gets the test score of this model which was derived by evaluating this model on new unseen test instances after
     * the training of this model has finished.
     * 
     * @return double between 0 and 1 representing macro-averaged F2-Score
     */
    double getTestScore();

    /**
     * Gets the training score of this model which was derived by evaluating this model on the same instances as this
     * model was trained on.
     * 
     * @return double between 0 and 1 representing macro-averaged F2-Score
     */
    double getTrainScore();

    /**
     * Checks whether this model is ready for prediction tasks. This is usually the case when the model has been loaded
     * with its persistent state which represents its training result.
     */
    boolean isModelReady();

    /**
     * Gets the number of training instances which were used to train this model.
     */
    long getNumberOfTrainingInstances();

    /**
     * Sets the training statistics for this model. This method must be called only when model training is performed.
     * 
     * @param trainScore
     *            Macro-averaged F2-Score with training data (see {@link #getTrainScore()})
     * @param testScore
     *            Macro-averaged F2-Score with test data (see {@link #getTestScore()}
     * @param numberOfTrainingInstances
     *            The number of instances which was used during model training
     */
    void setTrainingStats(double trainScore, double testScore, long numberOfTrainingInstances);
}
