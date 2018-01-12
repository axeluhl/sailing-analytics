package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverWithEstimationDataCalculator {
    
    Iterable<ManeuverWithEstimationData> complementManeuversWithEstimationData(TrackedRace trackedRace, Competitor competitor, Iterable<Maneuver> maneuvers);

}
