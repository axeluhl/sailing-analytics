package com.sap.sailing.gwt.ui.server;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

/**
 * Caches the expensive to compute {@link LeaderboardDTO} results of a
 * {@link SailingServiceImpl#computeLeaderboardByName(String, com.sap.sailing.domain.common.TimePoint, Collection, boolean)} call.
 * By listening as {@link RaceChangeListener} on all tracked races attached to the leaderboard, and by updating this list
 * by listening as {@link RaceColumnListener} on the {@link Leaderboard}, each time a race attached to a leaderboard for which
 * this cache holds one or more {@link LeaderboardDTO}s changes, the cache entries for that leaderboard are removed.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardDTOCache {
    private static final Logger logger = Logger.getLogger(LeaderboardDTOCache.class.getName());
    
    /**
     * In live operations, {@link #getLeaderboardByName(String, Date, Collection, boolean)} is the application's
     * bottleneck. When two clients ask the same data for the same leaderboard with their
     * <code>waitForLatestAnalyses</code> parameters set to <code>false</code>, expansion state and (quantized) time
     * stamp, no two computations should be spawned for the two clients. Instead, if the computation is still running,
     * all clients asking the same wait for the single result. Results are cached in this LRU-based evicting cache.
     */
    private final WeakHashMap<Leaderboard, Map<Util.Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>> leaderboardCache;
    private int leaderboardByNameCacheHitCount;
    private int leaderboardByNameCacheMissCount;
    
    private final WeakHashMap<Leaderboard, Map<TrackedRace, Set<CacheInvalidationListener>>> invalidationListenersPerLeaderboard;
    
    private final SailingServiceImpl sailingService;
    
    /**
     * A multi-threaded executor for the currently running leaderboard requests, executing the {@link Future}s currently
     * pending.
     */
    private final Executor computeLeadearboardByNameExecutor;
    
    private class CacheInvalidationListener implements RaceChangeListener {
        private final Leaderboard leaderboard;
        private final TrackedRace trackedRace;
        
        public CacheInvalidationListener(Leaderboard leaderboard, TrackedRace trackedRace) {
            this.leaderboard = leaderboard;
            this.trackedRace = trackedRace;
        }
        
        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
            removeFromCache();
        }

        @Override
        public void buoyPositionChanged(GPSFix fix, Buoy buoy) {
            removeFromCache();
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
                Iterable<MarkPassing> markPassings) {
            removeFromCache();
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            removeFromCache();
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            removeFromCache();
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            removeFromCache();
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            removeFromCache();
        }

        @Override
        public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
            removeFromCache();
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            removeFromCache();
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            removeFromCache();
        }

        private void removeFromCache() {
            synchronized (leaderboardCache) {
                leaderboardCache.remove(leaderboard);
            }
            synchronized (invalidationListenersPerLeaderboard) {
                Map<TrackedRace, Set<CacheInvalidationListener>> listenersMap = invalidationListenersPerLeaderboard.remove(leaderboard);
                if (listenersMap != null) {
                    Set<CacheInvalidationListener> listeners = listenersMap.get(trackedRace);
                    if (listeners != null) {
                        for (CacheInvalidationListener listener : listeners) {
                            listener.removeFromTrackedRace();
                        }
                    }
                }
            }
        }

        private void removeFromTrackedRace() {
            trackedRace.removeListener(this);
        }
    }

    public LeaderboardDTOCache(SailingServiceImpl sailingService) {
        this.sailingService = sailingService;
        // if the leaderboard becomes weakly referenced and eventually GCed, then so can the cached results for it
        this.leaderboardCache = new WeakHashMap<Leaderboard, Map<Util.Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>>();
        this.computeLeadearboardByNameExecutor = Executors.newFixedThreadPool(10*Runtime.getRuntime().availableProcessors());
        this.invalidationListenersPerLeaderboard = new WeakHashMap<Leaderboard, Map<TrackedRace, Set<CacheInvalidationListener>>>();
    }
    
    /**
     * Listens at the leaderboard for {@link TrackedRace}s being connected to / disconnected from race columns. Whenever this
     * happens, the listener structure that uses {@link CacheInvalidationListener}s to observe the individual tracked races
     * is updated accordingly.
     */
    private void registerAsListener(final Leaderboard leaderboard) {
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            registerListener(leaderboard, trackedRace);
        }
        leaderboard.addRaceColumnListener(new RaceColumnListener() {
            private static final long serialVersionUID = 8165124797028386317L;
            @Override
            public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                registerListener(leaderboard, trackedRace);
            }

            @Override
            public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                Map<TrackedRace, Set<CacheInvalidationListener>> listenersMap = invalidationListenersPerLeaderboard.get(leaderboard);
                if (listenersMap != null) {
                    Set<CacheInvalidationListener> listeners = listenersMap.get(trackedRace);
                    if (listeners != null) {
                        for (CacheInvalidationListener listener : listeners) {
                            listener.removeFromTrackedRace();
                        }
                    }
                }
            }
        });
    }

    private void registerListener(final Leaderboard leaderboard, TrackedRace trackedRace) {
        final CacheInvalidationListener listener = new CacheInvalidationListener(leaderboard, trackedRace);
        trackedRace.addListener(listener);
        Map<TrackedRace, Set<CacheInvalidationListener>> invalidationListeners = invalidationListenersPerLeaderboard.get(leaderboard);
        if (invalidationListeners == null) {
            invalidationListeners = new HashMap<TrackedRace, Set<CacheInvalidationListener>>();
            invalidationListenersPerLeaderboard.put(leaderboard, invalidationListeners);
        }
        Set<CacheInvalidationListener> listeners = invalidationListeners.get(trackedRace);
        if (listeners == null) {
            listeners = new HashSet<CacheInvalidationListener>();
            invalidationListeners.put(trackedRace, listeners);
        }
        listeners.add(listener);
    }

    public LeaderboardDTO getLeaderboardByName(final Leaderboard leaderboard, final TimePoint timePoint,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, final boolean waitForLatestAnalyses)
            throws NoWindException, InterruptedException, ExecutionException {
        long startOfRequestHandling = System.currentTimeMillis();
        Util.Pair<TimePoint, Collection<String>> key = new Util.Pair<TimePoint, Collection<String>>(timePoint,
                namesOfRaceColumnsForWhichToLoadLegDetails);
        FutureTask<LeaderboardDTO> future = null;
        Map<Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>> map = null;
        boolean cacheHit = false;
        synchronized (leaderboardCache) {
            if (!waitForLatestAnalyses) {
                map = leaderboardCache.get(leaderboard);
                if (map == null) {
                    future = null;
                    map = new HashMap<Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>();
                    leaderboardCache.put(leaderboard, map);
                    registerAsListener(leaderboard);
                } else {
                    future = map.get(key);
                }
            }
            if (future == null) {
                future = new FutureTask<LeaderboardDTO>(new Callable<LeaderboardDTO>() {
                    @Override
                    public LeaderboardDTO call() throws Exception {
                        LeaderboardDTO result = sailingService.computeLeaderboardByName(leaderboard, timePoint,
                                namesOfRaceColumnsForWhichToLoadLegDetails, waitForLatestAnalyses);
                        return result;
                    }
                });
                computeLeadearboardByNameExecutor.execute(future);
                map.put(key, future);
            } else {
                cacheHit = true;
            }
        }
        if (cacheHit) {
            leaderboardByNameCacheHitCount++;
            logger.info("Cache hit in getLeaderboardByName("+leaderboard.getName()+", "+timePoint+", "+namesOfRaceColumnsForWhichToLoadLegDetails+")");
        } else {
            leaderboardByNameCacheMissCount++;
        }
        logger.info("getLeaderboardByName cache hit vs. miss: "+leaderboardByNameCacheHitCount+"/"+leaderboardByNameCacheMissCount);
        LeaderboardDTO result = future.get();
        logger.fine("getLeaderboardByName("+leaderboard.getName()+", "+timePoint+", "+namesOfRaceColumnsForWhichToLoadLegDetails+") took "+
                (System.currentTimeMillis()-startOfRequestHandling)+"ms");
        return result;
    }
}
