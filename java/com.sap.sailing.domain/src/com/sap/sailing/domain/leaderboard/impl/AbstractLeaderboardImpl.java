package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
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

public abstract class AbstractLeaderboardImpl implements Leaderboard {
    private static final long serialVersionUID = -328091952760083438L;

    /**
     * The factor by which a medal race score is multiplied in the overall point scheme
     */
    private static final int MEDAL_RACE_FACTOR = 2;
    
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
    private final Map<Competitor, Integer> carriedPoints;
    
    /**
     * A leaderboard entry representing a snapshot of a cell at a given time point for a single race/competitor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class EntryImpl implements Entry {
        private final int trackedPoints;
        private final int netPoints;
        private final boolean isNetPointsCorrected;
        private final int totalPoints;
        private final MaxPointsReason maxPointsReason;
        private final boolean discarded;
        private EntryImpl(int trackedPoints, int netPoints, boolean isNetPointsCorrected, int totalPoints, MaxPointsReason maxPointsReason, boolean discarded) {
            super();
            this.trackedPoints = trackedPoints;
            this.netPoints = netPoints;
            this.isNetPointsCorrected = isNetPointsCorrected;
            this.totalPoints = totalPoints;
            this.maxPointsReason = maxPointsReason;
            this.discarded = discarded;
        }
        @Override
        public int getTrackedPoints() {
            return trackedPoints;
        }
        @Override
        public int getNetPoints() {
            return netPoints;
        }
        @Override
        public boolean isNetPointsCorrected() {
            return isNetPointsCorrected;
        }
        @Override
        public int getTotalPoints() {
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
    }

    /**
     * @param name must not be <code>null</code>
     */
    public AbstractLeaderboardImpl(SettableScoreCorrection scoreCorrection, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        this.carriedPoints = new HashMap<Competitor, Integer>();
        this.scoreCorrection = scoreCorrection;
        this.displayNames = new HashMap<Competitor, String>();
        this.resultDiscardingRule = resultDiscardingRule;
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
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (TrackedRace r : getTrackedRaces()) {
            for (Competitor c : r.getRace().getCompetitors()) {
                result.add(c);
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
        return trackedRace == null ? 0 : trackedRace.hasStarted(timePoint) ? trackedRace.getRank(competitor, timePoint) : 0;
    }

    @Override
    public int getNetPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        return getScoreCorrection().getCorrectedScore(getTrackedRank(competitor, raceColumn, timePoint), competitor,
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
    public int getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        return isDiscarded(competitor, raceColumn, timePoint) ?
                0 :
                (raceColumn.isMedalRace() ? MEDAL_RACE_FACTOR : 1) * getNetPoints(competitor, raceColumn, timePoint);
    }
    
    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = getCarriedPoints(competitor);
        for (RaceColumn r : getRaceColumns()) {
            result += getTotalPoints(competitor, r, timePoint);
        }
        return result;
    }

    @Override
    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        int trackedPoints = getTrackedRank(competitor, race, timePoint);
        final Result correctedResults = getScoreCorrection().getCorrectedScore(trackedPoints, competitor, race,
                timePoint, Util.size(getCompetitors()));
        boolean discarded = isDiscarded(competitor, race, timePoint);
        return new EntryImpl(trackedPoints, correctedResults.getCorrectedScore(), correctedResults.isCorrected(),
                discarded ? 0
                        : correctedResults.getCorrectedScore() * (race.isMedalRace() ? 2 : 1), correctedResults.getMaxPointsReason(), discarded);
    }
    
    @Override
    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(TimePoint timePoint) throws NoWindException {
        Map<Pair<Competitor, RaceColumn>, Entry> result = new HashMap<Pair<Competitor, RaceColumn>, Entry>();
        Map<Competitor, Set<RaceColumn>> discardedRaces = new HashMap<Competitor, Set<RaceColumn>>();
        for (RaceColumn raceColumn : getRaceColumns()) {
            for (Competitor competitor : getCompetitors()) {
                int trackedPoints;
                final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                if (trackedRace != null && trackedRace.hasStarted(timePoint)) {
                    trackedPoints = trackedRace.getRank(competitor, timePoint);
                } else {
                    trackedPoints = 0;
                }
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
                                * (raceColumn.isMedalRace() ? 2 : 1), correctedResults.getMaxPointsReason(), discarded);
                result.put(new Pair<Competitor, RaceColumn>(competitor, raceColumn), entry);
            }
        }
        return result;
    }

    @Override
    public void setCarriedPoints(Competitor competitor, int carriedPoints) {
        this.carriedPoints.put(competitor, carriedPoints);
    }

    @Override
    public int getCarriedPoints(Competitor competitor) {
        Integer result = carriedPoints.get(competitor);
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
        final Map<Competitor, Pair<Integer, Fleet>> netPointsAndFleet = new HashMap<Competitor, Pair<Integer, Fleet>>();
        for (Competitor competitor : getCompetitors()) {
            int netPoints = getNetPoints(competitor, raceColumn, timePoint);
            if (netPoints != 0) {
                netPointsAndFleet.put(competitor, new Pair<Integer, Fleet>(netPoints, raceColumn.getFleetOfCompetitor(competitor)));
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
                    comparisonResult = netPointsAndFleet.get(o1).getB().compareTo(netPointsAndFleet.get(o2).getB());
                    if (comparisonResult == 0) {
                        comparisonResult = netPointsAndFleet.get(o1).getA() - netPointsAndFleet.get(o2).getA();
                    }
                }
                return comparisonResult;
            }
        });
        return result;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        Map<Competitor, List<Integer>> scores;
        // TODO which columns to score? Is shallColumnBeUsedForTotalRank the answer?
        // TODO issues:
        //  - columns that count may contain competitors that have no score in that column; should they be ranked to the bottom?
        //  - we don't know which competitors compete in those fleets for which we have no tracked race in a column
        //  - in a FlexibleLeaderboard, may there be broken constellations in which a competitor belongs to a "better" fleet
        //    in one column and a worse fleet in another?
        //  - how to compute the total points if different competitors have different numbers of races where they scored?
        /*
         * Approach: for a RegattaLeaderboard there are Series which are ordered; for a FlexibleLeaderboard we don't know about Series.
         * In a FlexibleLeaderboard, all we have are the columns with their fleets which can be compared but may compare equal; also,
         * we know if a column is a medal race column. Participants of a medal race always score better than all remaining competitors.
         * (We only know the medal race participants if the column has a tracked race.)
         * As soon as we find a column with more than one tracked race with the fleets comparing non-equal, this decides the direct
         * comparison of all pairs of competitors in different fleets.
         * 
         * For a RegattaLeaderboard, if a column in a series has tracked races for all of its fleets, these competitors rank better
         * than all remaining competitors that appear in prior series. This is probably the generalization of the "medal race" rule
         * where the medal "series" has one race, and if it's tracked, its participants rank better than all others in prior series
         * who did not reach the medal race.
         * 
         * If none of these rules decide the comparison between two competitors, use their total points, including the doubling rule
         * for the medal race and the score corrections, max points and discarding rules. Then, if still two competitors score equal,
         * tie breaking rules have to be applied. The algorithm for this means to sort the scores from best to worst and compare
         * pairwise. As soon as the first pair differs, we have a decision.
         */
        for (RaceColumn raceColumn : getRaceColumns()) {
            if (shallColumnBeUsedForTotalRank(raceColumn, timePoint)) {
                // the column counts for sorting
                
            }
        }
        return null;
    }

    /**
     * The column has one or more fleets, zero or more of which may have a tracked race associated, each of which may or
     * may not have started at <code>timePoint</code>. For the competitors who will be racing or have been racing
     * without a tracked race in the remaining fleets, score corrections may exist. However, we cannot know whether this
     * is the case for all competitors as the competitors are currently provided by the tracked races which in this case
     * are missing for at least one fleet.
     */
    private boolean shallColumnBeUsedForTotalRank(RaceColumn raceColumn, TimePoint timePoint) {
        boolean allFleetsHaveTrackedRace = true;
        Map<Competitor, Fleet> fleetForCompetitor = new HashMap<Competitor, Fleet>();
        Map<Fleet, Set<Competitor>> competitorsForFleets = new HashMap<Fleet, Set<Competitor>>();
        for (Fleet fleet : raceColumn.getFleets()) {
            final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
            if (trackedRace == null || !trackedRace.hasStarted(timePoint)) {
                allFleetsHaveTrackedRace = false;
                break;
            } else {
                Set<Competitor> competitorsForFleet = new HashSet<Competitor>();
                for (Competitor c : trackedRace.getRace().getCompetitors()) {
                    fleetForCompetitor.put(c, fleet);
                }
                competitorsForFleets.put(fleet, competitorsForFleet);
            }
        }
        if (!allFleetsHaveTrackedRace) {
            // look for score corrections for competitors in un-tracked fleet
            for (Competitor competitor : getCompetitors()) {
                if (fleetForCompetitor.get(competitor) != null && getScoreCorrection().isScoreCorrected(competitor, raceColumn)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

}
