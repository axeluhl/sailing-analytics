package com.sap.sailing.windestimation.data;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;

public interface ManeuverAndPolarsBasedWindEstimator {

    List<WindWithConfidence<TimePoint>> estimateWind(Iterable<CompetitorTrackWithEstimationData> competitorTracks);

}
