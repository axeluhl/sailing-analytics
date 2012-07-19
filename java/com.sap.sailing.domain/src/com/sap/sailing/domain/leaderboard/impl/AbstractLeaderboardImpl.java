package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Abstract leaderboard implementation that already knows about carried points, competitor display name re-keying,
 * score corrections and result discarding rules. It manages a set of registered {@link RaceColumnListener}s and
 * is itself one. All events received this way are forwarded to all {@link RaceColumnListener}s subscribed. This can
 * be used to subscribe a concrete leaderboard implementation to the data structure providing the actual race columns
 * in order to be notified whenever the set of {@link TrackedRace}s attached to the leaderboard changes.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractLeaderboardImpl implements Leaderboard, RaceColumnListener {
    private static final long serialVersionUID = -328091952760083438L;

    /**
     * The factor by which a medal race score is multiplied in the overall point scheme
     */
    private static final double MEDAL_RACE_FACTOR = 2.0;
    
    private final SettableScoreCorrection scoreCorrection;
    private ThresholdBasedResultDiscardingRule resultDiscardingRule;
    
    /**
     * The optional display name mappings for competitors. This allows a user to override the tracking-provided
     * competitor names for display in a leaderboard.
     */
    private final Map<Competitor, String> displayNames;
    
    /**
     * Backs the {@link #getCarriedPoints(Competitor)} API with data. Can be used to prime this leaderboard
     * with aggregated results of races not tracked / displayed by this leaderboard in detail. The points
     * provided by this map are considered by {@link #getTotalPoints(Competitor, TimePoint)}.
     */
    private final Map<Competitor, Double> carriedPoints;

    private final Comparator<Double> scoreComparator;
    
    private Set<RaceColumnListener> raceColumnListeners;
    
    /**
     * Cache for the combined competitors of this leaderboard; taken from the {@link TrackedRace#getRace() races of the
     * tracked races} associated with this leaderboard. Updated when the set of tracked races changes.
     */
    private transient Iterable<Competitor> competitorsCache;

    /**
     * A leaderboard entry representing a snapshot of a cell at a given time point for a single race/competitor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class EntryImpl implements Entry {
        private final Callable<Integer> trackedPoints;
        private final double netPoints;
        private final boolean isNetPointsCorrected;
        private final double totalPoints;
        private final MaxPointsReason maxPointsReason;
        private final boolean discarded;
        private final Fleet fleet;

        private EntryImpl(Callable<Integer> trackedPoints, double netPoints, boolean isNetPointsCorrected, double totalPoints,
                MaxPointsReason maxPointsReason, boolean discarded, Fleet fleet) {
            super();
            this.trackedPoints = trackedPoints;
            this.netPoints = netPoints;
            this.isNetPointsCorrected = isNetPointsCorrected;
            this.totalPoints = totalPoints;
            this.maxPointsReason = maxPointsReason;
            this.discarded = discarded;
            this.fleet = fleet;
        }
        @Override
        public int getTrackedPoints() {
            try {
                return trackedPoints.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public double getNetPoints() {
            return netPoints;
        }
        @Override
        public boolean isNetPointsCorrected() {
            return isNetPointsCorrected;
        }
        @Override
        public double getTotalPoints() {
            return totalPoints;
        }
        @Override
        public MaxPointsReason getMaxPointsReason() {
            return maxPointsReason;
        }
        @Override
        public boolean isDiscarded() {
            return discarded;
        }
        @Override
        public Fleet getFleet() {
            return fleet;
        }
    }

    /**
     * @param scoreComparator the comparator to use to compare basic scores, such as net points
     * @param name must not be <code>null</code>
     */
    public AbstractLeaderboardImpl(SettableScoreCorrection scoreCorrection,
            ThresholdBasedResultDiscardingRule resultDiscardingRule, Comparator<Double> scoreComparator) {
        this.carriedPoints = new HashMap<Competitor, Double>();
        this.scoreCorrection = scoreCorrection;
        this.displayNames = new HashMap<Competitor, String>();
        this.resultDiscardingRule = resultDiscardingRule;
        this.scoreComparator = scoreComparator;
        this.raceColumnListeners = new HashSet<RaceColumnListener>();
    }
    
    @Override
    public Fleet getFleet(String fleetName) {
        for (RaceColumn raceColumn : getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                if (fleet.getName().equals(fleetName)) {
                    return fleet;
                }
            }
        }
        return null;
    }

    @Override
    public RaceColumn getRaceColumnByName(String columnName) {
        RaceColumn result = null;
        for (RaceColumn r : getRaceColumns()) {
            if (r.getName().equals(columnName)) {
                result = r;
                break;
            }
        }
        return result;
    }
    
    private Iterable<TrackedRace> getTrackedRaces() {
        Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
        for (RaceColumn r : getRaceColumns()) {
            for (Fleet fleet : r.getFleets()) {
                TrackedRace trackedRace = r.getTrackedRace(fleet);
                if (trackedRace != null) {
                    trackedRaces.add(trackedRace);
                }
            }
        }
        return Collections.unmodifiableSet(trackedRaces);
    }

    @Override
    public synchronized Iterable<Competitor> getCompetitors() {
        if (competitorsCache == null) {
            Set<Competitor> result = new HashSet<Competitor>();
            for (TrackedRace r : getTrackedRaces()) {
                for (Competitor c : r.getRace().getCompetitors()) {
                    result.add(c);
                }
            }
            competitorsCache = result;
        }
        return competitorsCache;
    }
    
    @Override
    public Competitor getCompetitorByName(String competitorName) {
        for (Competitor competitor : getCompetitors()) {
            if (competitor.getName().equals(competitorName)) {
                return competitor;
            }
        }
        return null;
    }
    
    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        for (Competitor competitor : getCompetitors()) {
            if (competitor.getId().toString().equals(idAsString)) {
                return competitor;
            }
        }
        return null;
    }

    @Override
    public SettableScoreCorrection getScoreCorrection() {
        return scoreCorrection;
    }
    
    @Override
    public ThresholdBasedResultDiscardingRule getResultDiscardingRule() {
        return resultDiscardingRule;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        final TrackedRace trackedRace = race.getTrackedRace(competitor);
        return trackedRace == null ? 0
                : trackedRace.hasStarted(timePoint) ? improveByDisqualificationsOfBetterRankedCompetitors(race, trackedRace, timePoint, trackedRace
                        .getRank(competitor, timePoint)) : 0;
    }

    /**
     * Per competitor disqualified ({@link ScoreCorrection} has a {@link MaxPointsReason} for the competitor), all
     * competitors ranked worse by the tracking system need to have their rank corrected by one.
     * @param trackedRace the race to which the rank refers; look for disqualifications / max points reasons in this column
     * @param timePoint
     *            time point at which to consider disqualifications (not used yet because currently we don't remember
     *            <em>when</em> a competitor was disqualified)
     * @param rank a competitors rank according to the tracking system
     * 
     * @return the unmodified <code>rank</code> if no disqualifications for better-ranked competitors exist for <code>race</code>,
     * or otherwise a rank improved (lowered) by the number of disqualifications of competitors whose tracked rank is better (lower)
     * than <code>rank</code>.
     */
    private int improveByDisqualificationsOfBetterRankedCompetitors(RaceColumn raceColumn, TrackedRace trackedRace, TimePoint timePoint, int rank) {
        int correctedRank = rank;
        List<Competitor> competitorsFromBestToWorst = trackedRace.getCompetitorsFromBestToWorst(timePoint);
        int betterCompetitorRank=1;
        Iterator<Competitor> ci = competitorsFromBestToWorst.iterator();
        while (betterCompetitorRank < rank && ci.hasNext()) {
            Competitor betterTrackedCompetitor = ci.next();
            MaxPointsReason maxPointsReasonForBetterCompetitor = getScoreCorrection().getMaxPointsReason(betterTrackedCompetitor, raceColumn);
            if (maxPointsReasonForBetterCompetitor != null && maxPointsReasonForBetterCompetitor != MaxPointsReason.NONE) {
                correctedRank--;
            }
            betterCompetitorRank++;
        }
        return correctedRank;
    }

    @Override
    public double getNetPoints(final Competitor competitor, final RaceColumn raceColumn, final TimePoint timePoint) throws NoWindException {
        return getScoreCorrection().getCorrectedScore(
                new Callable<Integer>() {
                    public Integer call() throws NoWindException {
                        return getTrackedRank(competitor, raceColumn, timePoint);
                    }
                }, competitor,
                raceColumn, timePoint, Util.size(getCompetitors())).getCorrectedScore();
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return raceColumn.getTrackedRace(competitor) == null ? MaxPointsReason.NONE : getScoreCorrection()
                .getMaxPointsReason(competitor, raceColumn);
    }
    
    @Override
    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return !raceColumn.isMedalRace() && getMaxPointsReason(competitor, raceColumn, timePoint).isDiscardable()
                && getResultDiscardingRule().getDiscardedRaceColumns(competitor, this, timePoint).contains(
                        raceColumn);
    }

    @Override
    public double getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        return isDiscarded(competitor, raceColumn, timePoint) ?
                0.0 :
                (raceColumn.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0) * getNetPoints(competitor, raceColumn, timePoint);
    }
    
    @Override
    public double getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        double result = getCarriedPoints(competitor);
        for (RaceColumn r : getRaceColumns()) {
            result += getTotalPoints(competitor, r, timePoint);
        }
        return result;
    }

    @Override
    public Entry getEntry(final Competitor competitor, final RaceColumn race, final TimePoint timePoint) throws NoWindException {
        Callable<Integer> trackedPoints = new Callable<Integer>() {
            public Integer call() throws NoWindException {
                return getTrackedRank(competitor, race, timePoint);
            }
        };
        final Result correctedResults = getScoreCorrection().getCorrectedScore(trackedPoints, competitor, race,
                timePoint, Util.size(getCompetitors()));
        boolean discarded = isDiscarded(competitor, race, timePoint);
        return new EntryImpl(trackedPoints, correctedResults.getCorrectedScore(), correctedResults.isCorrected(),
                discarded ? 0
                        : correctedResults.getCorrectedScore() * (race.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0),
                        correctedResults.getMaxPointsReason(), discarded, race.getFleetOfCompetitor(competitor));
    }
    
    @Override
    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(final TimePoint timePoint) throws NoWindException {
        Map<Pair<Competitor, RaceColumn>, Entry> result = new HashMap<Pair<Competitor, RaceColumn>, Entry>();
        Map<Competitor, Set<RaceColumn>> discardedRaces = new HashMap<Competitor, Set<RaceColumn>>();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            for (final Competitor competitor : getCompetitors()) {
                Callable<Integer> trackedPoints = new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        int trackedPoints;
                        final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                        if (trackedRace != null && trackedRace.hasStarted(timePoint)) {
                            trackedPoints = trackedRace.getRank(competitor, timePoint);
                        } else {
                            trackedPoints = 0;
                        }
                        return trackedPoints;
                    }
                };
                Result correctedResults = getScoreCorrection().getCorrectedScore(trackedPoints, competitor, raceColumn,
                        timePoint, Util.size(getCompetitors()));
                Set<RaceColumn> discardedRacesForCompetitor = discardedRaces.get(competitor);
                if (discardedRacesForCompetitor == null) {
                    discardedRacesForCompetitor = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this, timePoint);
                    discardedRaces.put(competitor, discardedRacesForCompetitor);
                }
                boolean discarded = discardedRacesForCompetitor.contains(raceColumn);
                Entry entry = new EntryImpl(trackedPoints, correctedResults.getCorrectedScore(),
                        correctedResults.isCorrected(), discarded ? 0 : correctedResults.getCorrectedScore()
                                * (raceColumn.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0), correctedResults.getMaxPointsReason(), discarded,
                                raceColumn.getFleetOfCompetitor(competitor));
                result.put(new Pair<Competitor, RaceColumn>(competitor, raceColumn), entry);
            }
        }
        return result;
    }

    @Override
    public void setCarriedPoints(Competitor competitor, double carriedPoints) {
        this.carriedPoints.put(competitor, carriedPoints);
    }

    @Override
    public double getCarriedPoints(Competitor competitor) {
        Double result = carriedPoints.get(competitor);
        return result == null ? 0 : result;
    }

    @Override
    public void unsetCarriedPoints(Competitor competitor) {
        carriedPoints.remove(competitor);
    }

    @Override
    public boolean hasCarriedPoints() {
        return !carriedPoints.isEmpty();
    }
    
    @Override
    public boolean hasCarriedPoints(Competitor competitor) {
        return carriedPoints.containsKey(competitor);
    }

    @Override
    public boolean considerForDiscarding(RaceColumn raceColumn, TimePoint timePoint) {
        boolean result = getScoreCorrection().hasCorrectionFor(raceColumn);
        if (!result && !raceColumn.isMedalRace()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null && trackedRace.hasStarted(timePoint)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getDisplayName(Competitor competitor) {
        return displayNames.get(competitor);
    }
    
    @Override
    public void setDisplayName(Competitor competitor, String displayName) {
        displayNames.put(competitor, displayName);
    }

    @Override
    public void setResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        this.resultDiscardingRule = discardingRule;
    }

    /**
     * All competitors with non-zero net points are added to the result which is then sorted by net points in ascending
     * order. The fleet is the primary ordering criterion, followed by the net points.
     */
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        final Map<Competitor, Pair<Double, Fleet>> netPointsAndFleet = new HashMap<Competitor, Pair<Double, Fleet>>();
        for (Competitor competitor : getCompetitors()) {
            double netPoints = getNetPoints(competitor, raceColumn, timePoint);
            if (netPoints != 0) {
                netPointsAndFleet.put(competitor, new Pair<Double, Fleet>(netPoints, raceColumn.getFleetOfCompetitor(competitor)));
            }
        }
        List<Competitor> result = new ArrayList<Competitor>(netPointsAndFleet.keySet());
        Collections.sort(result, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                int comparisonResult;
                if (o1 == o2) {
                    comparisonResult = 0;
                } else {
                    final Fleet o1Fleet = netPointsAndFleet.get(o1).getB();
                    final Fleet o2Fleet = netPointsAndFleet.get(o2).getB();
                    if (o1Fleet == null) {
                        if (o2Fleet == null) {
                            comparisonResult = 0;
                        } else {
                            comparisonResult = 1; // o1 ranks "worse" because it doesn't have a fleet set while o2 has
                        }
                    } else {
                        if (o2Fleet == null) {
                            comparisonResult = -1; // o1 ranks "better" because it has a fleet set while o2 hasn't
                        } else {
                            comparisonResult = o1Fleet.compareTo(o2Fleet);
                        }
                    }
                    if (comparisonResult == 0) {
                        comparisonResult = scoreComparator.compare(netPointsAndFleet.get(o1).getA(), netPointsAndFleet.get(o2).getA());
                    }
                }
                return comparisonResult;
            }
        });
        return result;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) throws NoWindException {
        List<Competitor> result = new ArrayList<Competitor>();
        for (Competitor competitor : getCompetitors()) {
            result.add(competitor);
        }
        Collections.sort(result, getTotalRankComparator(timePoint));
        return result;
    }

    protected Comparator<? super Competitor> getTotalRankComparator(TimePoint timePoint) throws NoWindException {
        return new LeaderboardTotalRankComparator(this, timePoint, scoreComparator);
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.add(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.remove(listener);
    }
    
    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        competitorsCache = null;
        notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        competitorsCache = null;
        notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

    private void notifyListenersAboutTrackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceLinked(raceColumn, fleet, trackedRace);
        }
    }

    private void notifyListenersAboutTrackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
        }
    }

}
