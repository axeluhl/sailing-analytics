package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.windestimation.model.store.ContextType;
import com.sap.sailing.windestimation.model.store.PersistableModel;

public interface TrainableClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ClassificationModel<InstanceType, T>, PersistableModel {

    void train(double[][] x, int[] y);

    @Override
    default ContextType getContextType() {
        return getModelMetadata().getContextSpecificModelMetadata().getContextType();
    }

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    int getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, int numberOfTrainingInstances);

}
