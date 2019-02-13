package com.sap.sailing.windestimation.aggregator;

import java.util.List;

import com.sap.sailing.windestimation.data.ManeuverWithEstimatedType;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;

/**
 * Aggregates maneuver classifications so that it allows conclusion about a plausible non-bumping wind track.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverClassificationsAggregator {

    List<ManeuverWithEstimatedType> aggregateManeuverClassifications(
            RaceWithEstimationData<ManeuverWithProbabilisticTypeClassification> raceWithManeuverClassifications);

}
