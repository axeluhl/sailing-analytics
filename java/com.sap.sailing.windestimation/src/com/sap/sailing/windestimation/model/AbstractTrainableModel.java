package com.sap.sailing.windestimation.model;

public abstract class AbstractTrainableModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        implements TrainableModel<InstanceType, T> {

    private static final long serialVersionUID = 6483063459903792768L;
    private final T contextSpecificModelMetadata;
    private boolean trainingFinished = false;
    private double testScore = 0;
    private double trainScore = 0;
    private int numberOfTrainingInstances;

    public AbstractTrainableModel(T contextSpecificModelMetadata) {
        this.contextSpecificModelMetadata = contextSpecificModelMetadata;
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
    public int getNumberOfTrainingInstances() {
        return numberOfTrainingInstances;
    }

    @Override
    public void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances) {
        this.trainScore = trainScore;
        this.testScore = testScore;
        this.numberOfTrainingInstances = numberOfTrainingInstances;
    }

    @Override
    public boolean hasSupportForProvidedFeatures() {
        return true;
    }

    protected void trainingStarted() {
        trainingFinished = false;
        testScore = 0;
        trainScore = 0;
    }

    protected void trainingFinishedSuccessfully() {
        trainingFinished = true;
    }

    @Override
    public T getContextSpecificModelMetadata() {
        return contextSpecificModelMetadata;
    }

}
