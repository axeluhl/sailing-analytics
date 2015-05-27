package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.util.SmartFutureCache;
import com.sap.sse.util.SmartFutureCache.EmptyUpdateInterval;

public abstract class AbstractRaceExecutionOrderProvider implements RaceExecutionOrderProvider, RaceColumnListenerWithDefaultAction {
    private static final long serialVersionUID = 4795731834688229568L;
    private transient SmartFutureCache<String, Map<TrackedRace, Set<TrackedRace>>, EmptyUpdateInterval> raceOrderCacheMappingRaceToItsPredecessor;
    private final String RACES_ORDER_LIST_CACHE_KEY = "racesOrderCacheKey";
    private final String RACES_ORDER_LIST_LOCKS_NAME = getClass().getName();

    public AbstractRaceExecutionOrderProvider() {
        raceOrderCacheMappingRaceToItsPredecessor = createRacesOrderCache();
        triggerUpdate();
    }

    @Override
    public void defaultAction() {
        triggerUpdate();
    }

    @Override
    public void triggerUpdate() {
        raceOrderCacheMappingRaceToItsPredecessor.triggerUpdate(RACES_ORDER_LIST_CACHE_KEY, /* update interval */null);
    }

    private Map<TrackedRace, Set<TrackedRace>> getPredecessorsMap() {
        return raceOrderCacheMappingRaceToItsPredecessor.get(RACES_ORDER_LIST_CACHE_KEY, /* waitForLatest */true);
    }

    protected abstract Map<Fleet, Iterable<? extends RaceColumn>> getRaceColumnsOfSeries();
    
    private Map<TrackedRace, Set<TrackedRace>> reloadRacesInExecutionOrder() {
        final Map<TrackedRace, Set<TrackedRace>> raceIdListInExecutionOrder = new HashMap<>();
        for (Entry<Fleet, Iterable<? extends RaceColumn>> raceColumnsInSeries : getRaceColumnsOfSeries().entrySet()) {
            addPredecessors(raceIdListInExecutionOrder, raceColumnsInSeries.getKey(), raceColumnsInSeries.getValue());
        }
        return raceIdListInExecutionOrder;
    }

    protected void addPredecessors(final Map<TrackedRace, Set<TrackedRace>> raceIdListInExecutionOrder, Fleet fleet,
            final Iterable<? extends RaceColumn> raceColumns) {
        TrackedRace predecessor = null;
        for (RaceColumn currentRaceColumn : raceColumns) {
            final TrackedRace trackedRaceInColumnForFleet = currentRaceColumn.getTrackedRace(fleet);
            if (trackedRaceInColumnForFleet != null) {
                Set<TrackedRace> predecessors = raceIdListInExecutionOrder.get(trackedRaceInColumnForFleet);
                if (predecessors == null) {
                    predecessors = new HashSet<>();
                    raceIdListInExecutionOrder.put(trackedRaceInColumnForFleet, predecessors);
                }
                if (predecessor != null) {
                    predecessors.add(predecessor);
                }
                predecessor = trackedRaceInColumnForFleet;
            }
        }
    }

    private SmartFutureCache<String, Map<TrackedRace, Set<TrackedRace>>, EmptyUpdateInterval> createRacesOrderCache() {
        return new SmartFutureCache<String, Map<TrackedRace, Set<TrackedRace>>, SmartFutureCache.EmptyUpdateInterval>(
                new SmartFutureCache.AbstractCacheUpdater<String, Map<TrackedRace, Set<TrackedRace>>, SmartFutureCache.EmptyUpdateInterval>() {
                    @Override
                    public Map<TrackedRace, Set<TrackedRace>> computeCacheUpdate(String key,
                            EmptyUpdateInterval updateInterval) throws Exception {
                        if (key.equals(RACES_ORDER_LIST_CACHE_KEY)) {
                            return reloadRacesInExecutionOrder();
                        } else {
                            final Map<TrackedRace, Set<TrackedRace>> emptyMap = Collections.emptyMap();
                            return emptyMap;
                        }
                    }
                }, RACES_ORDER_LIST_LOCKS_NAME);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.raceOrderCacheMappingRaceToItsPredecessor = createRacesOrderCache();
        // don't call triggerUpdate() as of now because the cache's owner may not yet be fully initialized,
        // so the getRaceColumnsOfSeries() method may not yet be able to do its work. The owner must call
        // triggerUpdate when fully initialized.
    }

    @Override
    public Set<TrackedRace> getPreviousRaceInExecutionOrder(TrackedRace race) {
        final Set<TrackedRace> result;
        final Map<TrackedRace, Set<TrackedRace>> predecessorMap = getPredecessorsMap();
        if (predecessorMap != null) {
            result = predecessorMap.get(race);
        } else {
            result = null;
        }
        return result;
    }
}
