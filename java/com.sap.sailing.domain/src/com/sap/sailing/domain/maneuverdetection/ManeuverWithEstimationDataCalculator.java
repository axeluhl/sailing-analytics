package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Constructs {@link ManeuverWithEstimationData} from {@link Maneuver}.
 * 
 * @author Vladislav Chumak (D069712)
 * @see ManeuverWithEstimationData
 *
 */
public interface ManeuverWithEstimationDataCalculator {

    /**
     * Computes estimation data for the provided maneuvers.
     */
    Iterable<ManeuverWithEstimationData> computeEstimationDataForManeuvers(TrackedRace trackedRace,
            Competitor competitor, Iterable<Maneuver> maneuvers, boolean avgSpeedAndCogCalculationBeforeAndAfterManeuver);

}
