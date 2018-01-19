package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.windestimation.ManeuverBasedWindDirectionEstimator;

public class CompetitorManeuverBasedWindDirectionEstimator implements ManeuverBasedWindDirectionEstimator {

    @Override
    public Iterable<WindDirectionCandidatesForManeuver> computeWindDirectionCandidates(Iterable<Maneuver> competitorManeuvers) {
        return null;
    }

}
