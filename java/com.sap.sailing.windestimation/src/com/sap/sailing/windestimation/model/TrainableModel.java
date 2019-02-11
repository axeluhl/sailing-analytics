package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.PersistableModel;

/**
 * Machine Learning model which is 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 * @param <MC>
 */
public interface TrainableModel<InstanceType, MC extends ModelContext<InstanceType>>
        extends PersistableModel<InstanceType, MC> {

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    long getNumberOfTrainingInstances();

    void setTrainingStats(double trainScore, double testScore, long numberOfTrainingInstances);
}
