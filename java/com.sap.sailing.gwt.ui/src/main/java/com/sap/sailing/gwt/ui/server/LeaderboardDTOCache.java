package com.sap.sailing.gwt.ui.server;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.util.impl.LockUtil;

/**
 * Caches the expensive to compute {@link LeaderboardDTO} results of a
 * {@link SailingServiceImpl#computeLeaderboardByName(String, com.sap.sailing.domain.common.TimePoint, Collection, boolean)} call.
 * By listening as {@link RaceChangeListener} on all tracked races attached to the leaderboard, and by updating this list
 * by listening as {@link RaceColumnListener} on the {@link Leaderboard}, each time a race attached to a leaderboard for which
 * this cache holds one or more {@link LeaderboardDTO}s changes, the cache entries for that leaderboard are removed. Also,
 * when the {@link ScoreCorrection}s of a leaderboard change, a {@link ScoreCorrectionListener} that is registered will be
 * notified and removes the leaderboard's cache entries from this cache.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardDTOCache {
    private static final Logger logger = Logger.getLogger(LeaderboardDTOCache.class.getName());
    
    /**
     * In live operations, {@link #getLeaderboardByName(String, Date, Collection)} is the application's
     * bottleneck. When two clients ask the same data for the same leaderboard with their
     * <code>waitForLatestAnalyses</code> parameters set to <code>false</code>, expansion state and (quantized) time
     * stamp, no two computations should be spawned for the two clients. Instead, if the computation is still running,
     * all clients asking the same wait for the single result. Results are cached in this LRU-based evicting cache.
     */
    private final WeakHashMap<Leaderboard, Map<Util.Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>> leaderboardCache;
    private int leaderboardByNameCacheHitCount;
    private int leaderboardByNameCacheMissCount;
    
    /**
     * Tells if leaderboard computations shall wait for long-running analyses to complete or if they instead use the
     * last good analysis result, even if it is a bit outdated as compared to the time point queried. This particularly
     * applies to the wind estimation and the maneuver analysis.
     */
    private final boolean waitForLatestAnalyses;
    
    private final WeakHashMap<Leaderboard, Map<TrackedRace, Set<CacheInvalidationListener>>> invalidationListenersPerLeaderboard;
    
    private final SailingServiceImpl sailingService;
    
    /**
     * A multi-threaded executor for the currently running leaderboard requests, executing the {@link Future}s currently
     * pending.
     */
    private final Executor computeLeadearboardByNameExecutor;
    
    private final WeakHashMap<Leaderboard, RaceColumnListener> raceColumnListeners;
    
    private final WeakHashMap<Leaderboard, CacheInvalidationUponScoreCorrectionListener> scoreCorrectionListeners;
    
    private class CacheInvalidationListener implements RaceChangeListener {
        private final Leaderboard leaderboard;
        private final TrackedRace trackedRace;
        
        public CacheInvalidationListener(Leaderboard leaderboard, TrackedRace trackedRace) {
            this.leaderboard = leaderboard;
            this.trackedRace = trackedRace;
        }
        
        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
            removeFromCache(leaderboard);
        }

        @Override
        public void statusChanged(TrackedRaceStatus newStatus) {
            removeFromCache(leaderboard);
        }

        @Override
        public void markPositionChanged(GPSFix fix, Mark mark) {
            removeFromCache(leaderboard);
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
                Iterable<MarkPassing> markPassings) {
            removeFromCache(leaderboard);
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            removeFromCache(leaderboard);
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            removeFromCache(leaderboard);
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            removeFromCache(leaderboard);
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            removeFromCache(leaderboard);
        }

        @Override
        public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
            removeFromCache(leaderboard);
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            removeFromCache(leaderboard);
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            removeFromCache(leaderboard);
        }

        private void removeFromTrackedRace() {
            trackedRace.removeListener(this);
        }
    }
    
    private class CacheInvalidationUponScoreCorrectionListener implements ScoreCorrectionListener {
        private final Leaderboard leaderboard;
        
        public CacheInvalidationUponScoreCorrectionListener(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
        }

        @Override
        public void correctedScoreChanced(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore) {
            removeFromCache(leaderboard);
        }

        @Override
        public void maxPointsReasonChanced(Competitor competitor, MaxPointsReason oldMaxPointsReason,
                MaxPointsReason newMaxPointsReason) {
            removeFromCache(leaderboard);
        }

        @Override
        public void carriedPointsChanged(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints) {
            removeFromCache(leaderboard);
        }

        @Override
        public void isSuppressedChanged(Competitor competitor, boolean newIsSuppressed) {
            removeFromCache(leaderboard);
        }
    }
    
    public LeaderboardDTOCache(SailingServiceImpl sailingService, boolean waitForLatestAnalyses) {
        this.sailingService = sailingService;
        this.waitForLatestAnalyses = waitForLatestAnalyses;
        // if the leaderboard becomes weakly referenced and eventually GCed, then so can the cached results for it
        this.leaderboardCache = new WeakHashMap<Leaderboard, Map<Util.Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>>();
        this.computeLeadearboardByNameExecutor =
                new ThreadPoolExecutor(/* corePoolSize */ 0,
                        /* maximumPoolSize */ 10*Runtime.getRuntime().availableProcessors(),
                        /* keepAliveTime */ 60, TimeUnit.SECONDS,
                        /* workQueue */ new LinkedBlockingQueue<Runnable>());
        this.invalidationListenersPerLeaderboard = new WeakHashMap<Leaderboard, Map<TrackedRace, Set<CacheInvalidationListener>>>();
        this.raceColumnListeners = new WeakHashMap<Leaderboard, RaceColumnListener>();
        this.scoreCorrectionListeners = new WeakHashMap<Leaderboard, CacheInvalidationUponScoreCorrectionListener>();
    }
    
    private void removeFromCache(Leaderboard leaderboard) {
        synchronized (leaderboardCache) {
            leaderboardCache.remove(leaderboard);
        }
        synchronized (invalidationListenersPerLeaderboard) {
            Map<TrackedRace, Set<CacheInvalidationListener>> listenersMap = invalidationListenersPerLeaderboard
                    .remove(leaderboard);
            if (listenersMap != null) {
                for (Map.Entry<TrackedRace, Set<CacheInvalidationListener>> e : listenersMap.entrySet()) {
                    for (CacheInvalidationListener listener : e.getValue()) {
                        listener.removeFromTrackedRace();
                    }
                }
            }
        }
        synchronized (raceColumnListeners) {
            leaderboard.removeRaceColumnListener(raceColumnListeners.remove(leaderboard));
        }
        synchronized (scoreCorrectionListeners) {
            leaderboard.getScoreCorrection().removeScoreCorrectionListener(scoreCorrectionListeners.remove(leaderboard));
        }
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
        final CacheInvalidationUponScoreCorrectionListener scoreCorrectionListener = new CacheInvalidationUponScoreCorrectionListener(leaderboard);
        leaderboard.getScoreCorrection().addScoreCorrectionListener(scoreCorrectionListener);
        synchronized (scoreCorrectionListeners) {
            scoreCorrectionListeners.put(leaderboard, scoreCorrectionListener);
        }
        final RaceColumnListener raceColumnListener = new RaceColumnListener() {
            private static final long serialVersionUID = 8165124797028386317L;

            @Override
            public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                removeFromCache(leaderboard);
                registerListener(leaderboard, trackedRace);
            }

            /**
             * This listener must not be serialized. See also bug 952. 
             */
            @Override
            public boolean isTransient() {
                return true;
            }
            
            @Override
            public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                removeFromCache(leaderboard);
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

            @Override
            public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
                removeFromCache(leaderboard);
            }

            @Override
            public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
                return true;
            }

            @Override
            public void raceColumnAddedToContainer(RaceColumn raceColumn) {
                removeFromCache(leaderboard);
            }

            @Override
            public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
                removeFromCache(leaderboard);
            }

            @Override
            public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
                removeFromCache(leaderboard);
            }

            @Override
            public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
                removeFromCache(leaderboard);
            }

            @Override
            public void resultDiscardingRuleChanged(ThresholdBasedResultDiscardingRule oldDiscardingRule,
                    ThresholdBasedResultDiscardingRule newDiscardingRule) {
                removeFromCache(leaderboard);
            }

            @Override
            public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
                removeFromCache(leaderboard);
            }
            
            @Override
            public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
                removeFromCache(leaderboard);
            }
        };
        leaderboard.addRaceColumnListener(raceColumnListener);
        synchronized (raceColumnListeners) {
            raceColumnListeners.put(leaderboard, raceColumnListener);
        }
    }

    private void registerListener(final Leaderboard leaderboard, TrackedRace trackedRace) {
        Map<TrackedRace, Set<CacheInvalidationListener>> invalidationListeners;
        final CacheInvalidationListener listener;
        synchronized (invalidationListenersPerLeaderboard) {
            listener = new CacheInvalidationListener(leaderboard, trackedRace);
            trackedRace.addListener(listener);
            invalidationListeners = invalidationListenersPerLeaderboard.get(leaderboard);
            if (invalidationListeners == null) {
                invalidationListeners = new HashMap<TrackedRace, Set<CacheInvalidationListener>>();
                invalidationListenersPerLeaderboard.put(leaderboard, invalidationListeners);
            }
        }
        Set<CacheInvalidationListener> listeners = invalidationListeners.get(trackedRace);
        if (listeners == null) {
            listeners = new HashSet<CacheInvalidationListener>();
            invalidationListeners.put(trackedRace, listeners);
        }
        listeners.add(listener);
    }
    
    /**
     * If the cache holds entries for the <code>leaderboard</code> requested, compare <code>timePoint</code> to the
     * {@link #getLatestModification latest modification} affecting the <code>leaderboard</code>. If
     * <code>timePoint</code> is after that time, adjust it to the {@link #getLatestModification latest modification
     * time} for cache lookup and computation. This will increase chances that a subsequent request will achieve a cache
     * hit.
     * <p>
     * 
     * The {@link #waitForLatestAnalyses} field is passed on to
     * {@link SailingServiceImpl#computeLeaderboardByName(Leaderboard, TimePoint, Collection, boolean)} if a new cache
     * entry needs to be computed. Caching distinguished between
     */
    public LeaderboardDTO getLeaderboardByName(final Leaderboard leaderboard, final TimePoint timePoint,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails)
            throws NoWindException, InterruptedException, ExecutionException {
        long startOfRequestHandling = System.currentTimeMillis();
        final TimePoint adjustedTimePoint;
        TimePoint timePointOfLastModification = leaderboard.getTimePointOfLatestModification();
        if (timePointOfLastModification != null && timePoint.after(timePointOfLastModification)) {
            adjustedTimePoint = timePointOfLastModification; 
            logger.fine("Adjusted time point in getLeaderboardByName from "+timePoint+" to "+adjustedTimePoint);
        } else {
            adjustedTimePoint = timePoint;
        }
        Util.Pair<TimePoint, Collection<String>> key = new Util.Pair<TimePoint, Collection<String>>(adjustedTimePoint,
                namesOfRaceColumnsForWhichToLoadLegDetails);
        FutureTask<LeaderboardDTO> future = null;
        Map<Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>> map = null;
        boolean cacheHit = false;
        synchronized (leaderboardCache) {
            map = leaderboardCache.get(leaderboard);
            if (map == null) {
                future = null;
                map = new LinkedHashMap<Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>>() {
                    private static final long serialVersionUID = 7287916997229815039L;
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<Pair<TimePoint, Collection<String>>, FutureTask<LeaderboardDTO>> e) {
                        return size() > 10; // remember 10 LeaderboardDTOs per leaderborad
                    }
                };
                leaderboardCache.put(leaderboard, map);
                registerAsListener(leaderboard);
            } else {
                /*
                 * Waiting for latest analyzes results largely regards wind estimation and maneuver cache; see
                 * SmartFutureCache. Even if waitForLatestAnalysis is requested, it is OK to cache. The cache would be
                 * invalidated when the race changes, forcing a new re-calculation based on the latest analysis results.
                 * Once the race stabilizes, the latest analysis results for maneuvers and wind estimation will no
                 * longer change and can quickly be obtained from the respective SmartFutureCache. At the same time, if
                 * a LeaderboardDTOCache entry is found, that was based on the latest analysis results at the time. If
                 * new evidence is received, that would also invalidate the LeaderboardDTOCache. Therefore, it's okay to
                 * re-use the LeaderboardDTOCache match even if the latest analysis results are requested.
                 */
                future = map.get(key);
            }
            if (future == null) {
                final Thread callerThread = Thread.currentThread();
                future = new FutureTask<LeaderboardDTO>(new Callable<LeaderboardDTO>() {
                    @Override
                    public LeaderboardDTO call() throws Exception {
                        // The outer getLeaderboardByName(...) method will always wait for this future's completion.
                        // Therefore, it's safe to propagate the calling thread's locks to this one:
                        LockUtil.propagateLockSetFrom(callerThread);
                        try {
                            LeaderboardDTO result = sailingService.computeLeaderboardByName(leaderboard,
                                    adjustedTimePoint, namesOfRaceColumnsForWhichToLoadLegDetails,
                                    waitForLatestAnalyses);
                            return result;
                        } finally {
                            LockUtil.unpropagateLockSetFrom(callerThread);
                        }
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
            logger.info("Cache hit in getLeaderboardByName("+leaderboard.getName()+", "+adjustedTimePoint+", "+namesOfRaceColumnsForWhichToLoadLegDetails+")");
        } else {
            leaderboardByNameCacheMissCount++;
        }
        logger.info("getLeaderboardByName cache hit vs. miss: "+leaderboardByNameCacheHitCount+"/"+leaderboardByNameCacheMissCount);
        LeaderboardDTO result = future.get();
        logger.fine("getLeaderboardByName("+leaderboard.getName()+", "+adjustedTimePoint+", "+namesOfRaceColumnsForWhichToLoadLegDetails+") took "+
                (System.currentTimeMillis()-startOfRequestHandling)+"ms");
        return result;
    }
}
