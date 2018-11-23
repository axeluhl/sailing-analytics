package com.sap.sailing.windestimation.classifier;

public abstract class AbstractClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        implements TrainableClassificationModel<InstanceType, T> {

    private final ModelMetadata<InstanceType, T> modelMetadata;
    private boolean trainingFinished = false;
    private double testScore = 0;
    private double trainScore = 0;
    private int numberOfTrainingInstances;

    public AbstractClassificationModel(ModelMetadata<InstanceType, T> modelMetadata) {
        this.modelMetadata = modelMetadata;
    }

    @Override
    public ModelMetadata<InstanceType, T> getModelMetadata() {
        return modelMetadata;
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

}
