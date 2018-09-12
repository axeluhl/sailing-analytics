package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.List;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public interface TrainableSingleManeuverOfflineClassifier extends ProbabilisticManeuverClassifier, PersistableModel {

    void trainWithManeuvers(List<ManeuverForEstimation> maneuvers);

    void setTestScore(double testScore);

    double getTrainScore();

    void setTrainScore(double trainScore);
    
    void setFixesCountForBoatClass(int fixesCountForBoatClass);

}
