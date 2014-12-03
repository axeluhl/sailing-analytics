package com.sap.sailing.domain.tracking.impl;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;

/**
 * Caches wind information across a short duration of a few seconds, based on position and time point. A separate timer
 * keeps invalidating records after a configurable duration. When the cache runs empty, the timer is stopped. When new entries
 * appear, the timer is started again.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ShortTimeWindCache {
    private static final Logger logger = Logger.getLogger(ShortTimeWindCache.class.getName());
    private final ConcurrentHashMap<Triple<Position, TimePoint, Set<WindSource>>,
                                    WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>>> cache;

    /**
     * The keys of {@link #cache} in the order in which to invalidate them, keyed by the time they were entered into the cache.
     */
    private final ConcurrentLinkedDeque<Pair<Long, Triple<Position, TimePoint, Set<WindSource>>>> order;
    
    /**
     * Creation and removal / cancellation of the timer is synchronized using {@link #order}.
     */
    private Timer timer;
    
    private final long preserveHowManyMilliseconds;
    private final TrackedRaceImpl trackedRace;
    
    private long hits;
    private long misses;
    
    private class CacheInvalidator extends TimerTask {
        @Override
        public void run() {
            long oldestToKeep = System.currentTimeMillis() - preserveHowManyMilliseconds;
            Pair<Long, Triple<Position, TimePoint, Set<WindSource>>> next;
            while ((next = order.pollFirst()) != null && next.getA() < oldestToKeep) {
                cache.remove(next.getB());
            }
            synchronized (order) {
                if (order.isEmpty()) {
                    cancel();
                    timer.cancel();
                    timer = null;
                }
            }
        }
    }
    
    public ShortTimeWindCache(TrackedRaceImpl trackedRace, long preserveHowManyMilliseconds) {
        this.trackedRace = trackedRace;
        this.preserveHowManyMilliseconds = preserveHowManyMilliseconds;
        cache = new ConcurrentHashMap<>();
        order = new ConcurrentLinkedDeque<>();
    }
    
    private void add(Triple<Position, TimePoint, Set<WindSource>> key, WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> wind) {
        cache.put(key, wind);
        boolean orderEmpty = order.isEmpty();
        synchronized (order) {
            order.add(new Pair<Long, Triple<Position, TimePoint, Set<WindSource>>>(System.currentTimeMillis(), key));
            if (orderEmpty) {
                ensureTimerIsRunning();
            }
        }
    }
    
    WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> getWindWithConfidence(Position p,
            TimePoint at, Set<WindSource> windSourcesToExclude) {
        WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> wind;
        final Triple<Position, TimePoint, Set<WindSource>> key = new Triple<Position, TimePoint, Set<WindSource>>(p, at, windSourcesToExclude);
        wind = cache.get(key);
        if (wind == null) {
            misses++;
            wind = trackedRace.getWindWithConfidenceUncached(p, at, windSourcesToExclude);
            if (wind != null) {
                add(key, wind);
            }
        } else {
            hits++;
        }
        if ((hits+misses) % 100000l == 0 && logger.isLoggable(Level.FINE)) {
            logger.fine("hits: " + hits + ", misses: " + misses);
        }
        return wind;
    }
    
    /**
     * Must be called under write lock
     */
    private void ensureTimerIsRunning() {
        if (timer == null) {
            timer = new Timer(getClass().getSimpleName()+" for "+trackedRace.getRace().getName());
            timer.scheduleAtFixedRate(new CacheInvalidator(), /* delay */ preserveHowManyMilliseconds, preserveHowManyMilliseconds);
        }
    }
}
