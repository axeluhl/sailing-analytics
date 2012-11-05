package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.RaceColumnListeners;

/**
 * Base implementation for various types of leaderboards. The {@link RaceColumnListener} implementation forwards events
 * received to all {@link RaceColumnListener} subscribed with this leaderboard. To which objects this leaderboard subscribes
 * as {@link RaceColumnListener} is left to the concrete subclasses to implement, but the race columns seem like useful
 * candidates.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractSimpleLeaderboardImpl implements Leaderboard, RaceColumnListener {
    private static final long serialVersionUID = 330156778603279333L;

    static final Double DOUBLE_0 = new Double(0);

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

    private final RaceColumnListeners raceColumnListeners;
    
    /**
     * A synchronized set that manages the difference between {@link #getCompetitors()} and {@link #getAllCompetitors()}.
     */
    private final Set<Competitor> suppressedCompetitors;

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
        this.suppressedCompetitors = Collections.synchronizedSet(new HashSet<Competitor>());
        raceColumnListeners = new RaceColumnListeners();
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
        Double oldCarriedPoints = this.carriedPoints.put(competitor, carriedPoints);
        getScoreCorrection().notifyListenersAboutCarriedPointsChange(competitor, oldCarriedPoints, carriedPoints);
    }

    @Override
    public double getCarriedPoints(Competitor competitor) {
        Double result = carriedPoints.get(competitor);
        return result == null ? 0 : result;
    }

    @Override
    public void unsetCarriedPoints(Competitor competitor) {
        Double oldCarriedPoints = carriedPoints.remove(competitor);
        getScoreCorrection().notifyListenersAboutCarriedPointsChange(competitor, oldCarriedPoints, null);
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
                result = raceColumn.getFactor() * netPoints;
            }
        }
        return result;
    }

    @Override
    public Double getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        double result = getCarriedPoints(competitor);
        for (RaceColumn r : getRaceColumns()) {
            if (getScoringScheme().isValidInTotalScore(this, r, timePoint)) {
                final Double totalPoints = getTotalPoints(competitor, r, timePoint);
                if (totalPoints != null) {
                    result += totalPoints;
                }
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
        return new LeaderboardTotalRankComparator(this, timePoint, getScoringScheme(), /* nullScoresAreBetter */ false);
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
        for (Competitor competitor : getAllCompetitors()) {
            if (competitor.getName().equals(competitorName)) {
                return competitor;
            }
        }
        return null;
    }

    @Override
    public boolean countRaceForComparisonWithDiscardingThresholds(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        TrackedRace trackedRaceForCompetitorInColumn;
        return getScoringScheme().isValidInTotalScore(this, raceColumn, timePoint) && 
               (getScoreCorrection().isScoreCorrected(competitor, raceColumn) ||
                       ((trackedRaceForCompetitorInColumn=raceColumn.getTrackedRace(competitor)) != null &&
                        trackedRaceForCompetitorInColumn.hasStarted(timePoint)));
    }
    
    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().removeRaceColumnListener(listener);
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
                        : correctedScore == null ? null : Double.valueOf(correctedScore * race.getFactor()),
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
                        return getTrackedRank(competitor, raceColumn, timePoint);
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
                                Double.valueOf((correctedScore * raceColumn.getFactor()))),
                                correctedResults.getMaxPointsReason(), discarded,
                                raceColumn.getFleetOfCompetitor(competitor));
                result.put(new Pair<Competitor, RaceColumn>(competitor, raceColumn), entry);
            }
        }
        return result;
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }
    
    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        getRaceColumnListeners().notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }
    
    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    /**
     * A leaderboard will only accept the addition of a race column if the column's name is unique across the leaderboard.
     */
    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn newRaceColumn) {
        boolean result = true;
        for (RaceColumn raceColumn : getRaceColumns()) {
            if (raceColumn.getName().equals(newRaceColumn.getName())) {
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnAddedToContainer(raceColumn);
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnRemovedFromContainer(raceColumn);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    /**
     * Finds out the time point when any of the {@link Leaderboard#getTrackedRaces() tracked races currently attached to
     * the <code>leaderboard</code>} and the {@link Leaderboard#getScoreCorrection() score corrections} have last been
     * modified. If no tracked race is attached and no time-stamped score corrections have been applied to the leaderboard,
     * <code>null</code> is returned. The time point computed this way is a good choice for normalizing queries for later time
     * points in an attempt to achieve more cache hits.<p>
     * 
     * Note, however, that the result does not tell about structural changes to the leaderboard and therefore cannot be used
     * to determine the need for cache invalidation. For example, if a column is added to a leaderboard after the time point
     * returned by this method but that column's attached tracked race has finished before the time point returned by this method,
     * the result of this method won't change. Still, the contents of the leaderboard will change by a change in column structure.
     * A different means to determine the possibility of changes that happened to this leaderboard must be used for cache
     * management. Such a facility has to listen for score correction changes, tracked races being attached or detached and
     * the column structure changing.
     * 
     * @see TrackedRace#getTimePointOfNewestEvent()
     * @see SettableScoreCorrection#getTimePointOfLastCorrectionsValidity()
     */
    @Override
    public TimePoint getTimePointOfLatestModification() {
        TimePoint result = null;
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (result == null || (trackedRace.getTimePointOfNewestEvent() != null && trackedRace.getTimePointOfNewestEvent().after(result))) {
                result = trackedRace.getTimePointOfNewestEvent();
            }
        }
        TimePoint timePointOfLastScoreCorrection = getScoreCorrection().getTimePointOfLastCorrectionsValidity();
        if (timePointOfLastScoreCorrection != null && (result == null || timePointOfLastScoreCorrection.after(result))) {
            result = timePointOfLastScoreCorrection;
        }
        return result;
    }

    @Override
    public Pair<GPSFixMoving, Speed> getMaximumSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        Pair<GPSFixMoving, Speed> result = null;
        // TODO should we ensure that competitor participated in all race columns?
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                if (!markPassings.isEmpty()) {
                    TimePoint from = markPassings.first().getTimePoint();
                    TimePoint to;
                    if (timePoint.after(markPassings.last().getTimePoint()) &&
                            markPassings.last().getWaypoint() == trackedRace.getRace().getCourse().getLastWaypoint()) {
                        // stop counting when competitor finished the race
                        to = markPassings.last().getTimePoint();
                    } else {
                        to = timePoint;
                    }
                    Pair<GPSFixMoving, Speed> maxSpeed = trackedRace.getTrack(competitor).getMaximumSpeedOverGround(from, to);
                    if (result == null || result.getB() == null ||
                            (maxSpeed != null && maxSpeed.getB() != null && maxSpeed.getB().compareTo(result.getB()) > 0)) {
                        result = maxSpeed;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Long getTotalTimeSailedInLegTypeInMilliseconds(Competitor competitor, LegType legType, TimePoint timePoint) throws NoWindException {
        Long result = null;
        // TODO should we ensure that competitor participated in all race columns?
        outerLoop:
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                trackedRace.getRace().getCourse().lockForRead();
                try {
                    for (Leg leg : trackedRace.getRace().getCourse().getLegs()) {
                        TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, leg);
                        if (trackedLeg.hasStartedLeg(timePoint)) {
                            // find out leg type at the time the competitor started the leg
                            try {
                                LegType trackedLegType = trackedRace.getTrackedLeg(leg).getLegType(
                                        trackedLeg.getStartTime());
                                if (legType == trackedLegType) {
                                    Long millisecondsSpendOnDownwind = trackedLeg.getTimeInMilliSeconds(timePoint);
                                    if (millisecondsSpendOnDownwind != null) {
                                        if (result == null) {
                                            result = millisecondsSpendOnDownwind;
                                        } else {
                                            result += millisecondsSpendOnDownwind;
                                        }
                                    } else {
                                        // Although the competitor has started the leg, no value was produced. This
                                        // means that
                                        // the competitor didn't finish the leg before tracking ended. No useful value
                                        // can
                                        // be obtained for this competitor anymore.
                                        result = null;
                                        break outerLoop;
                                    }
                                }
                            } catch (NoWindException nwe) {
                                // without wind there is no leg type and hence there is no reasonable value for this:
                                result = null;
                                break outerLoop;
                            }
                        }
                    }
                } finally {
                    trackedRace.getRace().getCourse().unlockAfterRead();
                }
            }
        }
        return result;
    }

    @Override
    public Long getTotalTimeSailedInMilliseconds(Competitor competitor, TimePoint timePoint) {
        Long result = null;
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                if (!markPassings.isEmpty()) {
                    TimePoint from = trackedRace.getStartOfRace(); // start counting at race start, not when the competitor passed the line
                    if (!timePoint.before(from)) { // but only if the race started after timePoint
                        TimePoint to;
                        if (timePoint.after(markPassings.last().getTimePoint())
                                && markPassings.last().getWaypoint() == trackedRace.getRace().getCourse()
                                        .getLastWaypoint()) {
                            // stop counting when competitor finished the race
                            to = markPassings.last().getTimePoint();
                        } else {
                            if (trackedRace.getEndOfTracking() != null
                                    && timePoint.after(trackedRace.getEndOfTracking())) {
                                result = null; // race not finished until end of tracking; no reasonable value can be
                                               // computed for competitor
                                break;
                            } else {
                                to = timePoint;
                            }
                        }
                        long timeSpent = to.asMillis() - from.asMillis();
                        if (result == null) {
                            result = timeSpent;
                        } else {
                            result += timeSpent;
                        }
                    }
                }
            }
        }
        return result;
    }

    protected RaceColumnListeners getRaceColumnListeners() {
        return raceColumnListeners;
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<>();
        for (Competitor competitor : getAllCompetitors()) {
            if (!isSuppressed(competitor)) {
                result.add(competitor);
            }
        }
        return result;
    }

    /**
     * Defines the difference between {@link #getCompetitors} and {@link #getAllCompetitors}. If a competitor is suppressed,
     * it won't participate in the scoring process, particularly because it isn't considered by {@link #getCompetitorsFromBestToWorst(TimePoint)}
     * nor {@link #getCompetitorsFromBestToWorst(RaceColumn, TimePoint)}.
     */
    private boolean isSuppressed(Competitor competitor) {
        // no synchronization required because we use a synchronized set as implementation
        return suppressedCompetitors.contains(competitor);
    }
    
    @Override
    public Iterable<Competitor> getSuppressedCompetitors() {
        return new HashSet<Competitor>(suppressedCompetitors);
    }
    
    @Override
    public void setSuppressed(Competitor competitor, boolean suppressed) {
        // no synchronization required because we use a synchronized set as implementation
        if (suppressed) {
            suppressedCompetitors.add(competitor);
        } else {
            suppressedCompetitors.remove(competitor);
        }
        getScoreCorrection().notifyListenersAboutIsSuppressedChange(competitor, suppressed);
    }
}
