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
    private transient SmartFutureCache<String, Map<TrackedRace, Set<TrackedRace>>, EmptyUpdateInterval> previousRacesByRaceCache;
    private final String RACES_ORDER_LIST_CACHE_KEY = "racesOrderCacheKey";
    private final String RACES_ORDER_LIST_LOCKS_NAME = getClass().getName();

    public AbstractRaceExecutionOrderProvider() {
        previousRacesByRaceCache = createRacesOrderCache();
        triggerUpdate();
    }

    @Override
    public void defaultAction() {
        triggerUpdate();
    }

    @Override
    public void triggerUpdate() {
        previousRacesByRaceCache.triggerUpdate(RACES_ORDER_LIST_CACHE_KEY, /* update interval */null);
    }

    private Map<TrackedRace, Set<TrackedRace>> getPreviousRacesByRace() {
        return previousRacesByRaceCache.get(RACES_ORDER_LIST_CACHE_KEY, /* waitForLatest */true);
    }

    protected abstract Map<Fleet, Iterable<? extends RaceColumn>> getRaceColumnsOfSeries();
    
    /**
     * For all tracked races found on any of the {@link #getRaceColumnsOfSeries() race columns} produces a valid and
     * potentially empty set.
     */
    private Map<TrackedRace, Set<TrackedRace>> reloadAndGetPreviousRacesByRace() {
        final Map<TrackedRace, Set<TrackedRace>> previousRacesByRace = new HashMap<>();
        for (Entry<Fleet, Iterable<? extends RaceColumn>> raceColumnsInSeries : getRaceColumnsOfSeries().entrySet()) {
            addPreviousRaces(previousRacesByRace, raceColumnsInSeries.getKey(), raceColumnsInSeries.getValue());
        }
        return previousRacesByRace;
    }

    private void addPreviousRaces(final Map<TrackedRace, Set<TrackedRace>> previousRacesByRace, Fleet fleet,
            final Iterable<? extends RaceColumn> raceColumns) {
        TrackedRace previousRace = null;
        for (RaceColumn currentRaceColumn : raceColumns) {
            final TrackedRace trackedRaceInColumnForFleet = currentRaceColumn.getTrackedRace(fleet);
            if (trackedRaceInColumnForFleet != null) {
                Set<TrackedRace> previousRaces = previousRacesByRace.get(trackedRaceInColumnForFleet);
                if (previousRaces == null) {
                    previousRaces = new HashSet<>();
                    previousRacesByRace.put(trackedRaceInColumnForFleet, previousRaces);
                }
                if (previousRace != null) {
                    previousRaces.add(previousRace);
                }
                previousRace = trackedRaceInColumnForFleet;
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
                            return reloadAndGetPreviousRacesByRace();
                        } else {
                            final Map<TrackedRace, Set<TrackedRace>> emptyMap = Collections.emptyMap();
                            return emptyMap;
                        }
                    }
                }, RACES_ORDER_LIST_LOCKS_NAME);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.previousRacesByRaceCache = createRacesOrderCache();
        // don't call triggerUpdate() as of now because the cache's owner may not yet be fully initialized,
        // so the getRaceColumnsOfSeries() method may not yet be able to do its work. The owner must call
        // triggerUpdate when fully initialized.
    }

    @Override
    public Set<TrackedRace> getPreviousRacesInExecutionOrder(TrackedRace race) {
        final Set<TrackedRace> result;
        final Map<TrackedRace, Set<TrackedRace>> previousRacesByRace = getPreviousRacesByRace();
        if (previousRacesByRace != null) {
            result = previousRacesByRace.get(race);
        } else {
            result = Collections.emptySet();
        }
        return result;
    }
}
