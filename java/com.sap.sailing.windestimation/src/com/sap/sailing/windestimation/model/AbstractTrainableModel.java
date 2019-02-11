package com.sap.sailing.windestimation.model;

public abstract class AbstractTrainableModel<InstanceType, T extends ModelContext<InstanceType>>
        implements TrainableModel<InstanceType, T> {

    private static final long serialVersionUID = 6483063459903792768L;
    private final T modelContext;
    private boolean trainingFinished = false;
    private double testScore = 0;
    private double trainScore = 0;
    private long numberOfTrainingInstances;

    public AbstractTrainableModel(T modelContext) {
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
    public void setTrainingStats(double trainScore, double testScore, long numberOfTrainingInstances) {
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
    public T getModelContext() {
        return modelContext;
    }

}
