package com.sap.sailing.polars.caching;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;

public class PolarFixCacheRaceInterval implements UpdateInterval<PolarFixCacheRaceInterval> {

    private final Set<RegattaAndRaceIdentifier> races;

    public PolarFixCacheRaceInterval(Set<RegattaAndRaceIdentifier> races) {
        this.races = races;
    }

    public Set<RegattaAndRaceIdentifier> getRaces() {
        return races;
    }

    @Override
    public PolarFixCacheRaceInterval join(PolarFixCacheRaceInterval otherUpdateInterval) {
        Set<RegattaAndRaceIdentifier> joined = new HashSet<RegattaAndRaceIdentifier>();
        for (RegattaAndRaceIdentifier race : races) {
            joined.add(race);
        }
        for (RegattaAndRaceIdentifier race : otherUpdateInterval.getRaces()) {
            joined.add(race);
        }
        return new PolarFixCacheRaceInterval(joined);
    }

}
