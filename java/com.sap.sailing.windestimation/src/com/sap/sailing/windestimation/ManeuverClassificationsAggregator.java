package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverWithEstimatedType;

public interface ManeuverClassificationsAggregator {

    List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications);

}
