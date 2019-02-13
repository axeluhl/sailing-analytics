package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.ModelSerializationStrategyType;

/**
 * Base class for {@link TrainableModel} with useful attributes which are required by all implementations.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y. For instance, to classify a maneuver type
 *            of a maneuver, a maneuver instance with features such as speed in/out, turning rate, lowest speed and etc.
 *            must be provided to maneuver classifier.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public abstract class AbstractTrainableModel<InstanceType, MC extends ModelContext<InstanceType>>
        implements TrainableModel<InstanceType, MC> {

    private static final long serialVersionUID = 6483063459903792768L;
    private final MC modelContext;
    private boolean trainingFinished = false;
    private double testScore = 0;
    private double trainScore = 0;
    private long numberOfTrainingInstances;

    public AbstractTrainableModel(MC modelContext) {
        this.modelContext = modelContext;
    }

    @Override
    public boolean isModelReady() {
        return trainingFinished;
    }

    @Override
    public double getTestScore() {
        return testScore;
    }

    @Override
    public double getTrainScore() {
        return trainScore;
    }

    @Override
    public long getNumberOfTrainingInstances() {
        return numberOfTrainingInstances;
    }

    @Override
    public void setStatsAfterSuccessfulTraining(double trainScore, double testScore, long numberOfTrainingInstances) {
        trainingFinished = true;
        this.trainScore = trainScore;
        this.testScore = testScore;
        this.numberOfTrainingInstances = numberOfTrainingInstances;
    }

    @Override
    public void resetTrainingStats() {
        trainingFinished = false;
        testScore = 0;
        trainScore = 0;
        numberOfTrainingInstances = 0;
    }

    @Override
    public MC getModelContext() {
        return modelContext;
    }

    @Override
    public ModelSerializationStrategyType getModelSerializationStrategyType() {
        return ModelSerializationStrategyType.SERIALIZATION;
    }

}
