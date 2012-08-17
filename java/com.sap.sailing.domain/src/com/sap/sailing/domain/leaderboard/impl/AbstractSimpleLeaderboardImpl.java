package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractSimpleLeaderboardImpl implements Leaderboard {
    private static final long serialVersionUID = 330156778603279333L;

    static final Double DOUBLE_0 = new Double(0);

    /**
     * The factor by which a medal race score is multiplied in the overall point scheme
     */
    static final double MEDAL_RACE_FACTOR = 2.0;

    private  final SettableScoreCorrection scoreCorrection;

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

    protected Set<RaceColumnListener> raceColumnListeners;

    /**
     * A leaderboard entry representing a snapshot of a cell at a given time point for a single race/competitor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    class EntryImpl implements Entry {
        private final Callable<Integer> trackedRankProvider;
        private final Double netPoints;
        private final boolean isNetPointsCorrected;
        private final Double totalPoints;
        private final MaxPointsReason maxPointsReason;
        private final boolean discarded;
        private final Fleet fleet;

        private EntryImpl(Callable<Integer> trackedRankProvider, Double netPoints, boolean isNetPointsCorrected, Double totalPoints,
                MaxPointsReason maxPointsReason, boolean discarded, Fleet fleet) {
            super();
            this.trackedRankProvider = trackedRankProvider;
            this.netPoints = netPoints;
            this.isNetPointsCorrected = isNetPointsCorrected;
            this.totalPoints = totalPoints;
            this.maxPointsReason = maxPointsReason;
            this.discarded = discarded;
            this.fleet = fleet;
        }
        @Override
        public int getTrackedRank() {
            try {
                return trackedRankProvider.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public Double getNetPoints() {
            return netPoints;
        }
        @Override
        public boolean isNetPointsCorrected() {
            return isNetPointsCorrected;
        }
        @Override
        public Double getTotalPoints() {
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

    public AbstractSimpleLeaderboardImpl(SettableScoreCorrection scoreCorrection,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        this.carriedPoints = new HashMap<Competitor, Double>();
        this.scoreCorrection = scoreCorrection;
        this.displayNames = new HashMap<Competitor, String>();
        this.resultDiscardingRule = resultDiscardingRule;
    }

    @Override
    public SettableScoreCorrection getScoreCorrection() {
        return scoreCorrection;
    }

    @Override
    public String getDisplayName(Competitor competitor) {
        return displayNames.get(competitor);
    }
    
    @Override
    public ThresholdBasedResultDiscardingRule getResultDiscardingRule() {
        return resultDiscardingRule;
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
    public void setDisplayName(Competitor competitor, String displayName) {
        displayNames.put(competitor, displayName);
    }

    @Override
    public void setResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        this.resultDiscardingRule = discardingRule;
    }

    @Override
    public Double getNetPoints(final Competitor competitor, final RaceColumn raceColumn, final TimePoint timePoint) throws NoWindException {
        return getScoreCorrection().getCorrectedScore(
                new Callable<Integer>() {
                    public Integer call() throws NoWindException {
                        return getTrackedRank(competitor, raceColumn, timePoint);
                    }
                }, competitor,
                raceColumn, timePoint, Util.size(getCompetitors()), getScoringScheme()).getCorrectedScore();
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getScoreCorrection().getMaxPointsReason(competitor, raceColumn);
    }

    @Override
    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return !raceColumn.isMedalRace() && getMaxPointsReason(competitor, raceColumn, timePoint).isDiscardable()
                && getResultDiscardingRule().getDiscardedRaceColumns(competitor, this, timePoint).contains(
                        raceColumn);
    }

    @Override
    public Double getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        Double result;
        if (isDiscarded(competitor, raceColumn, timePoint)) {
            result = 0.0;
        } else {
            final Double netPoints = getNetPoints(competitor, raceColumn, timePoint);
            if (netPoints == null) {
                result = null;
            } else {
                result = (raceColumn.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0) * netPoints;
            }
        }
        return result;
    }

    @Override
    public Double getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        double result = getCarriedPoints(competitor);
        for (RaceColumn r : getRaceColumns()) {
            final Double totalPoints = getTotalPoints(competitor, r, timePoint);
            if (totalPoints != null) {
                result += totalPoints;
            }
        }
        return result;
    }

    /**
     * All competitors with non-<code>null</code> net points are added to the result which is then sorted by net points in ascending
     * order. The fleet is the primary ordering criterion, followed by the net points.
     */
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        final Map<Competitor, Pair<Double, Fleet>> netPointsAndFleet = new HashMap<Competitor, Pair<Double, Fleet>>();
        for (Competitor competitor : getCompetitors()) {
            Double netPoints = getNetPoints(competitor, raceColumn, timePoint);
            if (netPoints != null) {
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
                        comparisonResult = getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false).compare(
                                netPointsAndFleet.get(o1).getA(), netPointsAndFleet.get(o2).getA());
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
        return new LeaderboardTotalRankComparator(this, timePoint, getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false));
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
    public void addRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.add(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        raceColumnListeners.remove(listener);
    }

    protected void notifyListenersAboutTrackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceLinked(raceColumn, fleet, trackedRace);
        }
    }

    protected void notifyListenersAboutTrackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : raceColumnListeners) {
            listener.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
        }
    }

    @Override
    public Entry getEntry(final Competitor competitor, final RaceColumn race, final TimePoint timePoint) throws NoWindException {
        Callable<Integer> trackedRankProvider = new Callable<Integer>() {
            public Integer call() throws NoWindException {
                return getTrackedRank(competitor, race, timePoint);
            }
        };
        final Result correctedResults = getScoreCorrection().getCorrectedScore(trackedRankProvider, competitor, race,
                timePoint, Util.size(getCompetitors()), getScoringScheme());
        boolean discarded = isDiscarded(competitor, race, timePoint);
        final Double correctedScore = correctedResults.getCorrectedScore();
        return new EntryImpl(trackedRankProvider, correctedScore, correctedResults.isCorrected(),
                discarded ? DOUBLE_0
                        : correctedScore == null ? null : Double.valueOf(correctedScore * (race.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0)),
                        correctedResults.getMaxPointsReason(), discarded, race.getFleetOfCompetitor(competitor));
    }

    @Override
    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(final TimePoint timePoint) throws NoWindException {
        Map<Pair<Competitor, RaceColumn>, Entry> result = new HashMap<Pair<Competitor, RaceColumn>, Entry>();
        Map<Competitor, Set<RaceColumn>> discardedRaces = new HashMap<Competitor, Set<RaceColumn>>();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            for (final Competitor competitor : getCompetitors()) {
                Callable<Integer> trackedRankProvider = new Callable<Integer>() {
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
                Result correctedResults = getScoreCorrection().getCorrectedScore(trackedRankProvider, competitor, raceColumn,
                        timePoint, Util.size(getCompetitors()), getScoringScheme());
                Set<RaceColumn> discardedRacesForCompetitor = discardedRaces.get(competitor);
                if (discardedRacesForCompetitor == null) {
                    discardedRacesForCompetitor = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this, timePoint);
                    discardedRaces.put(competitor, discardedRacesForCompetitor);
                }
                boolean discarded = discardedRacesForCompetitor.contains(raceColumn);
                final Double correctedScore = correctedResults.getCorrectedScore();
                Entry entry = new EntryImpl(trackedRankProvider, correctedScore,
                        correctedResults.isCorrected(), discarded ? DOUBLE_0 : (correctedScore==null?null:
                                Double.valueOf((correctedScore * (raceColumn.isMedalRace() ? MEDAL_RACE_FACTOR : 1.0)))),
                                correctedResults.getMaxPointsReason(), discarded,
                                raceColumn.getFleetOfCompetitor(competitor));
                result.put(new Pair<Competitor, RaceColumn>(competitor, raceColumn), entry);
            }
        }
        return result;
    }
}
