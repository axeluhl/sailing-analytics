package com.sap.sailing.polars.aggregation;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util.Pair;

public interface PolarFixRaceInterval {

    Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> getCompetitorAndTimepointsForRace();

}
