package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LeaderboardScoringAndRankingTest extends AbstractLeaderboardTest {
    private ArrayList<Series> series;
    
    private Leaderboard createLeaderboard(Regatta regatta, int[] discardingThresholds) {
        ScoreCorrectionImpl scoreCorrections = new ScoreCorrectionImpl();
        ResultDiscardingRuleImpl discardingRules = new ResultDiscardingRuleImpl(discardingThresholds);
        return new RegattaLeaderboardImpl(regatta, scoreCorrections, discardingRules);
    }

    @Test
    public void testOneStartedRaceWithDifferentScores() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
     * Regarding bug 912, test adding a disqualification in the middle, with a high-point scoring scheme, and check that
     * all competitors ranked worse advance by one, including getting <em>more</em> points due to the high-point scoring
     * scheme. Note that this does not test the total points given for those competitors.
     */
    @Test
    public void testOneStartedRaceWithDifferentScoresAndDisqualificationUsingHighPointScoringScheme() throws NoWindException {
        List<Competitor> competitors = createCompetitors(10);
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1,
                new String[] { "Default" },
                /* medal */false, "testOneStartedRaceWithDifferentScores",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true),
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.HIGH_POINT));
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
        assertEquals(0, leaderboard.getTotalPoints(competitors.get(5), f1Column, now), 0.000000001);
        for (int i=0; i<5; i++) {
            assertEquals(10-i, leaderboard.getTotalPoints(competitors.get(i), f1Column, now), 0.000000001);
        }
        for (int i=6; i<10; i++) {
            assertEquals(10-(i-1), leaderboard.getTotalPoints(competitors.get(i), f1Column, now), 0.000000001);
        }
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
        assertEquals(11, leaderboard.getTotalPoints(competitors.get(5), f1Column, now), 0.000000001);
        for (int i=0; i<5; i++) {
            assertEquals(i+1, leaderboard.getTotalPoints(competitors.get(i), f1Column, now), 0.000000001);
        }
        for (int i=6; i<10; i++) {
            assertEquals(i, leaderboard.getTotalPoints(competitors.get(i), f1Column, now), 0.000000001);
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
    public void testSimpleLeaderboardWithHighPointScoringScheme() throws NoWindException {
        final int NUMBER_OF_COMPETITORS = 10;
        List<Competitor> competitors = createCompetitors(NUMBER_OF_COMPETITORS);
        List<Competitor> gold = new ArrayList<Competitor>();
        List<Competitor> silver = new ArrayList<Competitor>();
        for (int i=0; i<5; i++) {
            gold.add(competitors.get(2*i));
            silver.add(competitors.get(2*i+1));
        }
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */1, new String[] {
                "Gold", "Silver" },
                /* medal */false, "testSimpleLeaderboardWithHighPointScoringScheme",
                DomainFactory.INSTANCE.getOrCreateBoatClass("ESS40", /* typicallyStartsUpwind */false),
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.HIGH_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        RaceColumn f1Column = series.get(1).getRaceColumnByName("F1");
        TrackedRace f1Gold = new MockedTrackedRaceWithStartTimeAndRanks(now, gold);
        f1Column.setTrackedRace(f1Column.getFleetByName("Gold"), f1Gold);
        TrackedRace f1Silver = new MockedTrackedRaceWithStartTimeAndRanks(now, silver);
        f1Column.setTrackedRace(f1Column.getFleetByName("Silver"), f1Silver);
        int rank=1;
        for (Competitor goldCompetitor : gold) {
            assertEquals(rank, f1Column.getTrackedRace(goldCompetitor).getRank(goldCompetitor, later));
            assertEquals(rank, leaderboard.getTrackedRank(goldCompetitor, f1Column, later));
            assertEquals(NUMBER_OF_COMPETITORS/2+1-rank, leaderboard.getNetPoints(goldCompetitor, f1Column, later), 0.00000001);
            rank++;
        }
        rank=1;
        for (Competitor silverCompetitor : silver) {
            assertEquals(rank, f1Column.getTrackedRace(silverCompetitor).getRank(silverCompetitor, later));
            assertEquals(rank, leaderboard.getTrackedRank(silverCompetitor, f1Column, later));
            assertEquals(NUMBER_OF_COMPETITORS/2+1-rank, leaderboard.getNetPoints(silverCompetitor, f1Column, later), 0.00000001);
            rank++;
        }
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        for (int i=0; i<5; i++) {
            assertSame(gold.get(i), rankedCompetitors.get(i));
            assertSame(silver.get(i), rankedCompetitors.get(i+5));
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */ true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later), 0.000000001);
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3, f4, f5, f6);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later), 0.000000001);
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true), DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2);
        createAndAttachTrackedRaces(series.get(2), "Medal", m1);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        // assert that both have equal score
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later), 0.000000001);
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
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true),
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.LOW_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3, f4, f5, f6);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later), 0.000000001);
        assertEquals(rankedCompetitors.indexOf(c[0]), rankedCompetitors.indexOf(c[1])-1);
    }

    @Test
    public void testTieBreakWithEqualWinsAndTwoVersusOneSecondsWithHighPointScoringScheme() throws NoWindException {
        Competitor[] c = createCompetitors(4).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[0] scores 14
        Competitor[] f2 = new Competitor[] { c[2], c[0], c[1], c[3] }; // c[1] scores 14 points altogether but has only one second rank
        Competitor[] f3 = new Competitor[] { c[2], c[1], c[3], c[0] }; // c[2] scores 21
        Competitor[] f4 = new Competitor[] { c[3], c[2], c[0], c[1] }; // c[3] scores 11
        Competitor[] f5 = new Competitor[] { c[0], c[2], c[1], c[3] };
        Competitor[] f6 = new Competitor[] { c[1], c[2], c[3], c[0] };
        Regatta regatta = createRegatta(/* qualifying */0, new String[] { "Default" }, /* final */6, new String[] { "Default" },
        /* medal */ false, "testTieBreakWithEqualWinsAndTwoVersusOneSeconds",
                DomainFactory.INSTANCE.getOrCreateBoatClass("49er", /* typicallyStartsUpwind */true),
                DomainFactory.INSTANCE.createScoringScheme(ScoringSchemeType.HIGH_POINT));
        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint later = createAndAttachTrackedRaces(series.get(1), "Default", f1, f2, f3, f4, f5, f6);
        List<Competitor> rankedCompetitors = leaderboard.getCompetitorsFromBestToWorst(later);
        assertEquals(leaderboard.getTotalPoints(c[0], later), leaderboard.getTotalPoints(c[1], later), 0.000000001);
        assertEquals(rankedCompetitors.indexOf(c[0]), rankedCompetitors.indexOf(c[1])-1);
    }

    @Test
    public void testOverallLeaderboardWithESSHighPointScoring() throws NoWindException {
        // Let c0 lead the series, c3 trail it, c1 and c2 are one-time competitors which are then suppressed in the
        // overall leaderboard, expecting c3 to rank second overall, after c0 ranking first, with c1 and c2 not appearing
        // in the overall leaderboard's sorted competitor list
        Competitor[] c = createCompetitors(4).toArray(new Competitor[0]);
        Competitor[] f1 = new Competitor[] { c[2], c[0], c[1], c[3] };
        Competitor[] f2 = new Competitor[] { c[2], c[0], c[1], c[3] };
        Competitor[] f3 = new Competitor[] { c[1], c[3], c[0] };
        Competitor[] f4 = new Competitor[] { c[3], c[0], c[1] };
        Competitor[] f5 = new Competitor[] { c[0], c[3] };
        Competitor[] f6 = new Competitor[] { c[3], c[0] };
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis()+1000);
        FlexibleLeaderboard leaderboard1 = new FlexibleLeaderboardImpl("Leaderboard 1", new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(/* discarding thresholds */ new int[0]), new HighPoint());
        leaderboard1.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f1)), "R1", /* medalRace */ false,
                leaderboard1.getFleet(null));
        leaderboard1.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f2)), "R2", /* medalRace */ false,
                leaderboard1.getFleet(null));
        assertTrue(leaderboard1.getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false).compare(
                leaderboard1.getTotalPoints(c[0], later), leaderboard1.getTotalPoints(c[3], later)) < 0); // c0 better than c3
        FlexibleLeaderboard leaderboard2 = new FlexibleLeaderboardImpl("Leaderboard 3", new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(/* discarding thresholds */ new int[0]), new HighPoint());
        leaderboard2.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f3)), "R1", /* medalRace */ false,
                leaderboard1.getFleet(null));
        leaderboard2.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f4)), "R2", /* medalRace */ false,
                leaderboard1.getFleet(null));
        assertTrue(leaderboard2.getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false).compare(
                leaderboard2.getTotalPoints(c[3], later), leaderboard2.getTotalPoints(c[0], later)) < 0); // c3 better than c0
        FlexibleLeaderboard leaderboard3 = new FlexibleLeaderboardImpl("Leaderboard 3", new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(/* discarding thresholds */ new int[0]), new HighPoint());
        leaderboard3.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f5)), "R1", /* medalRace */ false,
                leaderboard1.getFleet(null));
        leaderboard3.addRace(new MockedTrackedRaceWithStartTimeAndRanks(now, Arrays.asList(f6)), "R2", /* medalRace */ false,
                leaderboard1.getFleet(null));
        assertTrue(leaderboard3.getCompetitorsFromBestToWorst(later).indexOf(c[3]) <
                leaderboard3.getCompetitorsFromBestToWorst(later).indexOf(c[0])); // c3 better than c0; won last race
        LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl("Leaderboard Group", "Leaderboard Group", Arrays.asList(leaderboard1,
                leaderboard2, leaderboard3));
        leaderboardGroup.setOverallLeaderboard(new LeaderboardGroupMetaLeaderboard(leaderboardGroup, new HighPointExtremeSailingSeriesOverall(),
                new ResultDiscardingRuleImpl(new int[0])));
        leaderboardGroup.getOverallLeaderboard().setSuppressed(c[1], true);
        leaderboardGroup.getOverallLeaderboard().setSuppressed(c[2], true);
        List<Competitor> rankedCompetitors = leaderboardGroup.getOverallLeaderboard().getCompetitorsFromBestToWorst(later);
        assertFalse(rankedCompetitors.contains(c[1]));
        assertFalse(rankedCompetitors.contains(c[2]));
        assertEquals(2, rankedCompetitors.size());
        assertEquals(28 /* one win, two second */, leaderboardGroup.getOverallLeaderboard().getTotalPoints(c[0], later), 0.000000001);
        assertEquals(29 /* two wins, one second */, leaderboardGroup.getOverallLeaderboard().getTotalPoints(c[3], later), 0.000000001);
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
            String[] finalFleetNames, boolean medalRaceAndSeries, final String regattaBaseName, BoatClass boatClass, ScoringScheme scoringScheme) {
        series = new ArrayList<Series>();
        
        // -------- qualifying series ------------
        if (qualifyingFleetNames != null && qualifyingFleetNames.length > 0) {
            List<Fleet> qualifyingFleets = new ArrayList<Fleet>();
            for (String qualifyingFleetName : qualifyingFleetNames) {
                qualifyingFleets.add(new FleetImpl(qualifyingFleetName));
            }
            List<String> qualifyingRaceColumnNames = new ArrayList<String>();
            for (int i = 1; i <= numberOfQualifyingRaces; i++) {
                qualifyingRaceColumnNames.add("Q" + i);
            }
            Series qualifyingSeries = new SeriesImpl("Qualifying", /* isMedal */false, qualifyingFleets,
                    qualifyingRaceColumnNames, /* trackedRegattaRegistry */null);
            series.add(qualifyingSeries);
        }
        
        // -------- final series ------------
        if (finalFleetNames != null && finalFleetNames.length > 0) {
            List<Fleet> finalFleets = new ArrayList<Fleet>();
            int fleetOrdering = 1;
            for (String finalFleetName : finalFleetNames) {
                finalFleets.add(new FleetImpl(finalFleetName, fleetOrdering++));
            }
            List<String> finalRaceColumnNames = new ArrayList<String>();
            for (int i = 1; i <= numberOfFinalRaces; i++) {
                finalRaceColumnNames.add("F" + i);
            }
            Series finalSeries = new SeriesImpl("Final", /* isMedal */false, finalFleets, finalRaceColumnNames, /* trackedRegattaRegistry */ null);
            series.add(finalSeries);
        }

        if (medalRaceAndSeries) {
            // ------------ medal --------------
            List<Fleet> medalFleets = new ArrayList<Fleet>();
            medalFleets.add(new FleetImpl("Medal"));
            List<String> medalRaceColumnNames = new ArrayList<String>();
            medalRaceColumnNames.add("M");
            Series medalSeries = new SeriesImpl("Medal", /* isMedal */true, medalFleets, medalRaceColumnNames, /* trackedRegattaRegistry */ null);
            series.add(medalSeries);
        }

        Regatta regatta = new RegattaImpl(regattaBaseName, boatClass, series, /* persistent */ false, scoringScheme);
        return regatta;
    }
}
