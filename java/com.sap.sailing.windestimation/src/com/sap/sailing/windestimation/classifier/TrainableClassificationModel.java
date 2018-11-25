package com.sap.sailing.windestimation.classifier;

import java.io.Serializable;

import com.sap.sailing.windestimation.classifier.store.PersistenceSupport;

public interface TrainableClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ClassificationModel<InstanceType, T>, Serializable {

    void train(double[][] x, int[] y);

    int getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances);

    PersistenceSupport getPersistenceSupport();

}
