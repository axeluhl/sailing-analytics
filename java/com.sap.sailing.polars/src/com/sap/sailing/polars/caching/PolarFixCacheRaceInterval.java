package com.sap.sailing.polars.caching;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;

public class PolarFixCacheRaceInterval implements UpdateInterval<PolarFixCacheRaceInterval> {

    private final Set<TrackedRace> races;

    public PolarFixCacheRaceInterval(Set<TrackedRace> races) {
        this.races = races;
    }

    public Set<TrackedRace> getRaces() {
        return races;
    }

    @Override
    public PolarFixCacheRaceInterval join(PolarFixCacheRaceInterval otherUpdateInterval) {
        Set<TrackedRace> joined = new HashSet<TrackedRace>();
        joined.addAll(races);
        joined.addAll(otherUpdateInterval.getRaces());
        return new PolarFixCacheRaceInterval(joined);
    }

}
