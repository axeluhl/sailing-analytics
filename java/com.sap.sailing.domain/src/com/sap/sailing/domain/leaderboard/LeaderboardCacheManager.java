package com.sap.sailing.domain.leaderboard;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatChangeListener;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorChangeListener;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardChangeListener;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.WithNationality;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardCache;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
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
    private final WeakHashMap<Leaderboard, NameChangeListener> nameChangeListeners;
    private final NamedReentrantReadWriteLock scoreCorrectionAndCompetitorAndNameChangeListenersLock;
    private final WeakHashMap<Leaderboard, CacheInvalidationUponCompetitorChangeListener> competitorChangeListeners;
    private final WeakHashMap<Leaderboard, CacheInvalidationUponBoatChangeListener> boatChangeListeners;
    private final ConcurrentWeakHashMap<Leaderboard, ConcurrentMap<TrackedRace, Set<CacheInvalidationListener>>> invalidationListenersPerLeaderboard;
    private final WeakHashMap<Leaderboard, RaceColumnListener> raceColumnListeners;
    
    private class NameChangeListener implements LeaderboardChangeListener {
        private final Leaderboard leaderboard;
        
        public NameChangeListener(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
        }

        @Override
        public void nameChanged(String oldName, String newName) {
            removeFromCache(leaderboard);
        }

        @Override
        public void displayNameChanged(String oldDisplayName, String newDisplayName) {
            removeFromCache(leaderboard);
        }
    }
    
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
    
    private class CacheInvalidationUponBoatChangeListener implements BoatChangeListener {
        private static final long serialVersionUID = -8117073993497852698L;
        private final Leaderboard leaderboard;
        private final Set<Boat> boatsToStopObserving;

        public CacheInvalidationUponBoatChangeListener(Leaderboard leaderboard) {
            super();
            this.leaderboard = leaderboard;
            boatsToStopObserving = registerBoatListeners();
        }

        public Set<Boat> registerBoatListeners() {
            Set<Boat> boatsToStopObserving = new HashSet<>();
            for (Boat boat : leaderboard.getAllBoats()) {
                boat.addBoatChangeListener(this); // start observing boat
                boatsToStopObserving.add(boat);
            }
            return boatsToStopObserving;
        }

        public synchronized void removeFromAllBoats() {
            for (Boat boat : boatsToStopObserving) {
                boat.removeBoatChangeListener(this);
            }
        }

        @Override
        public void nameChanged(String oldName, String newName) {
            removeFromCache(leaderboard);
        }

        @Override
        public void colorChanged(Color oldColor, Color newColor) {
            removeFromCache(leaderboard);
        }

        @Override
        public void sailIdChanged(String oldSailId, String newSailId) {
            removeFromCache(leaderboard);
        }
    }
    
    private class CacheInvalidationUponCompetitorChangeListener implements CompetitorChangeListener {
        private static final long serialVersionUID = 8308312509904366143L;
        private final Leaderboard leaderboard;
        private final Set<Competitor> competitorsToStopObserving;
        
        /**
         * Registers this listener as listener for all of the <code>leaderboard</code>'s current
         * {@link Leaderboard#getCompetitors() competitors}
         */
        public CacheInvalidationUponCompetitorChangeListener(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
            competitorsToStopObserving = registerCompetitorListeners();
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

        @Override
        public void shortNameChanged(String oldShortName, String newShortName) {
            removeFromCache(leaderboard);
        }
        
        public Set<Competitor> registerCompetitorListeners() {
            Set<Competitor> competitorsToStopObserving = new HashSet<>();
            for (Competitor competitor : leaderboard.getCompetitors()) {
                competitor.addCompetitorChangeListener(this); // start observing competitor
                competitorsToStopObserving.add(competitor);
            }
            return competitorsToStopObserving;
        }

        public synchronized void removeFromAllCompetitors() {
            for (Competitor competitor : competitorsToStopObserving) {
                competitor.removeCompetitorChangeListener(this);
            }
        }

        @Override
        public void emailChanged(String oldEmail, String newEmail) {
            //ignore (email not shown in leaderboard)
        }

        @Override
        public void searchTagChanged(String oldSearchTag, String newSearchTag) {
            //ignore (email not shown in leaderboard)
        }

        @Override
        public void flagImageChanged(URI oldFlagImageURL, URI newFlagImageURL) {
            removeFromCache(leaderboard);
        }

        @Override
        public void timeOnTimeFactorChanged(Double oldTimeOnTimeFactor, Double newTimeOnTimeFactor) {
            removeFromCache(leaderboard);
        }

        @Override
        public void timeOnDistanceAllowancePerNauticalMileChanged(Duration oldTimeOnDistanceAllowancePerNauticalMile,
                Duration newTimeOnDistanceAllowancePerNauticalMile) {
            removeFromCache(leaderboard);
        }
    }
    
    private class CacheInvalidationUponScoreCorrectionListener implements ScoreCorrectionListener {
        private final Leaderboard leaderboard;
        
        public CacheInvalidationUponScoreCorrectionListener(Leaderboard leaderboard) {
            this.leaderboard = leaderboard;
        }

        @Override
        public void correctedScoreChanged(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore) {
            removeFromCache(leaderboard);
        }

        @Override
        public void maxPointsReasonChanged(Competitor competitor, RaceColumn raceColumn,
                MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason) {
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

        @Override
        public void timePointOfLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity,
                TimePoint newTimePointOfLastCorrectionsValidity) {
            removeFromCache(leaderboard);
        }

        @Override
        public void commentChanged(String oldComment, String newComment) {
            removeFromCache(leaderboard);
        }
    }

    private final LeaderboardCache leaderboardCache;
    
    public LeaderboardCacheManager(LeaderboardCache leaderboardCache) {
        this.leaderboardCache = leaderboardCache;
        this.invalidationListenersPerLeaderboard = new ConcurrentWeakHashMap<>();
        this.raceColumnListeners = new WeakHashMap<Leaderboard, RaceColumnListener>();
        this.scoreCorrectionListeners = new WeakHashMap<>();
        this.nameChangeListeners = new WeakHashMap<>();
        this.scoreCorrectionAndCompetitorAndNameChangeListenersLock = new NamedReentrantReadWriteLock(
                "Score correction and competitor and name change listeners", /* fair */false);
        this.competitorChangeListeners = new WeakHashMap<>();
        this.boatChangeListeners = new WeakHashMap<>();
    }
    
    private void removeFromCache(Leaderboard leaderboard) {
        ConcurrentMap<TrackedRace, Set<CacheInvalidationListener>> listenersMap = invalidationListenersPerLeaderboard.remove(leaderboard);
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
        LockUtil.lockForWrite(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
        final CacheInvalidationUponScoreCorrectionListener removedScoreCorrectionListener;
        final CacheInvalidationUponCompetitorChangeListener removedCompetitorChangeListener;
        final CacheInvalidationUponBoatChangeListener removedBoatChangeListener;
        final NameChangeListener removedNameChangeListener;
        try {
            removedScoreCorrectionListener = scoreCorrectionListeners.remove(leaderboard);
            removedCompetitorChangeListener = competitorChangeListeners.remove(leaderboard);
            removedBoatChangeListener = boatChangeListeners.remove(leaderboard);
            removedNameChangeListener = nameChangeListeners.remove(leaderboard);
        } finally {
            LockUtil.unlockAfterWrite(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
        }
        if (removedScoreCorrectionListener != null) {
            leaderboard.getScoreCorrection().removeScoreCorrectionListener(removedScoreCorrectionListener);
        }
        if (removedCompetitorChangeListener != null) {
            removedCompetitorChangeListener.removeFromAllCompetitors();
        }
        if (removedBoatChangeListener != null) {
            removedBoatChangeListener.removeFromAllBoats();
        }
        if (removedNameChangeListener != null) {
            leaderboard.removeLeaderboardChangeListener(removedNameChangeListener);
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
        LockUtil.lockForRead(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
        try {
            containsKey = scoreCorrectionListeners.containsKey(leaderboard);
        } finally {
            LockUtil.unlockAfterRead(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
        }
        if (!containsKey) {
            LockUtil.lockForWrite(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
            try {
                for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    registerListener(leaderboard, trackedRace);
                }
                final CacheInvalidationUponScoreCorrectionListener scoreCorrectionListener = new CacheInvalidationUponScoreCorrectionListener(leaderboard);
                final CacheInvalidationUponCompetitorChangeListener competitorChangeListener = new CacheInvalidationUponCompetitorChangeListener(leaderboard);
                final CacheInvalidationUponBoatChangeListener boatChangeListener = new CacheInvalidationUponBoatChangeListener(leaderboard);
                final NameChangeListener nameChangeListener = new NameChangeListener(leaderboard);
                leaderboard.addScoreCorrectionListener(scoreCorrectionListener);
                leaderboard.addLeaderboardChangeListener(nameChangeListener);
                scoreCorrectionListeners.put(leaderboard, scoreCorrectionListener);
                competitorChangeListeners.put(leaderboard, competitorChangeListener);
                boatChangeListeners.put(leaderboard, boatChangeListener);
                nameChangeListeners.put(leaderboard, nameChangeListener);
                final RaceColumnListener raceColumnListener = new RaceColumnListenerWithDefaultAction() {
                    private static final long serialVersionUID = 8165124797028386317L;

                    @Override
                    public void defaultAction() {
                        removeFromCache(leaderboard);
                    }

                    @Override
                    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
                        defaultAction();
                        registerListener(leaderboard, trackedRace);
                    }

                    /**
                     * This listener must not be serialized. See also bug 952.
                     */
                    @Override
                    public boolean isTransient() {
                        return true;
                    }
                };
                leaderboard.addRaceColumnListener(raceColumnListener);
                synchronized (raceColumnListeners) {
                    raceColumnListeners.put(leaderboard, raceColumnListener);
                }
            } finally {
                LockUtil.unlockAfterWrite(scoreCorrectionAndCompetitorAndNameChangeListenersLock);
            }
        }
    }

    private void registerListener(final Leaderboard leaderboard, TrackedRace trackedRace) {
        ConcurrentMap<TrackedRace, Set<CacheInvalidationListener>> invalidationListeners;
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
