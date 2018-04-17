package com.sap.sailing.windestimation;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.impl.WindTrackCandidate;

public interface ManeuverBasedWindDirectionEstimator {

    Iterable<WindTrackCandidate> computeWindTrackCandidates(BoatClass boatClass,
            Iterable<CompleteManeuverCurveWithEstimationData> competitorManeuvers);

}
