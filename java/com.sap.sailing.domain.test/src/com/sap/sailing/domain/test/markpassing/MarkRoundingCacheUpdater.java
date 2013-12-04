package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.util.SmartFutureCache;
import com.sap.sailing.util.SmartFutureCache.AbstractCacheUpdater;
import com.sap.sailing.util.SmartFutureCache.UpdateInterval;


public class MarkRoundingCacheUpdater extends AbstractCacheUpdater<Competitor, Pair<Void, Void>, FixUpdateInterval> {
    private final SmartFutureCache<Competitor, Pair<Void, Void>, FixUpdateInterval> cache;
    
    public MarkRoundingCacheUpdater() {
        this.cache = new SmartFutureCache<>(this, "nameForLocks");
    }
    
    public SmartFutureCache<Competitor, Pair<Void, Void>, FixUpdateInterval> getCache() {
        return cache;
    }

    @Override
    public Pair<Void, Void> computeCacheUpdate(Competitor key, FixUpdateInterval updateInterval) throws Exception {
        Pair<Void, Void> latest = cache.get(key, /* waitForLatest */ false);
        return latest;
    }
}

class FixUpdateInterval implements UpdateInterval<FixUpdateInterval> {
    @Override
    public FixUpdateInterval join(FixUpdateInterval otherUpdateInterval) {
        // TODO Auto-generated method stub
        return null;
    }
}