package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.RaceColumnListeners;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.ObscuringIterable;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Base implementation for various types of leaderboards. The {@link RaceColumnListener} implementation forwards events
 * received to all {@link RaceColumnListener} subscribed with this leaderboard. To which objects this leaderboard
 * subscribes as {@link RaceColumnListener} is left to the concrete subclasses to implement, but the race columns seem
 * like useful candidates.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractSimpleLeaderboardImpl extends AbstractLeaderboardWithCache
        implements Leaderboard, RaceColumnListener {
    private static final long serialVersionUID = 330156778603279333L;

    static final Double DOUBLE_0 = new Double(0);

    private final SettableScoreCorrection scoreCorrection;

    private ThresholdBasedResultDiscardingRule crossLeaderboardResultDiscardingRule;

    /**
     * The optional display name mappings for competitors. This allows a user to override the tracking-provided
     * competitor names for display in a leaderboard.
     */
    private final Map<Competitor, String> displayNames;

    /**
     * Backs the {@link #getCarriedPoints(Competitor)} API with data. Can be used to prime this leaderboard with
     * aggregated results of races not tracked / displayed by this leaderboard in detail. The points provided by this
     * map are considered by {@link #getNetPoints(Competitor, TimePoint)}.
     */
    private final Map<Competitor, Double> carriedPoints;

    /**
     * A set that manages the difference between {@link #getCompetitors()} and {@link #getAllCompetitors()}. Access is
     * controlled by the {@link #suppressedCompetitorsLock} lock.
     */
    private final Set<Competitor> suppressedCompetitors;
    private final NamedReentrantReadWriteLock suppressedCompetitorsLock;

    private final RaceColumnListeners raceColumnListeners;

    /**
     * A leaderboard entry representing a snapshot of a cell at a given time point for a single race/competitor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    class EntryImpl implements Entry {
        private final Callable<Integer> trackedRankProvider;
        private final Double totalPoints;
        private final Callable<Double> totalPointsUncorrectedProvider;
        private final boolean isTotalPointsCorrected;
        private final Double netPoints;
        private final MaxPointsReason maxPointsReason;
        private final boolean discarded;
        private final Fleet fleet;

        private EntryImpl(Callable<Integer> trackedRankProvider, Double totalPoints,
                Callable<Double> totalPointsUncorrectedProvider, boolean isTotalPointsCorrected, Double netPoints,
                MaxPointsReason maxPointsReason, boolean discarded, Fleet fleet) {
            super();
            this.trackedRankProvider = trackedRankProvider;
            this.totalPoints = totalPoints;
            this.totalPointsUncorrectedProvider = totalPointsUncorrectedProvider;
            this.isTotalPointsCorrected = isTotalPointsCorrected;
            this.netPoints = netPoints;
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
        public Double getTotalPoints() {
            return totalPoints;
        }

        @Override
        public boolean isTotalPointsCorrected() {
            return isTotalPointsCorrected;
        }

        @Override
        public Double getNetPoints() {
            return netPoints;
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

        @Override
        public Double getTotalPointsUncorrected() {
            try {
                return totalPointsUncorrectedProvider.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Computing the competitors can be a bit expensive, particularly if the fleet is large and there may be suppressed
     * competitors, and the leaderboard may be a meta-leaderboard that refers to other leaderboards which each have
     * several tracked races attached from where the competitors need to be retrieved. Ideally, the competitors list
     * would be cached, but that is again difficult because we would have to monitor all changes in all dependent
     * leaderboards and columns and tracked races properly.
     * <p>
     * 
     * As it turns out, one of the most frequent uses of the {@link AbstractSimpleLeaderboardImpl#getCompetitors}
     * competitors list is to determine their number which in turn is only required for high-point scoring systems and
     * for computing the default score for penalties. Again, the most frequently used low-point family of scoring
     * schemes does not require this number. Yet, the scoring scheme requires an argument for polymorphic use by those
     * that need it. Instead of computing it for each call, this interface lets us defer the actual calculation until
     * the point when it's really needed. Once asked, this object will cache the result. Therefore, a new one should be
     * constructed each time the number shall be computed.
     * 
     * @author Axel Uhl (D043530)
     * 
     */
    public class NumberOfCompetitorsFetcherImpl implements NumberOfCompetitorsInLeaderboardFetcher {
        private int numberOfCompetitors = -1;
        private int numberOfCompetitorsWithoutMaxPointReason = -1;

        @Override
        public int getNumberOfCompetitorsInLeaderboard() {
            if (numberOfCompetitors == -1) {
                numberOfCompetitors = Util.size(getCompetitors());
            }
            return numberOfCompetitors;
        }

        @Override
        public int getNumberOfCompetitorsWithoutMaxPointReason(RaceColumn column, TimePoint timePoint) {
            if (numberOfCompetitorsWithoutMaxPointReason == -1) {
                numberOfCompetitorsWithoutMaxPointReason = 0;
                for (Competitor competitor : getCompetitors()) {
                    MaxPointsReason maxPointReason = getScoreCorrection().getMaxPointsReason(competitor, column,
                            timePoint);
                    numberOfCompetitorsWithoutMaxPointReason += maxPointReason == MaxPointsReason.NONE ? 1 : 0;
                }
            }
            return numberOfCompetitorsWithoutMaxPointReason;
        }
    }

    public AbstractSimpleLeaderboardImpl(ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        this.carriedPoints = new HashMap<Competitor, Double>();
        this.raceColumnListeners = new RaceColumnListeners();
        this.scoreCorrection = createScoreCorrection();
        this.displayNames = new HashMap<Competitor, String>();
        this.crossLeaderboardResultDiscardingRule = resultDiscardingRule;
        this.suppressedCompetitors = new HashSet<Competitor>();
        this.suppressedCompetitorsLock = new NamedReentrantReadWriteLock("suppressedCompetitorsLock", /* fair */ false);
    }

    protected RaceColumnListeners getRaceColumnListeners() {
        return raceColumnListeners;
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(raceColumn, fleet, trackedRace);
        super.trackedRaceUnlinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        getRaceColumnListeners().notifyListenersAboutTrackedRaceLinked(raceColumn, fleet, trackedRace);
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
        getRaceColumnListeners().notifyListenersAboutIsMedalRaceChanged(raceColumn, newIsMedalRace);
    }

    @Override
    public void isFleetsCanRunInParallelChanged(RaceColumn raceColumn, boolean newIsFleetsCanRunInParallel) {
        getRaceColumnListeners().notifyListenersAboutIsFleetsCanRunInParallelChanged(raceColumn,
                newIsFleetsCanRunInParallel);
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
        getRaceColumnListeners().notifyListenersAboutIsStartsWithZeroScoreChanged(raceColumn, newIsStartsWithZeroScore);
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn,
            boolean firstColumnIsNonDiscardableCarryForward) {
        getRaceColumnListeners().notifyListenersAboutIsFirstColumnIsNonDiscardableCarryForwardChanged(raceColumn,
                firstColumnIsNonDiscardableCarryForward);
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
        getRaceColumnListeners().notifyListenersAboutHasSplitFleetContiguousScoringChanged(raceColumn,
                hasSplitFleetContiguousScoring);
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnMoved(raceColumn, newIndex);
    }

    @Override
    public void raceColumnNameChanged(RaceColumn raceColumn, String oldName, String newName) {
        getRaceColumnListeners().notifyListenersAboutRaceColumnNameChanged(raceColumn, oldName, newName);
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
        getRaceColumnListeners().notifyListenersAboutFactorChanged(raceColumn, oldFactor, newFactor);
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
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
        getRaceColumnListeners().notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName,
                displayName);
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule,
            ResultDiscardingRule newDiscardingRule) {
        getRaceColumnListeners().notifyListenersAboutResultDiscardingRuleChanged(oldDiscardingRule, newDiscardingRule);
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    /**
     * Produces the score correction object to use in this leaderboard. Used by the constructor. Subclasses may override
     * this method to create a more specific type of score correction. This implementation produces an object of type
     * {@link ScoreCorrectionImpl}.
     */
    protected SettableScoreCorrection createScoreCorrection() {
        return new ScoreCorrectionImpl(this);
    }

    @Override
    public SettableScoreCorrection getScoreCorrection() {
        return scoreCorrection;
    }

    @Override
    public void addScoreCorrectionListener(ScoreCorrectionListener listener) {
        getScoreCorrection().addScoreCorrectionListener(listener);
    }

    @Override
    public void removeScoreCorrectionListener(ScoreCorrectionListener listener) {
        getScoreCorrection().removeScoreCorrectionListener(listener);
    }

    @Override
    public String getDisplayName(Competitor competitor) {
        return displayNames.get(competitor);
    }

    @Override
    public ResultDiscardingRule getResultDiscardingRule() {
        return crossLeaderboardResultDiscardingRule;
    }

    @Override
    public void setCarriedPoints(Competitor competitor, double carriedPoints) {
        Double oldCarriedPoints = this.carriedPoints.put(competitor, carriedPoints);
        if (!Util.equalsWithNull(oldCarriedPoints, carriedPoints)) {
            getScoreCorrection().notifyListenersAboutCarriedPointsChange(competitor, oldCarriedPoints, carriedPoints);
        }
    }

    @Override
    public double getCarriedPoints(Competitor competitor) {
        Double result = carriedPoints.get(competitor);
        return result == null ? 0 : result;
    }

    @Override
    public Map<Competitor, Double> getCompetitorsForWhichThereAreCarriedPoints() {
        return Collections.unmodifiableMap(carriedPoints);
    }

    @Override
    public void unsetCarriedPoints(Competitor competitor) {
        Double oldCarriedPoints = carriedPoints.remove(competitor);
        if (oldCarriedPoints != null) {
            getScoreCorrection().notifyListenersAboutCarriedPointsChange(competitor, oldCarriedPoints, null);
        }
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
        String oldDisplayName = displayNames.get(competitor);
        displayNames.put(competitor, displayName);
        if (!Util.equalsWithNull(oldDisplayName, displayName)) {
            getRaceColumnListeners().notifyListenersAboutCompetitorDisplayNameChanged(competitor, oldDisplayName,
                    displayName);
        }
    }

    @Override
    public void setCrossLeaderboardResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        ResultDiscardingRule oldDiscardingRule = getResultDiscardingRule();
        this.crossLeaderboardResultDiscardingRule = discardingRule;
        getRaceColumnListeners().notifyListenersAboutResultDiscardingRuleChanged(oldDiscardingRule, discardingRule);
    }

    @Override
    public Double getTotalPoints(final Competitor competitor, final RaceColumn raceColumn, final TimePoint timePoint) {
        return getScoreCorrection().getCorrectedScore(() -> getTrackedRank(competitor, raceColumn, timePoint),
                competitor, raceColumn, this, timePoint, new NumberOfCompetitorsFetcherImpl(), getScoringScheme())
                .getCorrectedScore();
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getScoreCorrection().getMaxPointsReason(competitor, raceColumn, timePoint);
    }

    @Override
    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return isDiscarded(competitor, raceColumn, getRaceColumns(), timePoint);
    }

    private boolean isDiscarded(Competitor competitor, RaceColumn raceColumn,
            Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint) {
        final Set<RaceColumn> discardedRaceColumns = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this,
                raceColumnsToConsider, timePoint);
        return isDiscarded(competitor, raceColumn, timePoint, discardedRaceColumns);
    }

    /**
     * Same as {@link #isDiscarded(Competitor, RaceColumn, TimePoint)}, only that the set of discarded race columns can
     * be specified which is useful when net points are to be computed for more than one column for the same competitor
     * because then the calculation of discards (which requires looking at all columns) only needs to be done once and
     * not again for each column (which would lead to quadratic effort).
     * 
     * @param discardedRaceColumns
     *            expected to be the result of what we would get if we called {@link #getResultDiscardingRule()}.
     *            {@link ResultDiscardingRule#getDiscardedRaceColumns(Competitor, Leaderboard, Iterable, TimePoint)
     *            getDiscardedRaceColumns(competitor, this, raceColumnsToConsider, timePoint)}.
     */
    private boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint,
            final Set<RaceColumn> discardedRaceColumns) {
        return !raceColumn.isMedalRace() && getMaxPointsReason(competitor, raceColumn, timePoint).isDiscardable()
                && discardedRaceColumns.contains(raceColumn);
    }

    @Override
    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getNetPoints(competitor, raceColumn, getRaceColumns(), timePoint);
    }

    @Override
    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) {
        final Set<RaceColumn> discardedRaceColumns = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this,
                raceColumnsToConsider, timePoint);
        return getNetPoints(competitor, raceColumn, timePoint, discardedRaceColumns);
    }

    /**
     * Same as {@link #getNetPoints(Competitor, RaceColumn, Iterable, TimePoint)}, only that the set of discarded race
     * columns can be specified which is useful when net points are to be computed for more than one column for the same
     * competitor because then the calculation of discards (which requires looking at all columns) only needs to be done
     * once and not again for each column (which would lead to quadratic effort).
     * 
     * @param discardedRaceColumns
     *            expected to be the result of what we would get if we called {@link #getResultDiscardingRule()}.
     *            {@link ResultDiscardingRule#getDiscardedRaceColumns(Competitor, Leaderboard, Iterable, TimePoint)
     *            getDiscardedRaceColumns(competitor, this, raceColumnsToConsider, timePoint)}.
     */
    @Override
    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) {
        Double result;
        if (isDiscarded(competitor, raceColumn, timePoint, discardedRaceColumns)) {
            result = 0.0;
        } else {
            final Double totalPoints = getTotalPoints(competitor, raceColumn, timePoint);
            if (totalPoints == null) {
                result = null;
            } else {
                result = getScoringScheme().getScoreFactor(raceColumn) * totalPoints;
            }
        }
        return result;
    }

    @Override
    public Double getNetPoints(Competitor competitor, TimePoint timePoint) {
        return getNetPoints(competitor, getRaceColumns(), timePoint);
    }

    @Override
    public Double getNetPoints(Competitor competitor, final Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) {
        // when a column with isStartsWithZeroScore() is found, only reset score if the competitor scored in any race
        // from there on
        boolean needToResetScoreUponNextNonEmptyEntry = false;
        double result = getCarriedPoints(competitor);
        final Set<RaceColumn> discardedRaceColumns = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this,
                raceColumnsToConsider, timePoint);
        for (RaceColumn raceColumn : raceColumnsToConsider) {
            if (raceColumn.isStartsWithZeroScore()) {
                needToResetScoreUponNextNonEmptyEntry = true;
            }
            if (getScoringScheme().isValidInNetScore(this, raceColumn, competitor, timePoint)) {
                final Double netPoints = getNetPoints(competitor, raceColumn, timePoint, discardedRaceColumns);
                if (netPoints != null) {
                    if (needToResetScoreUponNextNonEmptyEntry) {
                        result = 0;
                        needToResetScoreUponNextNonEmptyEntry = false;
                    }
                    result += netPoints;
                }
            }
        }
        return result;
    }

    /**
     * All competitors with non-<code>null</code> total points are added to the result which is then sorted by total
     * points in ascending order. The fleet, if ordered, is the primary ordering criterion, followed by the total
     * points.
     */
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, TimePoint timePoint)
            throws NoWindException {
        final Map<Competitor, com.sap.sse.common.Util.Pair<Double, Fleet>> totalPointsAndFleet = new HashMap<Competitor, com.sap.sse.common.Util.Pair<Double, Fleet>>();
        for (Competitor competitor : getCompetitors()) {
            Double totalPoints = getTotalPoints(competitor, raceColumn, timePoint);
            if (totalPoints != null) {
                totalPointsAndFleet.put(competitor, new com.sap.sse.common.Util.Pair<Double, Fleet>(totalPoints,
                        raceColumn.getFleetOfCompetitor(competitor)));
            }
        }
        List<Competitor> result = new ArrayList<Competitor>(totalPointsAndFleet.keySet());
        Collections.sort(result, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                int comparisonResult;
                if (o1 == o2) {
                    comparisonResult = 0;
                } else {
                    if (raceColumn.hasSplitFleets() && !raceColumn.hasSplitFleetContiguousScoring()) {
                        // only check fleets if there are more than one and the column is not to be contiguously scored
                        // even in case
                        // of split fleets
                        final Fleet o1Fleet = totalPointsAndFleet.get(o1).getB();
                        final Fleet o2Fleet = totalPointsAndFleet.get(o2).getB();
                        if (o1Fleet == null) {
                            if (o2Fleet == null) {
                                comparisonResult = 0;
                            } else {
                                comparisonResult = 1; // o1 ranks "worse" because it doesn't have a fleet set while o2
                                                      // has
                            }
                        } else {
                            if (o2Fleet == null) {
                                comparisonResult = -1; // o1 ranks "better" because it has a fleet set while o2 hasn't
                            } else {
                                comparisonResult = o1Fleet.compareTo(o2Fleet);
                            }
                        }
                    } else {
                        // either there are no split fleets or the split isn't relevant for scoring as for ordered
                        // fleets
                        // the scoring runs contiguously from top to bottom
                        comparisonResult = 0;
                    }
                    if (comparisonResult == 0) {
                        comparisonResult = getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false)
                                .compare(totalPointsAndFleet.get(o1).getA(), totalPointsAndFleet.get(o2).getA());
                    }
                }
                return comparisonResult;
            }
        });
        return result;
    }

    /**
     * suppressed competitors are removed from the result
     */
    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        return getCompetitorsFromBestToWorst(getRaceColumns(), timePoint);
    }

    /**
     * suppressed competitors are removed from the result
     */
    private List<Competitor> getCompetitorsFromBestToWorst(Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) {
        List<Competitor> result = new ArrayList<Competitor>();
        for (Competitor competitor : getCompetitors()) {
            result.add(competitor);
        }
        Collections.sort(result, getTotalRankComparator(raceColumnsToConsider, timePoint));
        return result;
    }

    protected Comparator<? super Competitor> getTotalRankComparator(Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) {
        return new LeaderboardTotalRankComparator(this, timePoint, getScoringScheme(), /* nullScoresAreBetter */ false,
                raceColumnsToConsider);
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
    public boolean countRaceForComparisonWithDiscardingThresholds(Competitor competitor, RaceColumn raceColumn,
            TimePoint timePoint) {
        TrackedRace trackedRaceForCompetitorInColumn;
        return getScoringScheme().isValidInNetScore(this, raceColumn, competitor, timePoint)
                && (getScoreCorrection().isScoreCorrected(competitor, raceColumn, timePoint)
                        || ((trackedRaceForCompetitorInColumn = raceColumn.getTrackedRace(competitor)) != null
                                && trackedRaceForCompetitorInColumn.hasStarted(timePoint)
                                && trackedRaceForCompetitorInColumn.getRank(competitor, timePoint) != 0));
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
    public Entry getEntry(final Competitor competitor, final RaceColumn race, final TimePoint timePoint)
            throws NoWindException {
        final Set<RaceColumn> discardedRaceColumns = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this,
                getRaceColumns(), timePoint);
        return getEntry(competitor, race, timePoint, discardedRaceColumns);
    }

    @Override
    public Entry getEntry(final Competitor competitor, final RaceColumn race, final TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) throws NoWindException {
        Callable<Integer> trackedRankProvider = () -> getTrackedRank(competitor, race, timePoint);
        final Result correctedResults = getScoreCorrection().getCorrectedScore(trackedRankProvider, competitor, race,
                this, timePoint, new NumberOfCompetitorsFetcherImpl(), getScoringScheme());
        boolean discarded = isDiscarded(competitor, race, timePoint, discardedRaceColumns);
        final Double correctedScore = correctedResults.getCorrectedScore();
        return new EntryImpl(trackedRankProvider, correctedScore, () -> correctedResults.getUncorrectedScore(),
                correctedResults.isCorrected(),
                discarded ? DOUBLE_0
                        : correctedScore == null ? null : Double.valueOf(correctedScore * getScoringScheme().getScoreFactor(race)),
                correctedResults.getMaxPointsReason(), discarded, race.getFleetOfCompetitor(competitor));
    }

    @Override
    public Map<RaceColumn, List<Competitor>> getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(TimePoint timePoint)
            throws NoWindException {
        Map<RaceColumn, List<Competitor>> result = new LinkedHashMap<>();
        List<RaceColumn> raceColumnsToConsider = new ArrayList<>();
        for (RaceColumn raceColumn : getRaceColumns()) {
            raceColumnsToConsider.add(raceColumn);
            result.put(raceColumn, getCompetitorsFromBestToWorst(raceColumnsToConsider, timePoint));
        }
        return result;
    }

    @Override
    public Map<RaceColumn, Map<Competitor, Double>> getNetPointsSumAfterRaceColumn(final TimePoint timePoint)
            throws NoWindException {
        final Map<RaceColumn, Map<Competitor, Double>> result = new LinkedHashMap<>();
        List<RaceColumn> raceColumnsToConsider = new ArrayList<>();
        Map<RaceColumn, Future<Map<Competitor, Double>>> futures = new HashMap<>();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            raceColumnsToConsider.add(raceColumn);
            final Iterable<RaceColumn> finalRaceColumnsToConsider = new ArrayList<>(raceColumnsToConsider);
            futures.put(raceColumn, executor.submit(new Callable<Map<Competitor, Double>>() {
                @Override
                public Map<Competitor, Double> call() {
                    Map<Competitor, Double> netPointsSumPerCompetitorInColumn = new HashMap<>();
                    for (Competitor competitor : getCompetitors()) {
                        netPointsSumPerCompetitorInColumn.put(competitor,
                                getNetPoints(competitor, finalRaceColumnsToConsider, timePoint));
                    }
                    synchronized (result) {
                        return netPointsSumPerCompetitorInColumn;
                    }
                }
            }));
        }
        for (RaceColumn raceColumn : getRaceColumns()) {
            try {
                result.put(raceColumn, futures.get(raceColumn).get());
            } catch (InterruptedException | ExecutionException e) {
                if (e.getCause() instanceof NoWindError) {
                    throw ((NoWindError) e.getCause()).getCause();
                } else {
                    throw new RuntimeException(e); // no caught exceptions occur in the futures executed
                }
            }
        }
        return result;
    }

    @Override
    public Map<com.sap.sse.common.Util.Pair<Competitor, RaceColumn>, Entry> getContent(final TimePoint timePoint)
            throws NoWindException {
        Map<com.sap.sse.common.Util.Pair<Competitor, RaceColumn>, Entry> result = new HashMap<com.sap.sse.common.Util.Pair<Competitor, RaceColumn>, Entry>();
        Map<Competitor, Set<RaceColumn>> discardedRaces = new HashMap<Competitor, Set<RaceColumn>>();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            for (final Competitor competitor : getCompetitors()) {
                Callable<Integer> trackedRankProvider = () -> getTrackedRank(competitor, raceColumn, timePoint);
                final Result correctedResults = getScoreCorrection().getCorrectedScore(trackedRankProvider, competitor,
                        raceColumn, this, timePoint, new NumberOfCompetitorsFetcherImpl(), getScoringScheme());
                Set<RaceColumn> discardedRacesForCompetitor = discardedRaces.get(competitor);
                if (discardedRacesForCompetitor == null) {
                    discardedRacesForCompetitor = getResultDiscardingRule().getDiscardedRaceColumns(competitor, this,
                            getRaceColumns(), timePoint);
                    discardedRaces.put(competitor, discardedRacesForCompetitor);
                }
                boolean discarded = discardedRacesForCompetitor.contains(raceColumn);
                final Double correctedScore = correctedResults.getCorrectedScore();
                Entry entry = new EntryImpl(trackedRankProvider, correctedScore,
                        () -> correctedResults.getUncorrectedScore(), correctedResults.isCorrected(),
                        discarded ? DOUBLE_0
                                : (correctedScore == null ? null
                                        : Double.valueOf((correctedScore * getScoringScheme().getScoreFactor(raceColumn)))),
                        correctedResults.getMaxPointsReason(), discarded, raceColumn.getFleetOfCompetitor(competitor));
                result.put(new com.sap.sse.common.Util.Pair<Competitor, RaceColumn>(competitor, raceColumn), entry);
            }
        }
        return result;
    }

    /**
     * A leaderboard will only accept the addition of a race column if the column's name is unique across the
     * leaderboard.
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

    /**
     * Finds out the time point when any of the {@link Leaderboard#getTrackedRaces() tracked races currently attached to
     * the <code>leaderboard</code>} and the {@link Leaderboard#getScoreCorrection() score corrections} have last been
     * modified. If no tracked race is attached and no time-stamped score corrections have been applied to the
     * leaderboard, <code>null</code> is returned. The time point computed this way is a good choice for normalizing
     * queries for later time points in an attempt to achieve more cache hits.
     * <p>
     * 
     * Note, however, that the result does not tell about structural changes to the leaderboard and therefore cannot be
     * used to determine the need for cache invalidation. For example, if a column is added to a leaderboard after the
     * time point returned by this method but that column's attached tracked race has finished before the time point
     * returned by this method, the result of this method won't change. Still, the contents of the leaderboard will
     * change by a change in column structure. A different means to determine the possibility of changes that happened
     * to this leaderboard must be used for cache management. Such a facility has to listen for score correction
     * changes, tracked races being attached or detached and the column structure changing.
     * 
     * @see TrackedRace#getTimePointOfNewestEvent()
     * @see SettableScoreCorrection#getTimePointOfLastCorrectionsValidity()
     */
    @Override
    public TimePoint getTimePointOfLatestModification() {
        TimePoint result = null;
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (result == null || (trackedRace.getTimePointOfNewestEvent() != null
                    && trackedRace.getTimePointOfNewestEvent().after(result))) {
                result = trackedRace.getTimePointOfNewestEvent();
            }
        }
        TimePoint timePointOfLastScoreCorrection = getScoreCorrection().getTimePointOfLastCorrectionsValidity();
        if (timePointOfLastScoreCorrection != null
                && (result == null || timePointOfLastScoreCorrection.after(result))) {
            result = timePointOfLastScoreCorrection;
        }
        return result;
    }

    @Override
    public com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> getMaximumSpeedOverGround(Competitor competitor,
            TimePoint timePoint) {
        com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> result = null;
        // TODO should we ensure that competitor participated in all race columns?
        for (TrackedRace trackedRace : getTrackedRaces()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor);
                if (!markPassings.isEmpty()) {
                    TimePoint from = markPassings.first().getTimePoint();
                    TimePoint to;
                    if (timePoint.after(markPassings.last().getTimePoint()) && markPassings.last()
                            .getWaypoint() == trackedRace.getRace().getCourse().getLastWaypoint()) {
                        // stop counting when competitor finished the race
                        to = markPassings.last().getTimePoint();
                    } else {
                        to = timePoint;
                    }
                    com.sap.sse.common.Util.Pair<GPSFixMoving, Speed> maxSpeed = trackedRace.getTrack(competitor)
                            .getMaximumSpeedOverGround(from, to);
                    if (result == null || result.getB() == null || (maxSpeed != null && maxSpeed.getB() != null
                            && maxSpeed.getB().compareTo(result.getB()) > 0)) {
                        result = maxSpeed;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Speed getAverageSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        Speed result = null;
        final Duration totalTimeSailed = this.getTotalTimeSailed(competitor, timePoint);
        final Distance totalDistanceSailed = this.getTotalDistanceTraveled(competitor, timePoint);
        if (totalDistanceSailed != null && totalTimeSailed != null && !totalTimeSailed.equals(Duration.NULL)) {
            result = totalDistanceSailed.inTime(totalTimeSailed);
        }
        return result;
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        final Iterable<Competitor> result;
        // mostly the set of suppressed competitors is empty; in this case, avoid having to loop over the
        // potentially large set of competitors
        if (Util.isEmpty(getSuppressedCompetitors())) {
            result = getAllCompetitors();
        } else {
            final Iterable<Competitor> allCompetitors = getAllCompetitors();
            final Set<Competitor> suppressed = new HashSet<>();
            Util.addAll(getSuppressedCompetitors(), suppressed);
            result = getCompetitorIterableSkippingSuppressed(allCompetitors, suppressed);
        }
        return result;
    }

    @Override
    public Iterable<Competitor> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return getCompetitorIterableSkippingSuppressed(getAllCompetitors(raceColumn, fleet),
                getSuppressedCompetitors());
    }

    /**
     * return an iterable with a smart iterator that filters out the suppressed elements on demand
     */
    protected Iterable<Competitor> getCompetitorIterableSkippingSuppressed(final Iterable<Competitor> allCompetitors,
            final Iterable<Competitor> suppressed) {
        return new ObscuringIterable<>(allCompetitors, suppressed);
    }

    @Override
    public Iterable<Competitor> getSuppressedCompetitors() {
        LockUtil.lockForRead(suppressedCompetitorsLock);
        try {
            return new HashSet<Competitor>(suppressedCompetitors);
        } finally {
            LockUtil.unlockAfterRead(suppressedCompetitorsLock);
        }
    }

    @Override
    public boolean isSuppressed(Competitor competitor) {
        LockUtil.lockForRead(suppressedCompetitorsLock);
        try {
            return suppressedCompetitors.contains(competitor);
        } finally {
            LockUtil.unlockAfterRead(suppressedCompetitorsLock);
        }
    }

    @Override
    public void setSuppressed(Competitor competitor, boolean suppressed) {
        if (competitor == null) {
            throw new IllegalArgumentException("Cannot change suppression for a null competitor");
        }
        LockUtil.lockForWrite(suppressedCompetitorsLock);
        try {
            if (suppressed) {
                suppressedCompetitors.add(competitor);
            } else {
                suppressedCompetitors.remove(competitor);
            }
        } finally {
            LockUtil.unlockAfterWrite(suppressedCompetitorsLock);
        }
        getScoreCorrection().notifyListenersAboutIsSuppressedChange(competitor, suppressed);
    }

    @Override
    public TimePoint getNowMinusDelay() {
        final TimePoint now = MillisecondsTimePoint.now();
        final Long delayToLiveInMillis = getDelayToLiveInMillis();
        TimePoint timePoint = delayToLiveInMillis == null ? now : now.minus(delayToLiveInMillis);
        return timePoint;
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
        getRaceColumnListeners().notifyListenersAboutRaceLogEventAdded(raceColumn, raceLogIdentifier, event);
        super.raceLogEventAdded(raceColumn, raceLogIdentifier, event);
    }

    @Override
    public void regattaLogEventAdded(RegattaLogEvent event) {
        getRaceColumnListeners().notifyListenersAboutRegattaLogEventAdded(event);
    }

    public String toString() {
        return getName() + " "
                + (getDefaultCourseArea() != null ? getDefaultCourseArea().getName() : "<No course area defined>") + " "
                + (getScoringScheme() != null ? getScoringScheme().getType().name() : "<No scoring scheme set>");
    }

    @Override
    public NumberOfCompetitorsInLeaderboardFetcher getNumberOfCompetitorsInLeaderboardFetcher() {
        return new NumberOfCompetitorsFetcherImpl();
    }

    @Override
    public BoatClass getBoatClass() {
        Set<Boat> allBoats = new HashSet<>();
        for (final RaceColumn raceColumn : getRaceColumns()) {
            Map<Competitor, Boat> competitorsAndTheirBoats = raceColumn.getAllCompetitorsAndTheirBoats();
            allBoats.addAll(competitorsAndTheirBoats.values());
        }
        return Util.getDominantObject(StreamSupport.stream(allBoats.spliterator(), /* parallel */ false)
                .map(b -> b.getBoatClass()).collect(Collectors.toList()));
    }
    
    

}
