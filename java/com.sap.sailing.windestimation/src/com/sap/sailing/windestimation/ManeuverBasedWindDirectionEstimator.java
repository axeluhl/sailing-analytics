package com.sap.sailing.windestimation;

import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.windestimation.impl.WindDirectionCandidatesForTimePoint;

public interface ManeuverBasedWindDirectionEstimator {

    Iterable<WindDirectionCandidatesForTimePoint> computeWindDirectionCandidates(Iterable<Maneuver> competitorManeuvers);

}
