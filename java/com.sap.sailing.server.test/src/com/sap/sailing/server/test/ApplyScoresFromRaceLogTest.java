package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.CompetitorResult.MergeState;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.test.LeaderboardScoringAndRankingTestBase;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sse.common.Color;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ApplyScoresFromRaceLogTest extends LeaderboardScoringAndRankingTestBase {
    protected RacingEventService service;
    private Regatta regatta;
    private RaceColumn f1Column;
    private List<Competitor> competitors;
    private Leaderboard leaderboard;
    
    @Before
    public void setUp() {
        service = new RacingEventServiceImpl();
    }
    
    private void setUp(int numberOfCompetitors, TimePoint now, ScoringSchemeType scoringScheme) {
        competitors = new ArrayList<>();
        for (int i=0; i<numberOfCompetitors; i++) {
            final String competitorName = "C"+i;
            competitors.add(service.getBaseDomainFactory().getCompetitorAndBoatStore().getOrCreateCompetitor(UUID.randomUUID(),
                    competitorName, "c", /* displayColor */ Color.RED, /* email */ null, /* flagImageURI */ null,
                    new TeamImpl("STG", Collections.singleton(
                            new PersonImpl(competitorName, new NationalityImpl("GER"),
                            /* dateOfBirth */ null, "This is famous "+competitorName)),
                            new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                            /* dateOfBirth */null, "This is Rigo, the coach")),
                    /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null));
        }
        regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, /* medal */ 0, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(scoringScheme));
        TrackedRace f1 = new MockedTrackedRaceWithStartTimeAndRanks(now, competitors);
        f1Column = series.get(1).getRaceColumnByName("F1");
        f1Column.setTrackedRace(f1Column.getFleets().iterator().next(), f1);
        leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        service.addLeaderboard(leaderboard); // should add a RaceLogScoringReplicator as listener to the leaderboard
        service.addRegattaWithoutReplication(regatta);
    }
    
    /**
     * See also bug 3794: make sure that different variants of scores and max points reasons / penalties are applied to the
     * leaderboard.
     */
    @Test
    public void testApplicationOfScoresFromRaceLog() throws NoWindException {
        final TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        setUp(20, now, ScoringSchemeType.LOW_POINT);
        final Map<Competitor, Double> scores = new HashMap<>();
        final Map<Competitor, MaxPointsReason> mprs = new HashMap<>();
        int oneBasedRank = 1;
        final CompetitorResults results = new CompetitorResultsImpl();
        Boat storedBoat = DomainFactory.INSTANCE.getOrCreateBoat(UUID.randomUUID(), "SAP Extreme Sailing Team",
                new BoatClassImpl("X40", false), "123", Color.RED);
        for (final Competitor c : competitors) {
            final MaxPointsReason mpr = new MaxPointsReason[] { null, MaxPointsReason.NONE, MaxPointsReason.DNF, MaxPointsReason.OCS }[oneBasedRank%4];
            final Double score = oneBasedRank%5 == 0 ? null : 20*Math.random();
            scores.put(c, score);
            mprs.put(c, mpr);
            results.add(new CompetitorResultImpl(c.getId(), c.getName(), c.getShortName(), storedBoat.getName(),
                    storedBoat.getSailID(), oneBasedRank++, mpr, score, /* finishingTime */ null, /* comment */ null,
                    MergeState.OK));
        }
        final RaceLog f1RaceLog = f1Column.getRaceLog(f1Column.getFleets().iterator().next());
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Axel", 0);
        final RaceState f1RaceState = new RaceStateImpl(service, f1RaceLog, author,
                new RacingProcedureFactoryImpl(author, new EmptyRegattaConfiguration()));
        f1RaceState.setFinishPositioningListChanged(now, results);
        final List<Competitor> rankedCompetitorsBeforeApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitorsBeforeApplying); // no effects of preliminary results list yet
        f1RaceState.setFinishPositioningConfirmed(now, results);
        final Function<Competitor, Double> expectedPoints =
                (c)->scores.get(c)==null?(mprs.get(c) == null || mprs.get(c) == MaxPointsReason.NONE ?
                        competitors.indexOf(c)+1 : competitors.size()+1):scores.get(c);
        for (final Competitor c : competitors) {
            assertEquals(expectedPoints.apply(c), leaderboard.getTotalPoints(c, f1Column, now), 0.00000001);
        }
        final List<Competitor> expectedNewOrder = new ArrayList<>(competitors);
        expectedNewOrder.sort((c1, c2)->new Double(expectedPoints.apply(c1)).compareTo(new Double(expectedPoints.apply(c1))));
        final List<Competitor> rankedCompetitorsAfterApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        double lastScore = 0;
        for (final Competitor c : rankedCompetitorsAfterApplying) {
            assertEquals(expectedPoints.apply(c), leaderboard.getTotalPoints(c, f1Column, later));
            assertTrue(leaderboard.getTotalPoints(c, f1Column, later) >= lastScore);
            lastScore = leaderboard.getTotalPoints(c, f1Column, later);
        }
    }

    /**
     * See also bug 3955: when for a competitor no score is explicitly provided but a {@link MaxPointsReason} has been set, don't
     * correct the score in the leaderboard's score correction but only set the {@link MaxPointsReason}. This will let the scoring
     * scheme select the score, as it used to be before the fix for bug 3794, commit 830e64842a39fb137446887c5177f7de34bd5b5a.
     */
    @Test
    public void testScoresFromRaceLogOnlyAppliedIfExplicitOrNoMaxPointsReason() throws NoWindException {
        final TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        setUp(8, now, ScoringSchemeType.HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT);
        int oneBasedRank = 1;
        final CompetitorResults results = new CompetitorResultsImpl();
        
        setResultForCompetitor(competitors.get(0), oneBasedRank++, results, MaxPointsReason.DNF, /* explicit score */ null);
        setResultForCompetitor(competitors.get(1), oneBasedRank++, results, MaxPointsReason.OCS, /* explicit score */ null);
        setResultForCompetitor(competitors.get(2), oneBasedRank++, results, MaxPointsReason.DNF, /* explicit score */ 0.1);
        setResultForCompetitor(competitors.get(3), oneBasedRank++, results, MaxPointsReason.OCS, /* explicit score */ 0.2);
        setResultForCompetitor(competitors.get(4), oneBasedRank++, results, /* maxPointsReason */ null, /* explicit score */ null);
        setResultForCompetitor(competitors.get(5), oneBasedRank++, results, /* maxPointsReason */ null, /* explicit score */ 2.4);
        setResultForCompetitor(competitors.get(6), oneBasedRank++, results, MaxPointsReason.NONE, /* explicit score */ null);
        setResultForCompetitor(competitors.get(7), oneBasedRank++, results, MaxPointsReason.NONE, /* explicit score */ 3.3);

        final RaceLog f1RaceLog = f1Column.getRaceLog(f1Column.getFleets().iterator().next());
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Axel", 0);
        final RaceState f1RaceState = new RaceStateImpl(service, f1RaceLog, author,
                new RacingProcedureFactoryImpl(author, new EmptyRegattaConfiguration()));
        f1RaceState.setFinishPositioningListChanged(now, results);
        final List<Competitor> rankedCompetitorsBeforeApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitorsBeforeApplying); // no effects of preliminary results list yet
        f1RaceState.setFinishPositioningConfirmed(now, results);
        
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.DNF,    0, /* score is corrected */ false, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(1), MaxPointsReason.OCS,    0, /* score is corrected */ false, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(2), MaxPointsReason.DNF,  0.1, /* score is corrected */  true, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(3), MaxPointsReason.OCS,  0.2, /* score is corrected */  true, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(4), MaxPointsReason.NONE,   6, /* score is corrected */  true, later); // score corrected based on rank because no MaxPointsReason set
        assertScoreCorrections(leaderboard, f1Column, competitors.get(5), MaxPointsReason.NONE, 2.4, /* score is corrected */  true, later); // score corrected based on explicit score
        assertScoreCorrections(leaderboard, f1Column, competitors.get(6), MaxPointsReason.NONE,   4, /* score is corrected */  true, later); // score corrected based on rank because MaxPointsReason.NONE set
        assertScoreCorrections(leaderboard, f1Column, competitors.get(7), MaxPointsReason.NONE, 3.3, /* score is corrected */  true, later); // score corrected based on explicit score
    }
    
    /**
     * See bugs 4025 and 4167: When a scoring race log event is received it may contain a partial competitor set. Before bug4167
     * there was an implementation that reset score corrections if previous messages had a result for a competitor, and later
     * messages did not. With bug 4167 we found that it should be possible to send in partial results, owed to the insight that
     * multiple devices may be used for the same race, not all in sync at all times, therefore producing partial results that
     * should not clear or overwrite valid results input by other devices.<p>
     * 
     * Therefore, now, with bug4167, this test has been modified such that first it ensures that a partial result
     * does <em>not</em> clear a previous score correction. Furthermore it tests that when sending a result that
     * has 0 as the rank and {@link MaxPointsReason#NONE} as the {@link MaxPointsReason} with no explicit score set,
     * the score correction for the competitor will be reset in the leaderboard.
     */
    @Test
    public void testApplyingOCSThenClearingOCS() {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = now.plus(1000);
        TimePoint yetLater = later.plus(1000);
        final int numberOfCompetitors = 2;
        setUp(numberOfCompetitors, now, ScoringSchemeType.LOW_POINT);
        final CompetitorResults results = new CompetitorResultsImpl();
        // set OCS
        setResultForCompetitor(competitors.get(0), /* one-based rank */ 0, results, MaxPointsReason.OCS, /* explicit score */ null);
        setResultForCompetitor(competitors.get(1), /* one-based rank */ 0, results, MaxPointsReason.OCS, /* explicit score */ null);
        final RaceLog f1RaceLog = f1Column.getRaceLog(f1Column.getFleets().iterator().next());
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Axel", 0);
        final RaceState f1RaceState = new RaceStateImpl(service, f1RaceLog, author,
                new RacingProcedureFactoryImpl(author, new EmptyRegattaConfiguration()));
        f1RaceState.setFinishPositioningListChanged(now, results);
        final List<Competitor> rankedCompetitorsBeforeApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitorsBeforeApplying); // no effects of preliminary results list yet
        f1RaceState.setFinishPositioningConfirmed(now, results);
        // validate that it arrived in leaderboard
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.OCS, numberOfCompetitors+1, /* score is corrected */ false, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(1), MaxPointsReason.OCS, numberOfCompetitors+1, /* score is corrected */ false, later);
        
        // now clear OCS for first competitor again and leave second competitor OCS:
        final CompetitorResults resultsWithOCSCleared = new CompetitorResultsImpl();
        setResultForCompetitor(competitors.get(1), /* one-based rank */ 0, resultsWithOCSCleared, MaxPointsReason.OCS, /* explicit score */ null);
        f1RaceState.setFinishPositioningListChanged(later, resultsWithOCSCleared);
        f1RaceState.setFinishPositioningConfirmed(later, resultsWithOCSCleared);
        // validate that it did not get cleared in leaderboard (changed by bug 4167):
        assertScoreCorrections(leaderboard, f1Column, competitors.get(1), MaxPointsReason.OCS, numberOfCompetitors+1, /* score is corrected */ false, later);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.OCS, numberOfCompetitors+1, /* score is corrected */ false, later); // ranking first, one point in low-point scheme
        // now explicitly overwrite with a 0/null/null result, validating that this will clear the corrections for competitor 0:
        setResultForCompetitor(competitors.get(0), /* one-based rank */ 0, resultsWithOCSCleared, /* maxPointsReason */ null, /* explicit score */ null);
        f1RaceState.setFinishPositioningListChanged(yetLater, resultsWithOCSCleared);
        f1RaceState.setFinishPositioningConfirmed(yetLater, resultsWithOCSCleared);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(1), MaxPointsReason.OCS, numberOfCompetitors+1, /* score is corrected */ false, yetLater);
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.NONE, 1, /* score is corrected */ false, yetLater); // ranking first, one point in low-point scheme
    }

    private void assertScoreCorrections(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor,
            MaxPointsReason expectedMaxPointsReason, double expectedScore, boolean expectedIsScoreCorrected,
            TimePoint timePoint) {
        assertEquals(expectedIsScoreCorrected, leaderboard.getScoreCorrection().getExplicitScoreCorrection(competitor, raceColumn) != null);
        assertEquals(expectedMaxPointsReason, leaderboard.getScoreCorrection().getMaxPointsReason(competitor, raceColumn, timePoint));
        assertEquals(expectedScore, leaderboard.getTotalPoints(competitor, raceColumn, timePoint), 0.000001);
    }

    private void setResultForCompetitor(final Competitor competitor, int oneBasedRank,
            final CompetitorResults results, MaxPointsReason maxPointsReason, Double explicitScore) {
        Boat storedBoat = DomainFactory.INSTANCE.getOrCreateBoat(UUID.randomUUID(), "SAP Extreme Sailing Team",
                new BoatClassImpl("X40", false), "123", Color.RED);
        results.add(new CompetitorResultImpl(competitor.getId(), competitor.getName(),
                competitor.getShortName(), storedBoat.getName(), storedBoat.getSailID(), oneBasedRank,
                maxPointsReason, explicitScore, /* finishingTime */ null, /* comment */ null, MergeState.OK));
    }
}
