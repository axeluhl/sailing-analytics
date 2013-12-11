package com.sap.sailing.polars.caching;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.polars.data.impl.PolarFixImpl;
import com.sap.sailing.util.SmartFutureCache.CacheUpdater;

public class PolarFixCacheUpdater implements
        CacheUpdater<BoatClass, Map<RegattaAndRaceIdentifier, List<PolarFixImpl>>, PolarFixCacheRaceInterval> {

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFixImpl>> computeCacheUpdate(BoatClass key,
            PolarFixCacheRaceInterval updateInterval) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<RegattaAndRaceIdentifier, List<PolarFixImpl>> provideNewCacheValue(BoatClass key,
            Map<RegattaAndRaceIdentifier, List<PolarFixImpl>> oldCacheValue,
            Map<RegattaAndRaceIdentifier, List<PolarFixImpl>> computedCacheUpdate, PolarFixCacheRaceInterval updateInterval) {
        // TODO Auto-generated method stub
        return null;
    }

}
