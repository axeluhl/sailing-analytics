package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public interface ManeuverClassificationsAggregator {

    List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications);

}
