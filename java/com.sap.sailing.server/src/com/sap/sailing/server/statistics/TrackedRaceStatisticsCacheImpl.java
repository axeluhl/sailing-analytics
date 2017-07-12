package com.sap.sailing.server.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.SmartFutureCache;
import com.sap.sse.util.SmartFutureCache.AbstractCacheUpdater;
import com.sap.sse.util.SmartFutureCache.EmptyUpdateInterval;

public class TrackedRaceStatisticsCacheImpl extends TrackedRegattaAndRaceObserver implements TrackedRaceStatisticsCache {
    
    private final Map<TrackedRace, Listener> listeners = new ConcurrentHashMap<>();
    private final SmartFutureCache<TrackedRace, TrackedRaceStatistics, ?> cache = new SmartFutureCache<>(new Updater(), TrackedRaceStatisticsCacheImpl.class.getSimpleName());

    public TrackedRaceStatisticsCacheImpl() {
    }
    
    @Override
    public TrackedRaceStatistics getStatistics(TrackedRace trackedRace) {
        return cache.get(trackedRace, false);
    }
    
    @Override
    protected void onRaceAdded(RegattaAndRaceIdentifier raceIdentifier, DynamicTrackedRegatta trackedRegatta,
            DynamicTrackedRace trackedRace) {
        Listener listener = new Listener(trackedRace);
        listeners.put(trackedRace, listener);
        trackedRace.addListener(listener);
        cache.triggerUpdate(trackedRace, null);
    }

    @Override
    protected void onRaceRemoved(DynamicTrackedRace trackedRace) {
        Listener listener = listeners.get(trackedRace);
        if(listener != null) {
            trackedRace.removeListener(listener);
        }
        cache.remove(trackedRace);
    }

    private class Updater extends AbstractCacheUpdater<TrackedRace, TrackedRaceStatistics, EmptyUpdateInterval> {

        @Override
        public TrackedRaceStatistics computeCacheUpdate(TrackedRace trackedRace, EmptyUpdateInterval updateInterval)
                throws Exception {
            return new TrackedRaceStatisticsCalculator(trackedRace, false, true).getStatistics();
        }
    }
    
    private class Listener extends AbstractRaceChangeListener {
        private final DynamicTrackedRace trackedRace;

        public Listener(DynamicTrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }
        
        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
            cache.triggerUpdate(trackedRace, null);
        }
        
        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            cache.triggerUpdate(trackedRace, null);
        }
    }
}
