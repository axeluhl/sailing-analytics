package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowerScoreIsBetter;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LeaderboardScoringAndRankingTest extends AbstractLeaderboardTest {
    private ArrayList<Series> series;
    
    private Leaderboard createLeaderboard(Regatta regatta, int[] discardingThresholds) {
        ScoreCorrectionImpl scoreCorrections = new ScoreCorrectionImpl();
        ResultDiscardingRuleImpl discardingRules = new ResultDiscardingRuleImpl(discardingThresholds);
        return new RegattaLeaderboardImpl(regatta, scoreCorrections, discardingRules, new LowerScoreIsBetter());
    }

    @Test
    public void testOneStartedRaceWithDifferentScores() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        TrackedRace f1 = new MockedTrackedRaceWithStartTimeAndRanks(now, competitors);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        f1Column.setTrackedRace(f1Column.getFleets().iterator().next(), f1);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors, rankedCompetitors);
    }

    /**
     * Asserts that the competitors ranking worse than the disqualified competitor advance by one in the
     * {@link Leaderboard#getCompetitorsFromBestToWorst(TimePoint)} ordering. Note that this does not test
     * the total points given for those competitors.
     */
    @Test
    public void testOneStartedRaceWithDifferentScoresAndDisqualification() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        Series finalSeries;
        Iterator<? extends Series> seriesIter = regatta.getSeries().iterator();
        seriesIter.next();
        finalSeries = seriesIter.next();
        leaderboard.getScoreCorrection().setMaxPointsReason(competitors.get(5), finalSeries.getRaceColumnByName("F1"), MaxPointsReason.DSQ);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        TrackedRace f1 = new MockedTrackedRaceWithStartTimeAndRanks(now, competitors);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        f1Column.setTrackedRace(f1Column.getFleets().iterator().next(), f1);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(competitors.subList(0, 5), rankedCompetitors.subList(0, 5));
        assertEquals(competitors.subList(6, 10), rankedCompetitors.subList(5, 9));
        assertEquals(competitors.get(5), rankedCompetitors.get(9));
        
        // Now test the total points and make sure the other competitors advanced by one, too
        assertEquals(11, leaderboard.getTotalPoints(competitors.get(5), f1Column, now));
        for (int i=0; i<5; i++) {
            assertEquals(i+1, leaderboard.getTotalPoints(competitors.get(i), f1Column, now));
        }
        for (int i=6; i<10; i++) {
            assertEquals(i, leaderboard.getTotalPoints(competitors.get(i), f1Column, now));
        }
    }

    @Test
    public void testDistributionAcrossQualifyingFleetsWithDifferentScores() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        List<Competitor> yellow = new ArrayList<Competitor>();
        List<Competitor> blue = new ArrayList<Competitor>();
        for (int i=0; i<5; i++) {
            yellow.add(competitors.get(2*i));
            blue.add(competitors.get(2*i+1));
        }
        Regatta regatta = createRegatta(/* qualifying */1, new String[] { "Yellow", "Blue" }, /* final */0,
                new String[] { "Default" },
                /* medal */false, "testAllTrackedAndStartedWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        RaceColumn q1Column = series.get(0).getRaceColumnByName("Q1");
        TrackedRace q1Yellow = new MockedTrackedRaceWithStartTimeAndRanks(now, yellow);
        q1Column.setTrackedRace(q1Column.getFleetByName("Yellow"), q1Yellow);
        TrackedRace q1Blue = new MockedTrackedRaceWithStartTimeAndRanks(now, blue);
        q1Column.setTrackedRace(q1Column.getFleetByName("Blue"), q1Blue);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        for (int i=0; i<5; i++) {
            Competitor first = rankedCompetitors.get(2*i);
            Competitor second = rankedCompetitors.get(2*i+1);
            assertTrue(first == yellow.get(i) || first == blue.get(i));
            assertTrue(second == yellow.get(i) || second == blue.get(i));
        }
    }

    @Test
    public void testDistributionAcrossFinalFleetsWithDifferentScores() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        List<Competitor> gold = new ArrayList<Competitor>();
        List<Competitor> silver = new ArrayList<Competitor>();
        for (int i=0; i<5; i++) {
            gold.add(competitors.get(2*i));
            silver.add(competitors.get(2*i+1));
        }
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1, new String[] {
                "Gold", "Silver" },
        /* medal */false, "testDistributionAcrossFinalFleetsWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        TrackedRace f1Gold = new MockedTrackedRaceWithStartTimeAndRanks(now, gold);
        f1Column.setTrackedRace(f1Column.getFleetByName("Gold"), f1Gold);
        TrackedRace f1Silver = new MockedTrackedRaceWithStartTimeAndRanks(now, silver);
        f1Column.setTrackedRace(f1Column.getFleetByName("Silver"), f1Silver);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        for (int i=0; i<5; i++) {
            assertSame(gold.get(i), rankedCompetitors.get(i));
            assertSame(silver.get(i), rankedCompetitors.get(i+5));
        }
    }

    @Test
    public void testMedalTakesPrecedence() throws NoWindException {
        final int firstMedalCompetitorIndex = 3;
        List<Competitor> competitors = createCompetitors(20);
        List<Competitor> medal = competitors.subList(firstMedalCompetitorIndex, firstMedalCompetitorIndex+10);
        Regatta regatta = createRegatta(/* qualifying */ 0, new String[] { "Default" }, /* final */ 1, new String[] { "Default" },
                /* medal */ true, "testMedalTakesPrecedence",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */ true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        TrackedRace q1Default = new MockedTrackedRaceWithStartTimeAndRanks(now, competitors);
        f1Column.setTrackedRace(f1Column.getFleetByName("Default"), q1Default);
        TrackedRace medalTrackedRace = new MockedTrackedRaceWithStartTimeAndRanks(now, medal);
        RaceColumn medalColumn = series.get(2).getRaceColumnByName("M");
        medalColumn.setTrackedRace(medalColumn.getFleetByName("Medal"), medalTrackedRace);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        // medalists rank top
        for (int i=0; i<10; i++) {
            assertSame(medal.get(i), rankedCompetitors.get(i));
        }
        // others rank according to their non-medal ranking in the final round
        for (int i=10; i<competitors.size(); i++) {
            if (i<10+firstMedalCompetitorIndex) {
                assertSame(competitors.get(i-10), rankedCompetitors.get(i));
            } else {
                assertSame(competitors.get(i), rankedCompetitors.get(i));
            }
        }
    }

    @Test
    public void testTieBreakWithTwoVersusOneWins() throws NoWindException {
        Competitor[] c = createCompetitors(3).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[0], c[1], c[2] };
        Competitor[] f2 = new Competitor[] { c[0], c[1], c[2] };
        Competitor[] f3 = new Competitor[] { c[1], c[2], c[0] };
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */3, new String[] { "Default" },
        /* medal */ false, "testTieBreakWithTwoVersusOneWins",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later));
        assertEquals(Arrays.asList(new Competitor[] { c[0], c[1], c[2] }), rankedCompetitors);
    }

    @Test
    public void testTieBreakWithTwoVersusOneSeconds() throws NoWindException {
        Competitor[] c = createCompetitors(4).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[0] scores 18 points altogether
        Competitor[] f2 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[1] scores 18 points altogether but has only one second rank
        Competitor[] f3 = new Competitor[] { c[2], c[1], c[0], c[3] };
        Competitor[] f4 = new Competitor[] { c[3], c[2], c[0], c[1] };
        Competitor[] f5 = new Competitor[] { c[3], c[2], c[1], c[0] };
        Competitor[] f6 = new Competitor[] { c[3], c[2], c[1], c[0] };
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */6, new String[] { "Default" },
        /* medal */ false, "testTieBreakWithTwoVersusOneSeconds",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3, f4, f5, f6);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later));
        assertTrue(rankedCompetitors.indexOf(c[0]) == rankedCompetitors.indexOf(c[1])-1);
    }

    @Test
    public void testTieBreakByMedalRaceScoreOnlyIfEqualTotalScore() throws NoWindException {
        Competitor[] c = createCompetitors(2).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[1], c[0] };
        Competitor[] f2 = new Competitor[] { c[1], c[0] };
        Competitor[] m1 = new Competitor[] { c[0], c[1] };
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */2, new String[] { "Default" },
        /* medal */ true, "testTieBreakWithTwoVersusOneSeconds",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2);
        createAndAttachTrackedRaces(series.get(2), "Medal", m1);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        // assert that both have equal score
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later));
        // assert that c[0] ranks better than c[1] (reason: c[0] ranked better in medal race)
        assertEquals(rankedCompetitors.indexOf(c[0]), rankedCompetitors.indexOf(c[1])-1);
    }

    @Test
    public void testTieBreakWithEqualWinsAndTwoVersusOneSeconds() throws NoWindException {
        Competitor[] c = createCompetitors(4).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[0] scores 16
        Competitor[] f2 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[1] scores 16 points altogether but has only one second rank
        Competitor[] f3 = new Competitor[] { c[2], c[1], c[3], c[0] }; // c[2] scores  9
        Competitor[] f4 = new Competitor[] { c[3], c[2], c[0], c[1] }; // c[3] scores 29
        Competitor[] f5 = new Competitor[] { c[0], c[2], c[1], c[3] };
        Competitor[] f6 = new Competitor[] { c[1], c[2], c[3], c[0] };
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */6, new String[] { "Default" },
        /* medal */ false, "testTieBreakWithEqualWinsAndTwoVersusOneSeconds",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3, f4, f5, f6);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later));
        assertEquals(rankedCompetitors.indexOf(c[0]), rankedCompetitors.indexOf(c[1])-1);
    }

    private TimePoint createAndAttachTrackedRaces(Series theSeries, String fleetName, Competitor[]... competitorLists) {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        Iterator<? extends RaceColumn> columnIter = theSeries.getRaceColumns().iterator();
        for (Competitor[] competitorList : competitorLists) {
            RaceColumn raceColumn = columnIter.next();
            TrackedRace trackedRace = new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(competitorList));
            raceColumn.setTrackedRace(raceColumn.getFleetByName(fleetName), trackedRace);
        }
        return later;
    }

    private List<Competitor> createCompetitors(int numberOfCompetitorsToCreate) {
        List<Competitor> result = new ArrayList<Competitor>();
        for (int i=1; i<=numberOfCompetitorsToCreate; i++) {
            result.add(createCompetitor("C"+i));
        }
        return result;
    }

    private Regatta createRegatta(final int numberOfQualifyingRaces, String[] qualifyingFleetNames, final int numberOfFinalRaces,
            String[] finalFleetNames, boolean medalRaceAndSeries, final String regattaBaseName, BoatClass boatClass) {
        series = new ArrayList<Series>();
        
        // -------- qualifying series ------------
        List<Fleet> qualifyingFleets = new ArrayList<Fleet>();
        for (String qualifyingFleetName : qualifyingFleetNames) {
            qualifyingFleets.add(new FleetImpl(qualifyingFleetName));
        }
        List<String> qualifyingRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfQualifyingRaces; i++) {
            qualifyingRaceColumnNames.add("Q"+i);
        }
        Series qualifyingSeries = new SeriesImpl("Qualifying", /* isMedal */false, qualifyingFleets,
                qualifyingRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(qualifyingSeries);
        
        // -------- final series ------------
        List<Fleet> finalFleets = new ArrayList<Fleet>();
        int fleetOrdering = 1;
        for (String finalFleetName : finalFleetNames) {
            finalFleets.add(new FleetImpl(finalFleetName, fleetOrdering++));
        }
        List<String> finalRaceColumnNames = new ArrayList<String>();
        for (int i=1; i<=numberOfFinalRaces; i++) {
            finalRaceColumnNames.add("F"+i);
        }
        Series finalSeries = new SeriesImpl("Final", /* isMedal */ false, finalFleets, finalRaceColumnNames, /* trackedRegattaRegistry */ null);
        series.add(finalSeries);

        if (medalRaceAndSeries) {
            // ------------ medal --------------
            List<Fleet> medalFleets = new ArrayList<Fleet>();
            medalFleets.add(new FleetImpl("Medal"));
            List<String> medalRaceColumnNames = new ArrayList<String>();
            medalRaceColumnNames.add("M");
            Series medalSeries = new SeriesImpl("Medal", /* isMedal */true, medalFleets, medalRaceColumnNames, /* trackedRegattaRegistry */ null);
            series.add(medalSeries);
        }

        Regatta regatta = new RegattaImpl(regattaBaseName, boatClass, series, /* persistent */ false);
        return regatta;
    }
}
