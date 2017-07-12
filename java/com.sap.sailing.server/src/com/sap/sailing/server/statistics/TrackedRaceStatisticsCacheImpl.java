package com.sap.sailing.server.statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(TrackedRaceStatisticsCacheImpl.class.getName());
    
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
        triggerUpdate(trackedRace);
    }

    private void triggerUpdate(DynamicTrackedRace trackedRace) {
        logger.log(Level.FINE, "Updating Statistics for race " + trackedRace.getRaceIdentifier());
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
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
            triggerUpdate(trackedRace);
        }
        
        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
            triggerUpdate(trackedRace);
        }
    }
}
