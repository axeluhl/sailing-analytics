package com.sap.sailing.server.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    /**
     * See also bug 3794: make sure that different variants of scores and max points reasons / penalties are applied to the
     * leaderboard.
     */
    @Test
    public void testApplicationOfScoresFromRaceLog() throws NoWindException {
        final RacingEventService service = new RacingEventServiceImpl();
        final List<Competitor> competitors = new ArrayList<>();
        for (int i=0; i<20; i++) {
            final String competitorName = "C"+i;
            competitors.add(service.getBaseDomainFactory().getCompetitorStore().getOrCreateCompetitor(UUID.randomUUID(),
                    competitorName, "c", /* displayColor */ Color.RED, /* email */ null, /* flagImageURI */ null,
                    new TeamImpl("STG", Collections.singleton(
                            new PersonImpl(competitorName, new NationalityImpl("GER"),
                            /* dateOfBirth */ null, "This is famous "+competitorName)),
                            new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                            /* dateOfBirth */null, "This is Rigo, the coach")), new BoatImpl(competitorName + "'s boat",
                    new BoatClassImpl("505", /* typicallyStartsUpwind */ true), /* sailID */ null),
                    /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null));
        }
        final Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, /* medal */ 0, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        TrackedRace f1 = new MockedTrackedRaceWithStartTimeAndRanks(now, competitors);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        f1Column.setTrackedRace(f1Column.getFleets().iterator().next(), f1);
        final Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        
        service.addLeaderboard(leaderboard); // should add a RaceLogScoringReplicator as listener to the leaderboard
        service.addRegattaWithoutReplication(regatta);
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
        for (final Competitor c : competitors) {
            assertEquals(scores.get(c)==null?competitors.indexOf(c)+1:scores.get(c), leaderboard.getTotalPoints(c, f1Column, now), 0.00000001);
        }
        final List<Competitor> expectedNewOrder = new ArrayList<>(competitors);
        expectedNewOrder.sort((c1, c2)->
            new Double(scores.get(c1)==null?competitors.indexOf(c1)+1:scores.get(c1)).compareTo(
            new Double(scores.get(c2)==null?competitors.indexOf(c2)+1:scores.get(c2))));
        final List<Competitor> rankedCompetitorsAfterApplying = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(expectedNewOrder, rankedCompetitorsAfterApplying);
    }


}
