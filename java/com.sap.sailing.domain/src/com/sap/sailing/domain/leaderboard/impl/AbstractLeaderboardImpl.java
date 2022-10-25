package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ResultsAreOfficialFinder;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterBoatEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogBoatDeregistrator;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogBoatsInLogAnalyzer;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorDeregistrator;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.HasRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.leaderboard.RankComparable;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Abstract leaderboard implementation that already knows about carried points, competitor display name re-keying, score
 * corrections and result discarding rules. It manages a set of registered {@link RaceColumnListener}s and is itself
 * one. All events received this way are forwarded to all {@link RaceColumnListener}s subscribed. This can be used to
 * subscribe a concrete leaderboard implementation to the data structure providing the actual race columns in order to
 * be notified whenever the set of {@link TrackedRace}s attached to the leaderboard changes.
 *
 * @author Axel Uhl (D043530)
 *
 */
public abstract class AbstractLeaderboardImpl extends AbstractSimpleLeaderboardImpl implements
        HasRaceColumnsAndRegattaLike {
    private static final long serialVersionUID = -328091952760083438L;

    /**
     * Cache for the combined competitors of this leaderboard; taken from the {@link TrackedRace#getRace() races of the
     * tracked races} associated with this leaderboard. Updated when the set of tracked races changes.
     */
    private transient CompetitorProviderFromRaceColumnsAndRegattaLike competitorsProvider;

    private final AbstractLogEventAuthor regattaLogEventAuthorForAbstractLeaderboard = new LogEventAuthorImpl(
            AbstractLeaderboardImpl.class.getName(), 0);

    /**
     * @param scoreComparator
     *            the comparator to use to compare basic scores, such as net points
     * @param name
     *            must not be <code>null</code>
     */
    public AbstractLeaderboardImpl(ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(resultDiscardingRule);
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
    public Iterable<TrackedRace> getTrackedRaces() {
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

    /**
     * This default implementation collects all competitors by visiting all {@link TrackedRace}s associated with this
     * leaderboard's columns (see {@link #getTrackedRaces()}) and considering the race and regatta logs.
     */
    @Override
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        return getOrCreateCompetitorsProvider().getAllCompetitorsWithRaceDefinitionsConsidered();
    }

    @Override
    public Iterable<Boat> getAllBoats() {
        return getOrCreateCompetitorsProvider().getAllBoats();
    }

    @Override
    public Iterable<Competitor> getAllCompetitors(RaceColumn raceColumn, Fleet fleet) {
        return getOrCreateCompetitorsProvider().getAllCompetitors(raceColumn, fleet);
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor, RaceColumn raceColumn, Fleet fleet) {
        return fleet == null ? null : raceColumn.getAllCompetitorsAndTheirBoats(fleet).get(competitor);
    }

    @Override
    public CompetitorProviderFromRaceColumnsAndRegattaLike getOrCreateCompetitorsProvider() {
        if (competitorsProvider == null) {
            competitorsProvider = new CompetitorProviderFromRaceColumnsAndRegattaLike(this);
        }
        return competitorsProvider;
    }

    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        for (Competitor competitor : getAllCompetitors()) {
            if (competitor.getId().toString().equals(idAsString)) {
                return competitor;
            }
        }
        return null;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final TrackedRace trackedRace = race.getTrackedRace(competitor);
        final int trackedRank;
        if (trackedRace == null || trackedRace.hasStarted(timePoint) == false) {
            // return 0 if the race has not started or no tracked race is available
            trackedRank = 0;
        } else {
            // if the race has no CrossFleetMergedRanking we can use the ranks of each race to compute the scores.
            // if on the other hand the flag is set, we first need to order the competitors after their rankComparables.
            // After that we can assign scores.
            if (race.hasCrossFleetMergedRanking()) {
                // group fleets by their ordering in order to check if the hasCrossFleetMergedRanking has an effect or
                // is irrelevant.
                HashMap<Integer, List<Fleet>> fleetsByRank = new HashMap<>();
                for (Fleet fleet : race.getFleets()) {
                    if (fleetsByRank.get(fleet.getOrdering()) == null) {
                        fleetsByRank.put(fleet.getOrdering(), Arrays.asList(fleet));
                    } else {
                        fleetsByRank.get(fleet.getOrdering()).add(fleet);
                    }
                }
                if (fleetsByRank.get(race.getFleetOfCompetitor(competitor).getOrdering()).size() > 1) {
                    // more than one fleet of the same rank -> need to merge these fleets
                    List<CompetitorAndRankComparable> competitorsFromBestToWorstAndRankComparable = new ArrayList<>();
                    for (Fleet fleet : fleetsByRank.get(race.getFleetOfCompetitor(competitor).getOrdering())) {
                        List<CompetitorAndRankComparable> currentCompetitorsAndRankcomparables = race
                                .getTrackedRace(fleet).getCompetitorsFromBestToWorstAndRankComparable(timePoint);
                        competitorsFromBestToWorstAndRankComparable.addAll(currentCompetitorsAndRankcomparables);
                    }
                    // A merge sort might be faster because the Lists in competitorsFromBestToWorstAndRankComparable are
                    // already ordered
                    Collections.sort(competitorsFromBestToWorstAndRankComparable);
                    Iterator<Competitor> competitorsIterator = competitorsFromBestToWorstAndRankComparable.stream()
                            .map(v -> v.getCompetitor()).iterator();
                    trackedRank = improveByDisqualificationsOfBetterRankedCompetitors(competitor, race, timePoint,
                            competitorsIterator);
                } else {
                    // just one fleet for the given rank -> no merge needed just use the normal behavior
                    trackedRank = improveByDisqualificationsOfBetterRankedCompetitors(competitor, race, timePoint,
                            trackedRace.getCompetitorsFromBestToWorst(timePoint, cache).iterator());
                }
            } else {
                trackedRank = improveByDisqualificationsOfBetterRankedCompetitors(competitor, race, timePoint,
                        trackedRace.getCompetitorsFromBestToWorst(timePoint, cache).iterator());
            }
        }
        return trackedRank;

    }

    /**
     * Per competitor disqualified ({@link ScoreCorrection} has a {@link MaxPointsReason} for the competitor that has
     * <code>{@link MaxPointsReason#isAdvanceCompetitorsTrackedWorse()}==true</code>) and those suppressed, all
     * competitors ranked worse by the tracking system need to have their rank corrected by one. The {@link RankComparable} must also
     * consider the {@link MaxPointsReason} so that the behavior remains consistent.
     *
     * @param competitor
     *            the competitor whose rank is to be improved
     * @param race           
     *            raceColumn that is used to check if a competitor is disqualified
     * @param timePoint
     *            time point at which to consider disqualifications (not used yet because currently we don't remember
     *            <em>when</em> a competitor was disqualified)
     * @param competitorsFromBestToWorst
     *            An iterator that contains all competitors through which the rank of the given competitor can be improved. 
     *
     * @return the unmodified <code>Pair(Rank, {@link RankComparable}</code> if no disqualifications for better-ranked competitors exist for
     *         <code>race</code>, or otherwise a <code>Pair(Rank, {@link RankComparable}</code> where the Rank is improved (lowered) by the number of disqualifications of
     *         competitors whose tracked rank is better (lower) than <code>rank</code> while the {@link RankComparable} is consistent with the new Rank.
     */
    private int improveByDisqualificationsOfBetterRankedCompetitors( Competitor competitor, RaceColumn race,
            TimePoint timePoint, Iterator<Competitor> competitorsFromBestToWorst) {
        int rank = 1; 
        int numberOfDisqualificationsOfBetterRankedCompetitors = 0; 
        while(competitorsFromBestToWorst.hasNext()) {
            Competitor currentCompetitor = competitorsFromBestToWorst.next();
            if(competitor.equals(currentCompetitor)) {
                break;
            }
            MaxPointsReason maxPointsReasonForBetterCompetitor = getScoreCorrection()
                    .getMaxPointsReason(currentCompetitor, race, timePoint);
            if (isSuppressed(currentCompetitor) || (maxPointsReasonForBetterCompetitor != null
                    && maxPointsReasonForBetterCompetitor != MaxPointsReason.NONE
                    && maxPointsReasonForBetterCompetitor.isAdvanceCompetitorsTrackedWorse())) {
                numberOfDisqualificationsOfBetterRankedCompetitors ++;
            }
            rank++; 
        }
        return rank - numberOfDisqualificationsOfBetterRankedCompetitors; 
    }

    // Note: no need to redefine isMedalRaceChanged because that doesn't affect the competitorsCache

    @Override
    public Long getDelayToLiveInMillis() {
        TimePoint startOfLatestRace = null;
        Long delayToLiveInMillisForLatestRace = null;
        for (RaceColumn raceColumn : getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    if (startOfLatestRace == null
                            || (trackedRace.getStartOfRace() != null && trackedRace.getStartOfRace().compareTo(
                                    startOfLatestRace) > 0)) {
                        delayToLiveInMillisForLatestRace = trackedRace.getDelayToLiveInMillis();
                    }
                }
            }
        }
        return delayToLiveInMillisForLatestRace;
    }

    @Override
    public RaceLog getRacelog(String raceColumnName, String fleetName) {
        RaceColumn raceColumn = getRaceColumnByName(raceColumnName);
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        return raceColumn.getRaceLog(fleet);
    }

    @Override
    public Iterable<Competitor> getCompetitorsRegisteredInRegattaLog() {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        CompetitorsInLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> analyzer = new CompetitorsInLogAnalyzer<>(
                regattaLog);
        return analyzer.analyze();
    }

    @Override
    public void registerCompetitor(Competitor competitor) {
        registerCompetitors(Collections.singletonList(competitor));
    }

    @Override
    public void registerCompetitors(Iterable<Competitor> competitors) {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        TimePoint now = MillisecondsTimePoint.now();

        for (Competitor competitor: competitors) {
            regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(now, now, regattaLogEventAuthorForAbstractLeaderboard,
                    UUID.randomUUID(), competitor));
        }
    }

    @Override
    public void deregisterCompetitor(Competitor competitor) {
        deregisterCompetitors(Collections.singleton(competitor));
    }

    @Override
    public void deregisterCompetitors(Iterable<Competitor> competitors) {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        CompetitorDeregistrator<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> deregisterer = new CompetitorDeregistrator<>(regattaLog, competitors, regattaLogEventAuthorForAbstractLeaderboard);
        deregisterer.deregister(deregisterer.analyze());
    }

    // boat functions
    @Override
    public Iterable<Boat> getBoatsRegisteredInRegattaLog() {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        RegattaLogBoatsInLogAnalyzer<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> analyzer = new RegattaLogBoatsInLogAnalyzer<>(
                regattaLog);
        return analyzer.analyze();
    }

    @Override
    public void registerBoat(Boat boat) {
        registerBoats(Collections.singleton(boat));
    }

    @Override
    public void registerBoats(Iterable<Boat> boats) {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        TimePoint now = MillisecondsTimePoint.now();

        for (Boat boat : boats) {
            regattaLog.add(new RegattaLogRegisterBoatEventImpl(now, now, regattaLogEventAuthorForAbstractLeaderboard,
                    UUID.randomUUID(), boat));
        }
    }

    @Override
    public void deregisterBoat(Boat boat) {
        deregisterBoats(Collections.singleton(boat));
    }

    @Override
    public void deregisterBoats(Iterable<Boat> boats) {
        RegattaLog regattaLog = getRegattaLike().getRegattaLog();
        RegattaLogBoatDeregistrator<RegattaLog, RegattaLogEvent, RegattaLogEventVisitor> deregisterer = new RegattaLogBoatDeregistrator<>(regattaLog, boats, regattaLogEventAuthorForAbstractLeaderboard);
        deregisterer.deregister(deregisterer.analyze());
    }

    @Override
    public boolean isResultsAreOfficial(RaceColumn raceColumn, Fleet fleet) {
        final RaceLog raceLog = raceColumn.getRaceLog(fleet);
        return new ResultsAreOfficialFinder(raceLog).analyze() != null;
    }
}
