package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardChangeListener;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.ObscuringIterable;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class DelegatingRegattaLeaderboardWithCompetitorElimination extends AbstractLeaderboardWithCache implements RegattaLeaderboard {
    private static final long serialVersionUID = 8331154893189722924L;
    private final String name;
    private final RegattaLeaderboard fullLeaderboard;
    
    /**
     * Competitors eliminated from this leaderboard for regatta ranking; those competitors are not part of
     * {@link #getCompetitors()} but appear in {@link #getAllCompetitors()}. They may have an overlap with
     * {@link #getSuppressedCompetitors()}, but while suppressed competitors cannot receive a score in a single race,
     * eliminated competitors can, and their scores are relevant for computing the regatta ranks, but ultimately, an
     * eliminated competitor's regatta rank is defined as {@code 0} for this leaderboard, and competitors ranking worse
     * in the {@link #fullLeaderboard original leaderboard} will advance by one rank per eliminated competitor ranking
     * better.
     */
    private final ConcurrentHashMap<Competitor, Boolean> eliminatedCompetitors;

    public DelegatingRegattaLeaderboardWithCompetitorElimination(RegattaLeaderboard fullLeaderboard, String name) {
        this.name = name;
        this.fullLeaderboard = fullLeaderboard;
        this.eliminatedCompetitors = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String newName) {
        fullLeaderboard.setName(newName);
    }

    public String getDisplayName() {
        return fullLeaderboard.getDisplayName();
    }

    public CompetitorProviderFromRaceColumnsAndRegattaLike getOrCreateCompetitorsProvider() {
        return fullLeaderboard.getOrCreateCompetitorsProvider();
    }

    public Regatta getRegatta() {
        return fullLeaderboard.getRegatta();
    }

    public void addLeaderboardChangeListener(LeaderboardChangeListener listener) {
        fullLeaderboard.addLeaderboardChangeListener(listener);
    }

    public Iterable<Competitor> getCompetitorsRegisteredInRegattaLog() {
        return fullLeaderboard.getCompetitorsRegisteredInRegattaLog();
    }

    public void removeLeaderboardChangeListener(LeaderboardChangeListener listener) {
        fullLeaderboard.removeLeaderboardChangeListener(listener);
    }

    public IsRegattaLike getRegattaLike() {
        return fullLeaderboard.getRegattaLike();
    }

    public RaceLog getRacelog(String raceColumnName, String fleetName) {
        return fullLeaderboard.getRacelog(raceColumnName, fleetName);
    }

    public void registerCompetitor(Competitor competitor) {
        fullLeaderboard.registerCompetitor(competitor);
    }

    public void registerCompetitors(Iterable<Competitor> competitor) {
        fullLeaderboard.registerCompetitors(competitor);
    }

    public void deregisterCompetitor(Competitor competitor) {
        fullLeaderboard.deregisterCompetitor(competitor);
    }

    public void deregisterCompetitors(Iterable<Competitor> competitor) {
        fullLeaderboard.deregisterCompetitors(competitor);
    }

    public Iterable<Competitor> getCompetitors() {
        return new ObscuringIterable<>(fullLeaderboard.getCompetitors(), eliminatedCompetitors.keySet());
    }
    
    public void setEliminated(Competitor competitor, boolean eliminated) {
        if (eliminated) {
            eliminatedCompetitors.put(competitor, true);
        } else {
            eliminatedCompetitors.remove(competitor);
        }
    }
    
    public boolean isEliminated(Competitor competitor) {
        return eliminatedCompetitors.containsKey(competitor);
    }

    public Iterable<Competitor> getAllCompetitors() {
        return fullLeaderboard.getAllCompetitors();
    }

    public Iterable<Competitor> getAllCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return fullLeaderboard.getAllCompetitors(raceColumn, fleet);
    }

    public Iterable<Competitor> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return fullLeaderboard.getCompetitors(raceColumn, fleet);
    }

    public Iterable<Competitor> getSuppressedCompetitors() {
        return fullLeaderboard.getSuppressedCompetitors();
    }

    public boolean isSuppressed(Competitor competitor) {
        return fullLeaderboard.isSuppressed(competitor);
    }

    public void setSuppressed(Competitor competitor, boolean suppressed) {
        fullLeaderboard.setSuppressed(competitor, suppressed);
    }

    public Fleet getFleet(String fleetName) {
        return fullLeaderboard.getFleet(fleetName);
    }

    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        return fullLeaderboard.getEntry(competitor, race, timePoint);
    }

    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) throws NoWindException {
        return fullLeaderboard.getEntry(competitor, race, timePoint, discardedRaceColumns);
    }

    public Map<RaceColumn, List<Competitor>> getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(TimePoint timePoint)
            throws NoWindException {
        Map<RaceColumn, List<Competitor>> preResult = fullLeaderboard.getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(timePoint);
        for (final List<Competitor> e : preResult.values()) {
            e.removeAll(eliminatedCompetitors.keySet());
        }
        return preResult;
    }

    public Map<RaceColumn, Map<Competitor, Double>> getNetPointsSumAfterRaceColumn(TimePoint timePoint)
            throws NoWindException {
        return fullLeaderboard.getNetPointsSumAfterRaceColumn(timePoint);
    }

    public double getCarriedPoints(Competitor competitor) {
        return fullLeaderboard.getCarriedPoints(competitor);
    }

    public Map<Competitor, Double> getCompetitorsForWhichThereAreCarriedPoints() {
        final Map<Competitor, Double> result = new HashMap<>();
        for (final java.util.Map.Entry<Competitor, Double> e : fullLeaderboard.getCompetitorsForWhichThereAreCarriedPoints().entrySet()) {
            if (!isEliminated(e.getKey())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        return fullLeaderboard.getTrackedRank(competitor, race, timePoint);
    }

    public Double getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return fullLeaderboard.getTotalPoints(competitor, raceColumn, timePoint);
    }

    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        return fullLeaderboard.getMaxPointsReason(competitor, race, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        return fullLeaderboard.getNetPoints(competitor, race, timePoint);
    }

    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return fullLeaderboard.isDiscarded(competitor, raceColumn, timePoint);
    }

    public Double getNetPoints(Competitor competitor, TimePoint timePoint) {
        return fullLeaderboard.getNetPoints(competitor, timePoint);
    }

    public Double getNetPoints(Competitor competitor, Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint)
            throws NoWindException {
        return fullLeaderboard.getNetPoints(competitor, raceColumnsToConsider, timePoint);
    }

    public Iterable<Competitor> getCompetitorsFromBestToWorst(RaceColumn raceColumn, TimePoint timePoint)
            throws NoWindException {
        return new ObscuringIterable<>(fullLeaderboard.getCompetitorsFromBestToWorst(raceColumn, timePoint), eliminatedCompetitors.keySet());
    }

    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        final List<Competitor> result = new ArrayList<>();
        for (final Competitor c : fullLeaderboard.getCompetitorsFromBestToWorst(timePoint)) {
            if (!isEliminated(c)) {
                result.add(c);
            }
        }
        return result;
    }

    public int getTotalRankOfCompetitor(Competitor competitor, TimePoint timePoint) throws NoWindException {
        return getCompetitorsFromBestToWorst(timePoint).indexOf(competitor) + 1;
    }

    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(TimePoint timePoint) throws NoWindException {
        final Map<Pair<Competitor, RaceColumn>, Entry> result = new HashMap<>();
        for (final java.util.Map.Entry<Pair<Competitor, RaceColumn>, Entry> e : fullLeaderboard.getContent(timePoint).entrySet()) {
            if (!isEliminated(e.getKey().getA())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    public Iterable<RaceColumn> getRaceColumns() {
        return fullLeaderboard.getRaceColumns();
    }

    public RaceColumn getRaceColumnByName(String name) {
        return fullLeaderboard.getRaceColumnByName(name);
    }

    public void setCarriedPoints(Competitor competitor, double carriedPoints) {
        fullLeaderboard.setCarriedPoints(competitor, carriedPoints);
    }

    public void unsetCarriedPoints(Competitor competitor) {
        fullLeaderboard.unsetCarriedPoints(competitor);
    }

    public boolean hasCarriedPoints() {
        return fullLeaderboard.hasCarriedPoints();
    }

    public boolean hasCarriedPoints(Competitor competitor) {
        return fullLeaderboard.hasCarriedPoints(competitor);
    }

    public SettableScoreCorrection getScoreCorrection() {
        return fullLeaderboard.getScoreCorrection();
    }

    public Competitor getCompetitorByName(String competitorName) {
        return fullLeaderboard.getCompetitorByName(competitorName);
    }

    public void setDisplayName(Competitor competitor, String displayName) {
        fullLeaderboard.setDisplayName(competitor, displayName);
    }

    public void setDisplayName(String displayName) {
        fullLeaderboard.setDisplayName(displayName);
    }

    public String getDisplayName(Competitor competitor) {
        return fullLeaderboard.getDisplayName(competitor);
    }

    public boolean countRaceForComparisonWithDiscardingThresholds(Competitor competitor, RaceColumn raceColumn,
            TimePoint timePoint) {
        return fullLeaderboard.countRaceForComparisonWithDiscardingThresholds(competitor, raceColumn, timePoint);
    }

    public ResultDiscardingRule getResultDiscardingRule() {
        return fullLeaderboard.getResultDiscardingRule();
    }

    public void setCrossLeaderboardResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        fullLeaderboard.setCrossLeaderboardResultDiscardingRule(discardingRule);
    }

    public Competitor getCompetitorByIdAsString(String idAsString) {
        return fullLeaderboard.getCompetitorByIdAsString(idAsString);
    }

    public void addRaceColumnListener(RaceColumnListener listener) {
        fullLeaderboard.addRaceColumnListener(listener);
    }

    public void removeRaceColumnListener(RaceColumnListener listener) {
        fullLeaderboard.removeRaceColumnListener(listener);
    }

    public Long getDelayToLiveInMillis() {
        return fullLeaderboard.getDelayToLiveInMillis();
    }

    public Iterable<TrackedRace> getTrackedRaces() {
        return fullLeaderboard.getTrackedRaces();
    }

    public ScoringScheme getScoringScheme() {
        return fullLeaderboard.getScoringScheme();
    }

    public TimePoint getTimePointOfLatestModification() {
        return fullLeaderboard.getTimePointOfLatestModification();
    }

    public Pair<GPSFixMoving, Speed> getMaximumSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        return fullLeaderboard.getMaximumSpeedOverGround(competitor, timePoint);
    }

    public Speed getAverageSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        return fullLeaderboard.getAverageSpeedOverGround(competitor, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) throws NoWindException {
        return fullLeaderboard.getNetPoints(competitor, raceColumn, raceColumnsToConsider, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) {
        return fullLeaderboard.getNetPoints(competitor, raceColumn, timePoint, discardedRaceColumns);
    }

    public TimePoint getNowMinusDelay() {
        return fullLeaderboard.getNowMinusDelay();
    }

    public CourseArea getDefaultCourseArea() {
        return fullLeaderboard.getDefaultCourseArea();
    }

    public NumberOfCompetitorsInLeaderboardFetcher getNumberOfCompetitorsInLeaderboardFetcher() {
        return fullLeaderboard.getNumberOfCompetitorsInLeaderboardFetcher();
    }

    public Pair<RaceColumn, Fleet> getRaceColumnAndFleet(TrackedRace trackedRace) {
        return fullLeaderboard.getRaceColumnAndFleet(trackedRace);
    }

    public BoatClass getBoatClass() {
        return fullLeaderboard.getBoatClass();
    }

    @Override
    protected LeaderboardType getLeaderboardType() {
        return LeaderboardType.RegattaLeaderboard;
    }
}
