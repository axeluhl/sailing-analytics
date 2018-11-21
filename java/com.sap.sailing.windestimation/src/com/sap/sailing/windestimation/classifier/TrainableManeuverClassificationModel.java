package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.windestimation.classifier.store.PersistenceSupport;

public interface TrainableManeuverClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ClassificationModel<InstanceType, T> {

    void train(double[][] x, int[] y);

    int getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances);

    PersistenceSupport getPersistenceSupport();

}
