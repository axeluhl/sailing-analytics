package com.sap.sailing.windestimation;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverAndPolarsBasedWindEstimator {

    List<WindWithConfidence<TimePoint>> estimateWind(Iterable<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracks);

}
