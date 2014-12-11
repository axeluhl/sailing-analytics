package com.sap.sailing.polars.caching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.util.SmartFutureCache;

public class PolarFixCache extends
        SmartFutureCache<BoatClass, Map<RegattaAndRaceIdentifier, List<PolarFix>>, PolarFixCacheRaceInterval> {

    private final List<PolarFixCacheUpdateDoneListener> cacheUpdateDoneListeners = new ArrayList<PolarFixCacheUpdateDoneListener>();

    public PolarFixCache(Executor executor) {
        super(new PolarFixCacheUpdater(executor), "polarFixCache");
    }

    @Override
    protected void cache(BoatClass key, Map<RegattaAndRaceIdentifier, List<PolarFix>> value) {
        super.cache(key, value);
        synchronized (cacheUpdateDoneListeners) {
            for (PolarFixCacheUpdateDoneListener listener : cacheUpdateDoneListeners) {
                listener.cacheUpdateDoneForBoatClass(key);
            }
        }
    }

    public void addListener(PolarFixCacheUpdateDoneListener listener) {
        synchronized (cacheUpdateDoneListeners) {
            cacheUpdateDoneListeners.add(listener);
        }
    }

    public void removeListener(PolarFixCacheUpdateDoneListener listener) {
        synchronized (cacheUpdateDoneListeners) {
            if (cacheUpdateDoneListeners.contains(listener)) {
                cacheUpdateDoneListeners.remove(listener);
            }
        }
    }

    public Set<PolarFix> getFixesForTrackedRaces(Set<TrackedRace> trackedRaces) throws NoCacheEntryException {
        Set<PolarFix> resultList = new HashSet<PolarFix>();
        Set<TrackedRace> notCached = new HashSet<TrackedRace>();
        for (TrackedRace trackedRace : trackedRaces) {
            Map<RegattaAndRaceIdentifier, List<PolarFix>> result = get(trackedRace.getRace().getBoatClass(), false);
            List<PolarFix> resultForRace = result.get(trackedRace.getRaceIdentifier());
            if (resultForRace == null) {
                notCached.add(trackedRace);
            } else {
                resultList.addAll(resultForRace);
            }
        }
        if (notCached.size() > 0) {
            throw new NoCacheEntryException(notCached, resultList);
        }
        return resultList;
    }

}
