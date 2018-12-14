package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.PersistableModel;

public interface TrainableModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends PersistableModel<InstanceType, T> {

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    int getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances);
}
