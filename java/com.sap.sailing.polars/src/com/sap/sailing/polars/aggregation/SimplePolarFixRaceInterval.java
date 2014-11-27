package com.sap.sailing.polars.aggregation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class SimplePolarFixRaceInterval implements PolarFixRaceInterval {


    private final Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> intervalMap;

    public SimplePolarFixRaceInterval(Set<TrackedRace> races) {
        intervalMap = new HashMap<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>>();
        for (TrackedRace race : races) {
            intervalMap.put(race, null);
        }
    }

    @Override
    public Map<TrackedRace, Map<Competitor, Pair<TimePoint, TimePoint>>> getCompetitorAndTimepointsForRace() {
        return intervalMap;
    }

}
