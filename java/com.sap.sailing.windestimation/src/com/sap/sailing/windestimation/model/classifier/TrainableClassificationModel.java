package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface TrainableClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ClassificationModel<InstanceType, T>, TrainableModel<InstanceType, T> {

    void train(double[][] x, int[] y);

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    int getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances);

}
