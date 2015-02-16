package com.sap.sailing.domain.leaderboard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorChangeListener;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.WithNationality;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardCache;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Color;
import com.sap.sse.concurrent.ConcurrentWeakHashMap;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Manages a {@link LeaderboardCache}. When a {@link Leaderboard} is {@link LeaderboardCache#add added} to the cache, it
 * start observing the leaderboard for changes through the linked {@link TrackedRace}s as a {@link RaceChangeListener}
 * and through the race columns as a {@link RaceColumnListener}. When changes affecting a leaderboard occur, the
 * {@link LeaderboardCache#removeFromCache(Leaderboard)} method is called on the leaderboard cache.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LeaderboardCacheManager {
    private final WeakHashMap<Leaderboard, CacheInvalidationUponScoreCorrectionListener> scoreCorrectionListeners;
    private final NamedReentrantReadWriteLock scoreCorrectionAndCompetitorChangeListenersLock;
    private final WeakHashMap<Leaderboard, CacheInvalidationUponCompetitorChangeListener> competitorChangeListeners;
    private final ConcurrentWeakHashMap<Leaderboard, ConcurrentHashMap<TrackedRace, Set<CacheInvalidationListener>>> invalidationListenersPerLeaderboard;
    private final WeakHashMap<Leaderboard, RaceColumnListener> raceColumnListeners;
    
    private class CacheInvalidationListener extends AbstractRaceChangeListener {
        private final Leaderboard leaderboard;
        private final TrackedRace trackedRace;
        
        public CacheInvalidationListener(Leaderboard leaderboard, TrackedRace trackedRace) {
            this.leaderboard = leaderboard;
            this.trackedRace = trackedRace;
        }
        
        @Override
        protected void defaultAction() {
            removeFromCache(leaderboard);
        }
        
        private void removeFromTrackedRace() {
            trackedRace.removeListener(this);
        }
    }
    
    private class CacheInvalidationUponCompetitorChangeListener implements CompetitorChangeListener {
        private static final long serialVersionUID = 8308312509904366143L;
        private final Leaderboard leaderboard;
        private final Set<Competitor> observedCompetitors;
        
        /**
         * Registers this listener as listener for all of the <code>leaderboard</code>'s current
         * {@link Leaderboard#getCompetitors() competitors}
         */
        public CacheInvalidationUponCompetitorChangeListener(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
            observedCompetitors = new HashSet<>();
            updateCompetitorListeners();
        }

        @Override
        public void sailIdChanged(String oldSailId, String newSailId) {
            removeFromCache(leaderboard);
        }

        @Override
        public void nationalityChanged(WithNationality what, Nationality oldNationality, Nationality newNationality) {
            removeFromCache(leaderboard);
        }

        @Override
        public void colorChanged(Color oldColor, Color newColor) {
            removeFromCache(leaderboard);
        }

        @Override
        public void nameChanged(String oldName, String newName) {
            removeFromCache(leaderboard);
        }

        public synchronized void updateCompetitorListeners() {
            Set<Competitor> competitorsToStopObserving = new HashSet<>(observedCompetitors);
            for (Competitor competitor : leaderboard.getCompetitors()) {
                if (!observedCompetitors.contains(competitor)) {
                    competitor.addCompetitorChangeListener(this); // start observing competitor
                } else {
                    competitorsToStopObserving.remove(competitor); // keep observing competitor
                }
            }
            for (Competitor competitorToStopObserving : competitorsToStopObserving) {
                competitorToStopObserving.removeCompetitorChangeListener(this);
                observedCompetitors.remove(competitorToStopObserving);
            }
        }

        public synchronized void removeFromAllCompetitors() {
            for (Competitor competitor : observedCompetitors) {
                competitor.removeCompetitorChangeListener(this);
            }
        }

        @Override
        public void emailChanged(String oldEmail, String newEmail) {
            //ignore (email not shown in leaderboard)
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

    private final LeaderboardCache leaderboardCache;
    
    public LeaderboardCacheManager(LeaderboardCache leaderboardCache) {
        this.leaderboardCache = leaderboardCache;
        this.invalidationListenersPerLeaderboard = new ConcurrentWeakHashMap<>();
        this.raceColumnListeners = new WeakHashMap<Leaderboard, RaceColumnListener>();
        this.scoreCorrectionListeners = new WeakHashMap<>();
        this.scoreCorrectionAndCompetitorChangeListenersLock = new NamedReentrantReadWriteLock(
                "Score correction and competitor change listeners", /* fair */false);
        this.competitorChangeListeners = new WeakHashMap<>();
    }
    
    private void removeFromCache(Leaderboard leaderboard) {
        ConcurrentHashMap<TrackedRace, Set<CacheInvalidationListener>> listenersMap = invalidationListenersPerLeaderboard.remove(leaderboard);
        if (listenersMap != null) {
            for (Map.Entry<TrackedRace, Set<CacheInvalidationListener>> e : listenersMap.entrySet()) {
                synchronized (e.getValue()) { // the Set is a Collections.synchronizedSet, and synchronization is required for iteration
                    for (CacheInvalidationListener listener : e.getValue()) {
                        listener.removeFromTrackedRace();
                    }
                }
            }
        }
        synchronized (raceColumnListeners) {
            leaderboard.removeRaceColumnListener(raceColumnListeners.remove(leaderboard));
        }
        LockUtil.lockForWrite(scoreCorrectionAndCompetitorChangeListenersLock);
        final CacheInvalidationUponScoreCorrectionListener removedScoreCorrectionListener;
        final CacheInvalidationUponCompetitorChangeListener removedCompetitorChangeListener;
        try {
            removedScoreCorrectionListener = scoreCorrectionListeners.remove(leaderboard);
            removedCompetitorChangeListener = competitorChangeListeners.remove(leaderboard);
        } finally {
            LockUtil.unlockAfterWrite(scoreCorrectionAndCompetitorChangeListenersLock);
        }
        if (removedScoreCorrectionListener != null) {
            leaderboard.getScoreCorrection().removeScoreCorrectionListener(removedScoreCorrectionListener);
        }
        if (removedCompetitorChangeListener != null) {
            removedCompetitorChangeListener.removeFromAllCompetitors();
        }
        // invalidate after the listeners have been removed; this is important because invalidation may trigger a
        // re-calculation which then in turn may call add(leaderboard) asynchronously again which may be executed
        // before the listener removal happens here. This could lead to a race condition where listeners are
        // removed again after the invalidation has just added them again. See also bug 1807.
        leaderboardCache.invalidate(leaderboard);
    }
    
    public void add(Leaderboard leaderboard) {
        leaderboardCache.add(leaderboard);
        registerAsListener(leaderboard);
    }
    
    /**
     * Listens at the leaderboard for {@link TrackedRace}s being connected to / disconnected from race columns. Whenever this
     * happens, the listener structure that uses {@link CacheInvalidationListener}s to observe the individual tracked races
     * is updated accordingly. Calling this method for a <code>leaderboard</code> that is already observed has no effect.
     */
    private void registerAsListener(final Leaderboard leaderboard) {
        // only add as listener again if not yet added
        final boolean containsKey;
        LockUtil.lockForRead(scoreCorrectionAndCompetitorChangeListenersLock);
        try {
            containsKey = scoreCorrectionListeners.containsKey(leaderboard);
        } finally {
            LockUtil.unlockAfterRead(scoreCorrectionAndCompetitorChangeListenersLock);
        }
        if (!containsKey) {
            LockUtil.lockForWrite(scoreCorrectionAndCompetitorChangeListenersLock);
            try {
                for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    registerListener(leaderboard, trackedRace);
                }
                final CacheInvalidationUponScoreCorrectionListener scoreCorrectionListener = new CacheInvalidationUponScoreCorrectionListener(
                        leaderboard);
                final CacheInvalidationUponCompetitorChangeListener competitorChangeListener = new CacheInvalidationUponCompetitorChangeListener(
                        leaderboard);
                leaderboard.getScoreCorrection().addScoreCorrectionListener(scoreCorrectionListener);
                scoreCorrectionListeners.put(leaderboard, scoreCorrectionListener);
                competitorChangeListeners.put(leaderboard, competitorChangeListener);
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
                        removeFromCache(leaderboard); // removes all listeners from invalidationListenersPerLeaderboard and from their TrackedRaces
                    }

                    @Override
                    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn,
                            boolean firstColumnIsNonDiscardableCarryForward) {
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
                    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule,
                            ResultDiscardingRule newDiscardingRule) {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName,
                            String displayName) {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier,
                            RaceLogEvent event) {
                        removeFromCache(leaderboard);
                    }
                };
                leaderboard.addRaceColumnListener(raceColumnListener);
                synchronized (raceColumnListeners) {
                    raceColumnListeners.put(leaderboard, raceColumnListener);
                }
            } finally {
                LockUtil.unlockAfterWrite(scoreCorrectionAndCompetitorChangeListenersLock);
            }
        }
    }

    private void registerListener(final Leaderboard leaderboard, TrackedRace trackedRace) {
        ConcurrentHashMap<TrackedRace, Set<CacheInvalidationListener>> invalidationListeners;
        final CacheInvalidationListener listener;
        listener = new CacheInvalidationListener(leaderboard, trackedRace);
        trackedRace.addListener(listener);
        invalidationListeners = invalidationListenersPerLeaderboard.get(leaderboard);
        if (invalidationListeners == null) {
            invalidationListeners = new ConcurrentHashMap<TrackedRace, Set<CacheInvalidationListener>>();
            invalidationListenersPerLeaderboard.put(leaderboard, invalidationListeners);
        }
        Set<CacheInvalidationListener> listeners = invalidationListeners.get(trackedRace);
        if (listeners == null) {
            listeners = Collections.synchronizedSet(new HashSet<CacheInvalidationListener>());
            invalidationListeners.put(trackedRace, listeners);
        }
        listeners.add(listener);
    }
}
