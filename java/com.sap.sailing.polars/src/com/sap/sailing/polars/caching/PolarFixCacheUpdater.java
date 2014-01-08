package com.sap.sailing.polars.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
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
        Set<TrackedRace> races = updateInterval.getRaces();
        Map<RegattaAndRaceIdentifier, PolarFixAggregator> aggregators = new HashMap<RegattaAndRaceIdentifier, PolarFixAggregator>();
        for (TrackedRace trackedRace : races) {
            Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
            trackedRaces.add(trackedRace);
            PolarFixAggregator aggregator = new PolarFixAggregator(trackedRaces,
                    PolarSheetGenerationSettingsImpl.createStandardPolarSettings(), executor);
            aggregator.startPolarFixAggregation();
            aggregators.put(trackedRace.getRaceIdentifier(), aggregator);
        }
        Map<RegattaAndRaceIdentifier, List<PolarFix>> resultMap = new HashMap<RegattaAndRaceIdentifier, List<PolarFix>>();
        for (Entry<RegattaAndRaceIdentifier, PolarFixAggregator> entry : aggregators.entrySet()) {
            PolarFixAggregator aggregator = entry.getValue();
            Set<PolarFix> result = aggregator.get();
            List<PolarFix> resultList = new ArrayList<PolarFix>(result);
            resultMap.put(entry.getKey(), resultList);
        }
        return resultMap;

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
            newCacheValue.put(newEntry.getKey(), newEntry.getValue());
        }
        return newCacheValue;
    }

}
