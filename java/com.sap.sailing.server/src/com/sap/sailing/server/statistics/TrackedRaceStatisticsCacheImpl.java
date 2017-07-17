package com.sap.sailing.server.statistics;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
import com.sap.sailing.domain.tracking.AbstractTrackedRegattaAndRaceObserver;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.util.SmartFutureCache;
import com.sap.sse.util.SmartFutureCache.AbstractCacheUpdater;
import com.sap.sse.util.SmartFutureCache.EmptyUpdateInterval;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * Implementation of {@link TrackedRaceStatisticsCache} that observes all {@link TrackedRegatta} and {@link TrackedRace}
 * instances to calculate and update the internal statistics cache.
 */
public class TrackedRaceStatisticsCacheImpl extends AbstractTrackedRegattaAndRaceObserver implements TrackedRaceStatisticsCache {
    private static final Logger logger = Logger.getLogger(TrackedRaceStatisticsCacheImpl.class.getName());
    
    private static final Duration MINIMUM_DELAY_FOR_CACHE_RECALCULATION = Duration.ONE_SECOND.times(10);
    
    /**
     * Listeners added a {@link TrackedRaces} that need to be cleaned when {@link TrackedRace}s are removed. 
     */
    private final Map<TrackedRace, Listener> listeners;
    
    
    /**
     * Cache that holds and updates {@link TrackedRaceStatistics} instances per known {@link TrackedRace}.
     */
    private final SmartFutureCache<TrackedRace, TrackedRaceStatistics, ?> cache;
    
    private final ScheduledExecutorService executor;
    
    /**
     * We don't want to flood the CPU with cache re-calculations. Therefore, we enqueue triggers with the
     * {@link #executor} when nothing is scheduled yet. Once the trigger is forwarded to the actual
     * {@link SmartFutureCache}, the respective entry for the {@link TrackedRace} is removed while holding the
     * monitor of the {@link #scheduledTriggers} map.
     */
    private final WeakHashMap<TrackedRace, Future<?>> scheduledTriggers;
    
    /**
     * The first call is to be made fast to compute the cache contents quickly upon the first trigger. After that,
     * triggering the cache re-calculation shall be delayed by {@link #MINIMUM_DELAY_FOR_CACHE_RECALCULATION}.
     */
    private final WeakHashMap<TrackedRace, Boolean> scheduleDelayed;
    
    public TrackedRaceStatisticsCacheImpl() {
        executor = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
        scheduledTriggers = new WeakHashMap<>();
        scheduleDelayed = new WeakHashMap<>();
        listeners = new ConcurrentHashMap<>();
        cache = new SmartFutureCache<>(new Updater(), TrackedRaceStatisticsCacheImpl.class.getSimpleName());
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

    private void triggerUpdate(final DynamicTrackedRace trackedRace) {
        synchronized (scheduledTriggers) {
            if (scheduledTriggers.get(trackedRace) == null) {
                final long delay = scheduleDelayed.containsKey(trackedRace) ? MINIMUM_DELAY_FOR_CACHE_RECALCULATION.asMillis() : 0l;
                logger.log(Level.FINEST, ()->"Scheduling statistics update trigger for race " + trackedRace.getRaceIdentifier()+
                        " in "+delay+"ms");
                scheduledTriggers.put(trackedRace, executor.schedule(()->{
                    synchronized (scheduledTriggers) {
                        scheduledTriggers.remove(trackedRace);
                    }
                    cache.triggerUpdate(trackedRace, null);
                    logger.log(Level.FINEST, ()->"Triggering statistics update for race "+trackedRace.getRaceIdentifier());
                }, delay, TimeUnit.MILLISECONDS));
                scheduleDelayed.put(trackedRace, true);
                // delay the triggering when we have triggered it at least once before
            }
        }
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
            logger.log(Level.FINE, ()->"Updating statistics for race " + trackedRace.getRaceIdentifier());
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
