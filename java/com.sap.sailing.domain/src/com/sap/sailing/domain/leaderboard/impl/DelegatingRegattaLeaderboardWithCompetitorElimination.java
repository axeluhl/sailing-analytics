package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.ObscuringIterable;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * A regatta leaderboard that is derived from another regatta leaderboard by eliminating a subset of the competitors and
 * that provides its own, unique name and optionally its own display name. The class generally implements a "delegate"
 * pattern for a {@link RegattaLeaderboard}. It therefore does not maintain its own score corrections or set of
 * suppressed competitors. Note: "suppressed" is different from "eliminated" in that suppressed competitors do not show
 * in any race and are not assigned any rank in any race, but eliminated competitors are; they only don't receive a
 * regatta ("total") rank, and all competitors advance by as many ranks compared to the original leaderboard as there
 * are eliminated competitors ranking better in the original leaderboard.
 * <p>
 * 
 * This behavior is achieved by overriding any method returning a collection of {@link Competitor}s, such as
 * {@link #getCompetitors()}, such that the eliminated competitors are removed from the result which should let any
 * leaderboard panel displaying the contents of this leaderboard list only the non-eliminated competitors. This includes
 * {@link #getCompetitorsFromBestToWorst(TimePoint)} which also leads the implementation of
 * {@link #getTotalRankOfCompetitor(Competitor, TimePoint)} to calculate the ranks based on the competitor list without
 * those eliminated.
 * 
 * @author Axel Uhl (d043530)
 */
public class DelegatingRegattaLeaderboardWithCompetitorElimination extends AbstractLeaderboardWithCache implements RegattaLeaderboardWithEliminations {
    private static final long serialVersionUID = 8331154893189722924L;
    private final String name;
    private RegattaLeaderboard fullLeaderboard;
    
    private transient final Supplier<RegattaLeaderboard> fullLeaderboardSupplier;
    
    /**
     * The particular use case for which this field is introduced is registering score correction
     * listeners at a point in time when the full leaderboard hasn't been resolved yet. Instead of
     * letting this listener registration attempt the resolution without success the request can
     * be queued here, and each time the {@link #getFullLeaderboard()} successfully resolves a
     * leaderboard, all consumers in this set will be triggered.
     */
    private final ConcurrentHashMap<Consumer<RegattaLeaderboard>, Boolean> triggerWhenFullLeaderboardIsResolved;
    
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

    /**
     * The leaderboard wrapper starts out with an empty set of eliminated competitors
     */
    public DelegatingRegattaLeaderboardWithCompetitorElimination(Supplier<RegattaLeaderboard> fullLeaderboardSupplier,
            String name) {
        this.name = name;
        this.fullLeaderboardSupplier = fullLeaderboardSupplier;
        this.eliminatedCompetitors = new ConcurrentHashMap<>();
        this.triggerWhenFullLeaderboardIsResolved = new ConcurrentHashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String newName) {
        getFullLeaderboard().setName(newName);
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        return new ObscuringIterable<>(getFullLeaderboard().getCompetitors(), eliminatedCompetitors.keySet());
    }
    
    @Override
    public void setEliminated(Competitor competitor, boolean eliminated) {
        if (eliminated) {
            eliminatedCompetitors.put(competitor, true);
        } else {
            eliminatedCompetitors.remove(competitor);
        }
    }
    
    @Override
    public boolean isEliminated(Competitor competitor) {
        return eliminatedCompetitors.containsKey(competitor);
    }

    @Override
    public Set<Competitor> getEliminatedCompetitors() {
        return new HashSet<Competitor>(eliminatedCompetitors.keySet());
    }

    public Map<RaceColumn, List<Competitor>> getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(TimePoint timePoint)
            throws NoWindException {
        Map<RaceColumn, List<Competitor>> preResult = getFullLeaderboard().getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(timePoint);
        for (final List<Competitor> e : preResult.values()) {
            e.removeAll(eliminatedCompetitors.keySet());
        }
        return preResult;
    }

    public Map<Competitor, Double> getCompetitorsForWhichThereAreCarriedPoints() {
        final Map<Competitor, Double> result = new HashMap<>();
        for (final java.util.Map.Entry<Competitor, Double> e : getFullLeaderboard().getCompetitorsForWhichThereAreCarriedPoints().entrySet()) {
            if (!isEliminated(e.getKey())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    public Iterable<Competitor> getCompetitorsFromBestToWorst(RaceColumn raceColumn, TimePoint timePoint)
            throws NoWindException {
        return new ObscuringIterable<>(getFullLeaderboard().getCompetitorsFromBestToWorst(raceColumn, timePoint), eliminatedCompetitors.keySet());
    }

    public List<Competitor> getCompetitorsFromBestToWorst(TimePoint timePoint) {
        final List<Competitor> result = new ArrayList<>();
        for (final Competitor c : getFullLeaderboard().getCompetitorsFromBestToWorst(timePoint)) {
            if (!isEliminated(c)) {
                result.add(c);
            }
        }
        return result;
    }

    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(TimePoint timePoint) throws NoWindException {
        final Map<Pair<Competitor, RaceColumn>, Entry> result = new HashMap<>();
        for (final java.util.Map.Entry<Pair<Competitor, RaceColumn>, Entry> e : getFullLeaderboard().getContent(timePoint).entrySet()) {
            if (!isEliminated(e.getKey().getA())) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    @Override
    public LeaderboardType getLeaderboardType() {
        return LeaderboardType.RegattaLeaderboardWithEliminations;
    }

    // --------------------- Delegate Pattern Implementation ----------------------
    public CompetitorProviderFromRaceColumnsAndRegattaLike getOrCreateCompetitorsProvider() {
        return getFullLeaderboard().getOrCreateCompetitorsProvider();
    }

    public Regatta getRegatta() {
        return getFullLeaderboard().getRegatta();
    }

    public Iterable<Competitor> getCompetitorsRegisteredInRegattaLog() {
        return getFullLeaderboard().getCompetitorsRegisteredInRegattaLog();
    }

    public IsRegattaLike getRegattaLike() {
        return getFullLeaderboard().getRegattaLike();
    }

    public RaceLog getRacelog(String raceColumnName, String fleetName) {
        return getFullLeaderboard().getRacelog(raceColumnName, fleetName);
    }

    public void registerCompetitor(Competitor competitor) {
        getFullLeaderboard().registerCompetitor(competitor);
    }

    public void registerCompetitors(Iterable<Competitor> competitors) {
        getFullLeaderboard().registerCompetitors(competitors);
    }

    public void deregisterCompetitor(Competitor competitor) {
        getFullLeaderboard().deregisterCompetitor(competitor);
    }

    public void deregisterCompetitors(Iterable<Competitor> competitor) {
        getFullLeaderboard().deregisterCompetitors(competitor);
    }

    public Iterable<Competitor> getAllCompetitors() {
        return getFullLeaderboard().getAllCompetitors();
    }

    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        return getFullLeaderboard().getAllCompetitorsWithRaceDefinitionsConsidered();
    }

    public Iterable<Competitor> getAllCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return getFullLeaderboard().getAllCompetitors(raceColumn, fleet);
    }

    public Iterable<Competitor> getCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return getFullLeaderboard().getCompetitors(raceColumn, fleet);
    }

    public Iterable<Competitor> getSuppressedCompetitors() {
        return getFullLeaderboard().getSuppressedCompetitors();
    }

    public boolean isSuppressed(Competitor competitor) {
        return getFullLeaderboard().isSuppressed(competitor);
    }

    public void setSuppressed(Competitor competitor, boolean suppressed) {
        getFullLeaderboard().setSuppressed(competitor, suppressed);
    }

    public Fleet getFleet(String fleetName) {
        return getFullLeaderboard().getFleet(fleetName);
    }

    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        return getFullLeaderboard().getEntry(competitor, race, timePoint);
    }

    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) throws NoWindException {
        return getFullLeaderboard().getEntry(competitor, race, timePoint, discardedRaceColumns);
    }

    public Map<RaceColumn, Map<Competitor, Double>> getNetPointsSumAfterRaceColumn(TimePoint timePoint)
            throws NoWindException {
        return getFullLeaderboard().getNetPointsSumAfterRaceColumn(timePoint);
    }

    public double getCarriedPoints(Competitor competitor) {
        return getFullLeaderboard().getCarriedPoints(competitor);
    }

    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        return getFullLeaderboard().getTrackedRank(competitor, race, timePoint);
    }

    public Double getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getFullLeaderboard().getTotalPoints(competitor, raceColumn, timePoint);
    }

    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        return getFullLeaderboard().getMaxPointsReason(competitor, race, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        return getFullLeaderboard().getNetPoints(competitor, race, timePoint);
    }

    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getFullLeaderboard().isDiscarded(competitor, raceColumn, timePoint);
    }

    public Double getNetPoints(Competitor competitor, TimePoint timePoint) {
        return getFullLeaderboard().getNetPoints(competitor, timePoint);
    }

    public Double getNetPoints(Competitor competitor, Iterable<RaceColumn> raceColumnsToConsider, TimePoint timePoint)
            throws NoWindException {
        return getFullLeaderboard().getNetPoints(competitor, raceColumnsToConsider, timePoint);
    }

    public Iterable<RaceColumn> getRaceColumns() {
        return getFullLeaderboard().getRaceColumns();
    }

    public RaceColumn getRaceColumnByName(String name) {
        return getFullLeaderboard().getRaceColumnByName(name);
    }

    public void setCarriedPoints(Competitor competitor, double carriedPoints) {
        getFullLeaderboard().setCarriedPoints(competitor, carriedPoints);
    }

    public void unsetCarriedPoints(Competitor competitor) {
        getFullLeaderboard().unsetCarriedPoints(competitor);
    }

    public boolean hasCarriedPoints() {
        return getFullLeaderboard().hasCarriedPoints();
    }

    public boolean hasCarriedPoints(Competitor competitor) {
        return getFullLeaderboard().hasCarriedPoints(competitor);
    }

    public SettableScoreCorrection getScoreCorrection() {
        return getFullLeaderboard().getScoreCorrection();
    }

    @Override
    public void addScoreCorrectionListener(ScoreCorrectionListener listener) {
        if (fullLeaderboard != null) {
            fullLeaderboard.addScoreCorrectionListener(listener);
        } else {
            triggerWhenFullLeaderboardIsResolved.put(leaderboard->leaderboard.addScoreCorrectionListener(listener), true);
        }
    }

    @Override
    public void removeScoreCorrectionListener(ScoreCorrectionListener listener) {
        if (fullLeaderboard != null) {
            fullLeaderboard.removeScoreCorrectionListener(listener);
        } else {
            triggerWhenFullLeaderboardIsResolved.put(leaderboard->leaderboard.removeScoreCorrectionListener(listener), true);
        }
    }

    public Competitor getCompetitorByName(String competitorName) {
        return getFullLeaderboard().getCompetitorByName(competitorName);
    }

    public void setDisplayName(Competitor competitor, String displayName) {
        getFullLeaderboard().setDisplayName(competitor, displayName);
    }

    public String getDisplayName(Competitor competitor) {
        return getFullLeaderboard().getDisplayName(competitor);
    }

    public boolean countRaceForComparisonWithDiscardingThresholds(Competitor competitor, RaceColumn raceColumn,
            TimePoint timePoint) {
        return getFullLeaderboard().countRaceForComparisonWithDiscardingThresholds(competitor, raceColumn, timePoint);
    }

    public ResultDiscardingRule getResultDiscardingRule() {
        return getFullLeaderboard().getResultDiscardingRule();
    }

    public void setCrossLeaderboardResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        getFullLeaderboard().setCrossLeaderboardResultDiscardingRule(discardingRule);
    }

    public Competitor getCompetitorByIdAsString(String idAsString) {
        return getFullLeaderboard().getCompetitorByIdAsString(idAsString);
    }

    public void addRaceColumnListener(RaceColumnListener listener) {
        getFullLeaderboard().addRaceColumnListener(listener);
    }

    public void removeRaceColumnListener(RaceColumnListener listener) {
        getFullLeaderboard().removeRaceColumnListener(listener);
    }

    public Long getDelayToLiveInMillis() {
        return getFullLeaderboard().getDelayToLiveInMillis();
    }

    public Iterable<TrackedRace> getTrackedRaces() {
        return getFullLeaderboard().getTrackedRaces();
    }

    public ScoringScheme getScoringScheme() {
        return getFullLeaderboard().getScoringScheme();
    }

    public TimePoint getTimePointOfLatestModification() {
        return getFullLeaderboard().getTimePointOfLatestModification();
    }

    public Pair<GPSFixMoving, Speed> getMaximumSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        return getFullLeaderboard().getMaximumSpeedOverGround(competitor, timePoint);
    }

    public Speed getAverageSpeedOverGround(Competitor competitor, TimePoint timePoint) {
        return getFullLeaderboard().getAverageSpeedOverGround(competitor, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, Iterable<RaceColumn> raceColumnsToConsider,
            TimePoint timePoint) throws NoWindException {
        return getFullLeaderboard().getNetPoints(competitor, raceColumn, raceColumnsToConsider, timePoint);
    }

    public Double getNetPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint,
            Set<RaceColumn> discardedRaceColumns) {
        return getFullLeaderboard().getNetPoints(competitor, raceColumn, timePoint, discardedRaceColumns);
    }

    public TimePoint getNowMinusDelay() {
        return getFullLeaderboard().getNowMinusDelay();
    }

    public CourseArea getDefaultCourseArea() {
        return getFullLeaderboard().getDefaultCourseArea();
    }

    public NumberOfCompetitorsInLeaderboardFetcher getNumberOfCompetitorsInLeaderboardFetcher() {
        return getFullLeaderboard().getNumberOfCompetitorsInLeaderboardFetcher();
    }

    public Pair<RaceColumn, Fleet> getRaceColumnAndFleet(TrackedRace trackedRace) {
        return getFullLeaderboard().getRaceColumnAndFleet(trackedRace);
    }

    public BoatClass getBoatClass() {
        return getFullLeaderboard().getBoatClass();
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor, RaceColumn raceColumn, Fleet fleet) {
        return getFullLeaderboard().getBoatOfCompetitor(competitor, raceColumn, fleet);
    }

    private RegattaLeaderboard getFullLeaderboard() {
        if (fullLeaderboard == null) {
            if (fullLeaderboardSupplier == null) {
                throw new NullPointerException("Internal error: Regatta leaderboard supplier is null; this can only happen upon premature serialization");
            }
            fullLeaderboard = fullLeaderboardSupplier.get();
            if (fullLeaderboard != null) {
                for (Iterator<Consumer<RegattaLeaderboard>> i=triggerWhenFullLeaderboardIsResolved.keySet().iterator(); i.hasNext(); ) {
                    final Consumer<RegattaLeaderboard> toTrigger = i.next();
                    toTrigger.accept(fullLeaderboard);
                    i.remove();
                }
            }
        }
        return fullLeaderboard;
    }
    
    /**
     * Before being serialized, ensure that the leaderboard supplier has been used to tru
     * to resolve the leaderboard.
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        getFullLeaderboard();
        oos.defaultWriteObject();
    }

    @Override
    public Iterable<Boat> getBoatsRegisteredInRegattaLog() {
        return getFullLeaderboard().getBoatsRegisteredInRegattaLog();
    }

    @Override
    public Iterable<Boat> getAllBoats() {
        return getFullLeaderboard().getAllBoats();
    }

    @Override
    public void registerBoat(Boat boat) {
        getFullLeaderboard().registerBoat(boat);
    }

    @Override
    public void registerBoats(Iterable<Boat> boats) {
        getFullLeaderboard().registerBoats(boats);
    }

    @Override
    public void deregisterBoat(Boat boat) {
        getFullLeaderboard().deregisterBoat(boat);
    }

    @Override
    public void deregisterBoats(Iterable<Boat> boats) {
        getFullLeaderboard().deregisterBoats(boats);
    }
}
