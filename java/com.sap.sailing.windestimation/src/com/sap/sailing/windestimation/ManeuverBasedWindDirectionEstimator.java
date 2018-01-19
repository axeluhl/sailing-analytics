package com.sap.sailing.windestimation;

import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForManeuver;

public interface ManeuverBasedWindDirectionEstimator {

    Iterable<WindDirectionCandidatesForManeuver> computeWindDirectionCandidates(Iterable<Maneuver> competitorManeuvers);

}
