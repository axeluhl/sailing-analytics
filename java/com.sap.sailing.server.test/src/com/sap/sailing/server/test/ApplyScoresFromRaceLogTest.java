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
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultImpl;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
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
    private RacingEventService service;
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
            competitors.add(service.getBaseDomainFactory().getCompetitorStore().getOrCreateCompetitor(UUID.randomUUID(),
                    competitorName, /* displayColor */ Color.RED, /* email */ null, /* flagImageURI */ null,
                    new TeamImpl("STG", Collections.singleton(
                            new PersonImpl(competitorName, new NationalityImpl("GER"),
                            /* dateOfBirth */ null, "This is famous "+competitorName)),
                            new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                            /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                    new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null),
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
        for (final Competitor c : competitors) {
            final MaxPointsReason mpr = new MaxPointsReason[] { null, MaxPointsReason.NONE, MaxPointsReason.DNF, MaxPointsReason.OCS }[oneBasedRank%4];
            final Double score = oneBasedRank%5 == 0 ? null : 20*Math.random();
            scores.put(c, score);
            mprs.put(c, mpr);
            results.add(new CompetitorResultImpl(c.getId(), c.getName(),
                    oneBasedRank++, mpr, score, /* finishingTime */ null, /* comment */ null));
        }
        final RaceLog f1RaceLog = f1Column.getRaceLog(f1Column.getFleets().iterator().next());
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Axel", 0);
        final RaceState f1RaceState = new RaceStateImpl(service, f1RaceLog, author,
                new RacingProcedureFactoryImpl(author, new EmptyRegattaConfiguration()));
        f1RaceState.setFinishPositioningListChanged(now, results);
        final List<Competitor> rankedCompetitorsBeforeApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitorsBeforeApplying); // no effects of preliminary results list yet
        f1RaceState.setFinishPositioningConfirmed(now);
        final Function<Competitor, Double> expectedPoints = (c)->scores.get(c)==null?(mprs.get(c) == null || mprs.get(c) == MaxPointsReason.NONE ? competitors.indexOf(c)+1 : competitors.size()+1):scores.get(c);
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
        f1RaceState.setFinishPositioningConfirmed(now);
        
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
     * See bug 4025: When a scoring race log event is received it may contain a partial competitor set. It shall depend on
     * the previous state resulting from the race log, without the last event, what needs to be done with the leaderboard
     * score corrections. If a competitor which which an earlier message did have a correction now no longer is part of
     * the latest scoring race log event, and if the score corrections in the leaderboard match up with what the previous
     * event had caused, remove that score correction again.
     */
    @Test
    public void testApplyingOCSThenClearingOCS() {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        setUp(3, now, ScoringSchemeType.LOW_POINT);
        int oneBasedRank = 1;
        final CompetitorResults results = new CompetitorResultsImpl();
        // set OCS
        setResultForCompetitor(competitors.get(0), oneBasedRank++, results, MaxPointsReason.OCS, /* explicit score */ null);
        final RaceLog f1RaceLog = f1Column.getRaceLog(f1Column.getFleets().iterator().next());
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Axel", 0);
        final RaceState f1RaceState = new RaceStateImpl(service, f1RaceLog, author,
                new RacingProcedureFactoryImpl(author, new EmptyRegattaConfiguration()));
        f1RaceState.setFinishPositioningListChanged(now, results);
        final List<Competitor> rankedCompetitorsBeforeApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitorsBeforeApplying); // no effects of preliminary results list yet
        f1RaceState.setFinishPositioningConfirmed(now);
        // validate that it arrived in leaderboard
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.OCS, 4, /* score is corrected */ false, later);
        
        // now clear OCS again:
        final CompetitorResults resultsWithOCSCleared = new CompetitorResultsImpl();
        f1RaceState.setFinishPositioningListChanged(later, resultsWithOCSCleared);
        f1RaceState.setFinishPositioningConfirmed(later);
        // validate that it got cleared in leaderboard
        assertScoreCorrections(leaderboard, f1Column, competitors.get(0), MaxPointsReason.NONE, 0, /* score is corrected */ false, later);
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
        results.add(new CompetitorResultImpl(competitor.getId(), competitor.getName(), oneBasedRank++, maxPointsReason, explicitScore, /* finishingTime */ null, /* comment */ null));
    }
    
    

}
