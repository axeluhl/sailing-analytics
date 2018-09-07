package com.sap.sailing.windestimation.maneuvergraph.pointofsail;

import java.util.List;
import java.util.stream.Collectors;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.evaluation.TargetWindFixesExtractor;

public class TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor
        implements TargetWindFixesExtractor<CompleteManeuverCurveWithEstimationData> {

    @Override
    public List<Wind> extractTargetWindFixes(
            CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrack) {
        return competitorTrack.getElements().stream().map(maneuver -> maneuver.getWind()).collect(Collectors.toList());
    }

}
