package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.PersistableModel;

public interface TrainableModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends PersistableModel<InstanceType, T> {

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    long getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, long numberOfTrainingInstances);
}
