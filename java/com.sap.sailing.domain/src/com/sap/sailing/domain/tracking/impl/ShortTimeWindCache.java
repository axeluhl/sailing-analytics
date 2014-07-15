package com.sap.sailing.domain.tracking.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.Util.Pair;

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
    private final ConcurrentHashMap<Pair<Position, TimePoint>, Wind> cache;
    
    /**
     * The keys of {@link #cache} in the order in which to invalidate them, keyed by the time they were entered into the cache.
     */
    private final ConcurrentLinkedDeque<Pair<Long, Pair<Position, TimePoint>>> order;
    
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
            Pair<Long, Pair<Position, TimePoint>> next;
            while ((next = order.pollFirst()) != null && next.getA() < oldestToKeep) {
                cache.remove(next.getB());
            }
            synchronized (order) {
                if (order.isEmpty()) {
                    cancel();
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
    
    public void add(Position position, TimePoint timePoint, Wind wind) {
        final Pair<Position, TimePoint> key = new Pair<Position, TimePoint>(position, timePoint);
        cache.put(key, wind);
        boolean orderEmpty = order.isEmpty();
        synchronized (order) {
            order.add(new Pair<Long, Pair<Position, TimePoint>>(System.currentTimeMillis(), key));
            if (orderEmpty) {
                ensureTimerIsRunning();
            }
        }
    }
    
    public Wind getWind(Position position, TimePoint timePoint) {
        Wind wind;
        final Pair<Position, TimePoint> key = new Pair<>(position, timePoint);
        wind = cache.get(key);
        if (wind == null) {
            misses++;
            if (misses % 100000l == 0 && logger.isLoggable(Level.FINE)) {
                logger.fine("hits: " + hits + ", misses: " + misses);
            }
            wind = trackedRace.getWindUncached(position, timePoint);
            if (wind != null) {
                add(position, timePoint, wind);
            }
        } else {
            hits++;
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
