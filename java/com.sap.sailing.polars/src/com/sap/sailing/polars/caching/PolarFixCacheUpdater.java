package com.sap.sailing.polars.caching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.util.SmartFutureCache.CacheUpdater;

public class PolarFixCacheUpdater implements
        CacheUpdater<BoatClass, Map<RegattaAndRaceIdentifier, List<PolarFix>>, PolarFixCacheRaceInterval> {

    private final Executor executor;

    public PolarFixCacheUpdater(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFix>> computeCacheUpdate(BoatClass key,
            PolarFixCacheRaceInterval updateInterval) throws Exception {
        PolarFixAggregator aggregator = new PolarFixAggregator(updateInterval,
                PolarSheetGenerationSettingsImpl.createStandardPolarSettings(), executor);
        Thread.sleep(updateInterval.getCompetitorAndTimepointsForRace().keySet().iterator().next()
                .getMillisecondsOverWhichToAverageSpeed() / 2);
        aggregator.startPolarFixAggregation();

        return aggregator.get();
    }

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFix>> provideNewCacheValue(BoatClass key,
            Map<RegattaAndRaceIdentifier, List<PolarFix>> oldCacheValue,
            Map<RegattaAndRaceIdentifier, List<PolarFix>> computedCacheUpdate, PolarFixCacheRaceInterval updateInterval) {
        Map<RegattaAndRaceIdentifier, List<PolarFix>> newCacheValue = new HashMap<RegattaAndRaceIdentifier, List<PolarFix>>();
        if (oldCacheValue != null) {
            newCacheValue.putAll(oldCacheValue);
        }
        for (Entry<RegattaAndRaceIdentifier, List<PolarFix>> newEntry : computedCacheUpdate.entrySet()) {
            List<PolarFix> oldList = newCacheValue.get(newEntry.getKey());
            if (oldList != null) {
                oldList.addAll(newEntry.getValue());
            } else {
                newCacheValue.put(newEntry.getKey(), newEntry.getValue());
            }
        }
        return newCacheValue;
    }

}
