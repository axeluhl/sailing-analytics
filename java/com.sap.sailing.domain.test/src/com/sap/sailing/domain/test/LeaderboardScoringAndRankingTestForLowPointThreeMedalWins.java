package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPointFirstToWinThreeRaces;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * This class contains several tests for the {@link LowPointFirstToWinThreeRaces} scoring rule defined by
 * {@link ScoringSchemeType#LOW_POINT_FIRST_TO_WIN_THREE_RACES}. It tests that carried wins are applied properly, that
 * final participants always rank better than semi-finalists who did not advance to the final, and that ties in the
 * medal stage are broken first by the last medal race score, then by the opening series rank. Furthermore it contains
 * several negative tests, that validate, that the normal low point behavior is not changed and still works for those
 * same cases in case the {@link ScoringSchemeType#LOW_POINT} scoring scheme is used.
 */
public class LeaderboardScoringAndRankingTestForLowPointThreeMedalWins extends LeaderboardScoringAndRankingTestBase {
    private static final String FINAL_CARRY_COLUMN_NAME = "Carry F";
    private static final String SEMIFINAL_CARRY_COLUMN_NAME = "Carry SF";
    private static final double EPSILON = 0.000001;

    private void executePreSeries(List<Competitor> yellow, List<Competitor> blue, TimePoint now) {
        RaceColumn qColumn = series.get(0).getRaceColumnByName("R1");
        TrackedRace qYellow = new MockedTrackedRaceWithStartTimeAndRanks(now, yellow);
        qColumn.setTrackedRace(qColumn.getFleetByName("Yellow"), qYellow);
        TrackedRace qBlue = new MockedTrackedRaceWithStartTimeAndRanks(now, blue);
        qColumn.setTrackedRace(qColumn.getFleetByName("Blue"), qBlue);
    }

    private void manuallyTransferCarry(Leaderboard leaderboard, List<Competitor> medalCompetitors, String carryColumnName) {
        int carryScore = 1;
        for (Competitor medalCompetitor : medalCompetitors) {
            leaderboard.getScoreCorrection().correctScore(medalCompetitor, leaderboard.getRaceColumnByName(carryColumnName), carryScore++);
        }
    }

    private Regatta setupRegatta() {
        final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("Kite", /* typicallyStartsUpwind */ true);
        Regatta regatta = new RegattaImpl(RegattaImpl.getDefaultName("Test Regatta", boatClass.getName()), boatClass,
                false, CompetitorRegistrationType.CLOSED, /* startDate */ null, /* endDate */ null, series, /* persistent */false,
                DomainFactory.INSTANCE
                        .createScoringScheme(ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_THREE_RACES),
                "123", /* course area */null, OneDesignRankingMetric::new,
                /* registrationLinkSecret */ UUID.randomUUID().toString());
        return regatta;
    }

    private void setupMedalSeriesWithCarryOverAndFourRaceColumns() {
        Iterable<? extends Fleet> semiFinalFleets = Arrays.asList(new Fleet[] { new FleetImpl("Yellow", 0), new FleetImpl("Blue", 0)});
        List<String> semiFinalRaceColumnNames = new ArrayList<String>();
        semiFinalRaceColumnNames.add(SEMIFINAL_CARRY_COLUMN_NAME);
        semiFinalRaceColumnNames.add("SF1");
        semiFinalRaceColumnNames.add("SF2");
        semiFinalRaceColumnNames.add("SF3");
        semiFinalRaceColumnNames.add("SF4");
        semiFinalRaceColumnNames.add("SF5");
        semiFinalRaceColumnNames.add("SF6");
        Series semiFinalSeries = new SeriesImpl("Semifinal", /* isMedal */ true, /* isFleetsCanRunInParallel */ true,
                semiFinalFleets, semiFinalRaceColumnNames, /* trackedRegattaRegistry */ null);
        semiFinalSeries.setFirstColumnIsNonDiscardableCarryForward(true);
        semiFinalSeries.setStartsWithZeroScore(true);
        series.add(semiFinalSeries);
        Set<? extends Fleet> grandFinalFleets = Collections.singleton(new FleetImpl("Default"));
        List<String> grandFinalRaceColumnNames = new ArrayList<String>();
        grandFinalRaceColumnNames.add(FINAL_CARRY_COLUMN_NAME);
        grandFinalRaceColumnNames.add("F1");
        grandFinalRaceColumnNames.add("F2");
        grandFinalRaceColumnNames.add("F3");
        grandFinalRaceColumnNames.add("F4");
        grandFinalRaceColumnNames.add("F5");
        grandFinalRaceColumnNames.add("F6");
        Series grandFinalSeries = new SeriesImpl("Grand Final", /* isMedal */ true, /* isFleetsCanRunInParallel */ true,
                grandFinalFleets, grandFinalRaceColumnNames, /* trackedRegattaRegistry */ null);
        grandFinalSeries.setFirstColumnIsNonDiscardableCarryForward(true);
        grandFinalSeries.setStartsWithZeroScore(true);
        series.add(grandFinalSeries);
    }

    private void setupOpeningSeriesWithOneRaceColumnPerSeries() {
        List<Fleet> qualificationFleets = new ArrayList<>();
        for (String qualificationFleetName : new String[] { "Yellow", "Blue" }) {
            qualificationFleets.add(new FleetImpl(qualificationFleetName));
        }
        List<String> qualificationRaceColumnNames = new ArrayList<String>();
        qualificationRaceColumnNames.add("R1");
        Series qualificationSeries = new SeriesImpl("Qualification", /* isMedal */ false,
                /* isFleetsCanRunInParallel */ true, qualificationFleets, qualificationRaceColumnNames,
                /* trackedRegattaRegistry */ null);
        series.add(qualificationSeries);
        List<Fleet> finalFleets = new ArrayList<>();
        int fleetRank = 0;
        for (String qualificationFleetName : new String[] { "Gold", "Silver" }) {
            finalFleets.add(new FleetImpl(qualificationFleetName, fleetRank++));
        }
        List<String> finalRaceColumnNames = new ArrayList<String>();
        finalRaceColumnNames.add("R2");
        Series finalSeries = new SeriesImpl("Final", /* isMedal */ false,
                /* isFleetsCanRunInParallel */ true, finalFleets, finalRaceColumnNames,
                /* trackedRegattaRegistry */ null);
        series.add(finalSeries);
    }

    private List<Pair<Competitor, Double>> createCompetitorResultForTimestamp(TimePoint time, Leaderboard leaderboard) {
        List<Pair<Competitor, Double>> list = new ArrayList<>();
        for (Competitor competitor : leaderboard.getCompetitorsFromBestToWorst(time)) {
            list.add(new Pair<Competitor, Double>(competitor, leaderboard.getNetPoints(competitor, time)));
        }
        return list;
    }

    private void assertNonFinalistsAreBehindFinalistsAndNotChanged(
            List<Pair<Competitor, Double>> preSeriesScoreRankResult, List<Pair<Competitor, Double>> afterFinalResults) {
        List<Pair<Competitor, Double>> nonFinalistsPreScore = preSeriesScoreRankResult.subList(4,
                preSeriesScoreRankResult.size());
        List<Pair<Competitor, Double>> nonFinalistsAfterScore = afterFinalResults.subList(4, afterFinalResults.size());
        Assert.assertEquals(nonFinalistsPreScore, nonFinalistsAfterScore);
    }

    /**
     * In this test the opening series winner will win the first race, and should get a score of 2, all other finalists
     * should be scored with Low_Points restarting at 0 for the medal series. The non finalists score should not change
     * during the medalseries.
     */
    @Test
    public void testFirstPreseriesWinsAgain() throws NoWindException {
        series = new ArrayList<Series>();
        setupOpeningSeriesWithOneRaceColumnPerSeries();

        setupMedalSeriesWithCarryOverAndFourRaceColumns();
        Regatta regatta = setupRegatta();
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executePreSeries(yellow, blue, now);

        List<Competitor> preSeriesRankResult = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitorsBestToWorst = preSeriesRankResult.subList(0, 4);
        List<Pair<Competitor, Double>> preSeriesScoreRankResult = createCompetitorResultForTimestamp(later, leaderboard);

        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst, FINAL_CARRY_COLUMN_NAME);

        Competitor preFirst = medalCompetitorsBestToWorst.get(0);
        Competitor preSecond = medalCompetitorsBestToWorst.get(1);
        Competitor preThird = medalCompetitorsBestToWorst.get(2);
        Competitor preFourth = medalCompetitorsBestToWorst.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preFirst);
        m1Results.add(preSecond);
        m1Results.add(preThird);
        m1Results.add(preFourth);

        TrackedRace mDefault = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), mDefault);

        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(4, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(6, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preFourth, later), EPSILON);

        List<Pair<Competitor, Double>> afterFinalResults = createCompetitorResultForTimestamp(later, leaderboard);
        assertNonFinalistsAreBehindFinalistsAndNotChanged(preSeriesScoreRankResult, afterFinalResults);
    }

    /**
     * In this test the second best in the preseries wins the first three medal races, and should get a score of 2, all
     * other finalists should be scored with Low_Points restarting at 0 for the medal series. The non finalists score
     * should not change during the medalseries.
     */
    @Test
    public void testSecondPreSeriesWinsTwice() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupOpeningSeriesWithOneRaceColumnPerSeries();
        setupMedalSeriesWithCarryOverAndFourRaceColumns();
        Regatta regatta = setupRegatta();
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executePreSeries(yellow, blue, now);

        List<Competitor> preSeriesRankResult = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitorsBestToWorst = preSeriesRankResult.subList(0, 4);
        List<Pair<Competitor, Double>> preSeriesScoreRankResult = createCompetitorResultForTimestamp(later,
                leaderboard);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst, FINAL_CARRY_COLUMN_NAME);

        Competitor preFirst = medalCompetitorsBestToWorst.get(0);
        Competitor preSecond = medalCompetitorsBestToWorst.get(1);
        Competitor preThird = medalCompetitorsBestToWorst.get(2);
        Competitor preFourth = medalCompetitorsBestToWorst.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preSecond);
        m1Results.add(preFirst);
        m1Results.add(preThird);
        m1Results.add(preFourth);

        TimePoint m1now = new MillisecondsTimePoint(later.asMillis() + 1000);
        TimePoint m1later = new MillisecondsTimePoint(m1now.asMillis() + 1000);

        TrackedRace m1Default = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), m1Default);

        Assert.assertEquals(3, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(3, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preFourth, later), EPSILON);
        
        List<Competitor> res = leaderboard.getCompetitorsFromBestToWorst(later);
        Assert.assertEquals(res.get(0), preFirst);
        Assert.assertEquals(res.get(1), preSecond);
        
        RaceColumn m2 = leaderboard.getRaceColumnByName("M2");

        ArrayList<Competitor> m2Results = new ArrayList<>();
        m2Results.add(preSecond);
        m2Results.add(preFirst);
        m2Results.add(preThird);
        m2Results.add(preFourth);

        TimePoint m2now = new MillisecondsTimePoint(m1later.asMillis() + 1000);
        TimePoint m2later = new MillisecondsTimePoint(m2now.asMillis() + 1000);
        TrackedRace m2Default = new MockedTrackedRaceWithStartTimeAndRanks(m2now, m2Results);
        m2.setTrackedRace(m1.getFleetByName("Default"), m2Default);

        Assert.assertEquals(4.0, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(5.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(9, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(12, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        
        List<Competitor> res2 = leaderboard.getCompetitorsFromBestToWorst(m2later);
        Assert.assertEquals(res2.get(0), preSecond);
        Assert.assertEquals(res2.get(1), preFirst);
        Assert.assertEquals(res2.get(2), preThird);
        Assert.assertEquals(res2.get(3), preFourth);

        List<Pair<Competitor, Double>> afterFinalResults = createCompetitorResultForTimestamp(m2later, leaderboard);
        assertNonFinalistsAreBehindFinalistsAndNotChanged(preSeriesScoreRankResult, afterFinalResults);
    }

    /**
     * In this test the best in the preseries wins the second medal race, and should get a score of 2, all other
     * finalists should be scored with Low_Points restarting at 0 for the medal series. The non finalists score should
     * not change during the medalseries. Also asserts that during tie-breaking the pre-series carry score takes
     * precedence.
     */
    @Test
    public void testFirstWinsSecondRace() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupOpeningSeriesWithOneRaceColumnPerSeries();
        setupMedalSeriesWithCarryOverAndFourRaceColumns();
        Regatta regatta = setupRegatta();
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executePreSeries(yellow, blue, now);

        List<Competitor> preSeriesRankResult = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitorsBestToWorst = preSeriesRankResult.subList(0, 4);
        List<Pair<Competitor, Double>> preSeriesScoreRankResult = createCompetitorResultForTimestamp(later,
                leaderboard);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst, FINAL_CARRY_COLUMN_NAME);

        Competitor preFirst = medalCompetitorsBestToWorst.get(0);
        Competitor preSecond = medalCompetitorsBestToWorst.get(1);
        Competitor preThird = medalCompetitorsBestToWorst.get(2);
        Competitor preFourth = medalCompetitorsBestToWorst.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preSecond);
        m1Results.add(preFirst);
        m1Results.add(preThird);
        m1Results.add(preFourth);

        TimePoint m1now = new MillisecondsTimePoint(later.asMillis() + 1000);
        TimePoint m1later = new MillisecondsTimePoint(m1now.asMillis() + 1000);

        TrackedRace m1Default = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), m1Default);

        Assert.assertEquals(3, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(3, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preFourth, later), EPSILON);
        // assert that during tie-breaking the pre-series carry score takes precedence
        List<Competitor> res = leaderboard.getCompetitorsFromBestToWorst(later);
        Assert.assertEquals(res.get(0), preFirst);
        Assert.assertEquals(res.get(1), preSecond);

        RaceColumn m2 = leaderboard.getRaceColumnByName("M2");

        ArrayList<Competitor> m2Results = new ArrayList<>();
        m2Results.add(preFirst);
        m2Results.add(preSecond);
        m2Results.add(preThird);
        m2Results.add(preFourth);

        TimePoint m2now = new MillisecondsTimePoint(m1later.asMillis() + 1000);
        TimePoint m2later = new MillisecondsTimePoint(m2now.asMillis() + 1000);
        TrackedRace m2Default = new MockedTrackedRaceWithStartTimeAndRanks(m2now, m2Results);
        m2.setTrackedRace(m1.getFleetByName("Default"), m2Default);

        Assert.assertEquals(4, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(5, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(9, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(12, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        
        List<Competitor> res2 = leaderboard.getCompetitorsFromBestToWorst(m2later);
        Assert.assertEquals(res2.get(0), preFirst);
        Assert.assertEquals(res2.get(1), preSecond);
        Assert.assertEquals(res2.get(2), preThird);
        Assert.assertEquals(res2.get(3), preFourth);

        List<Pair<Competitor, Double>> afterFinalResults = createCompetitorResultForTimestamp(m2later, leaderboard);
        assertNonFinalistsAreBehindFinalistsAndNotChanged(preSeriesScoreRankResult, afterFinalResults);
    }

    /**
     * In this test the worst finalist of the qualification wins the first and second medal races, and should get a
     * score of 2, all other finalists should be scored with Low_Points restarting at 0 for the medal series. The non
     * finalists score should not change during the medalseries
     */
    @Test
    public void testLastWinsThreeRaces() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupOpeningSeriesWithOneRaceColumnPerSeries();
        setupMedalSeriesWithCarryOverAndFourRaceColumns();
        Regatta regatta = setupRegatta();
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executePreSeries(yellow, blue, now);

        List<Competitor> preSeriesRankResult = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitorsBestToWorst = preSeriesRankResult.subList(0, 4);
        List<Pair<Competitor, Double>> preSeriesScoreRankResult = createCompetitorResultForTimestamp(later,
                leaderboard);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst, FINAL_CARRY_COLUMN_NAME);

        Competitor preFirst = medalCompetitorsBestToWorst.get(0);
        Competitor preSecond = medalCompetitorsBestToWorst.get(1);
        Competitor preThird = medalCompetitorsBestToWorst.get(2);
        Competitor preFourth = medalCompetitorsBestToWorst.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preFourth);
        m1Results.add(preFirst);
        m1Results.add(preSecond);
        m1Results.add(preThird);

        TimePoint m1now = new MillisecondsTimePoint(later.asMillis() + 1000);
        TimePoint m1later = new MillisecondsTimePoint(m1now.asMillis() + 1000);

        TrackedRace m1Default = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), m1Default);

        Assert.assertEquals(5, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(3.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(5, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(7, leaderboard.getNetPoints(preThird, later), EPSILON);
        
        List<Competitor> res = leaderboard.getCompetitorsFromBestToWorst(later);
        Assert.assertEquals(res.get(0), preFirst);
        Assert.assertEquals(res.get(1), preSecond);
        Assert.assertEquals(res.get(2), preFourth);

        RaceColumn m2 = leaderboard.getRaceColumnByName("M2");

        ArrayList<Competitor> m2Results = new ArrayList<>();
        m2Results.add(preFourth);
        m2Results.add(preFirst);
        m2Results.add(preSecond);
        m2Results.add(preThird);

        TimePoint m2now = new MillisecondsTimePoint(m1later.asMillis() + 1000);
        TimePoint m2later = new MillisecondsTimePoint(m2now.asMillis() + 1000);
        TrackedRace m2Default = new MockedTrackedRaceWithStartTimeAndRanks(m2now, m2Results);
        m2.setTrackedRace(m1.getFleetByName("Default"), m2Default);

        Assert.assertEquals(6.0, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(5.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(11, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        
        List<Competitor> res2 = leaderboard.getCompetitorsFromBestToWorst(m2later);
        Assert.assertEquals(res2.get(0), preFourth);
        Assert.assertEquals(res2.get(1), preFirst);
        Assert.assertEquals(res2.get(2), preSecond);
        Assert.assertEquals(res2.get(3), preThird);

        List<Pair<Competitor, Double>> afterFinalResults = createCompetitorResultForTimestamp(m2later, leaderboard);
        assertNonFinalistsAreBehindFinalistsAndNotChanged(preSeriesScoreRankResult, afterFinalResults);
    }

    /**
     * In this test no finalist reaches three wins before terminating (eg. due to bad weather) all finalists should be
     * scored with Low_Points restarting at 0 for the medal series. The non finalists score should not change during the
     * medalseries.
     */
    @Test
    public void noThreeWinsAbortAfterThreeRacesAndTieBreaker() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupOpeningSeriesWithOneRaceColumnPerSeries();
        setupMedalSeriesWithCarryOverAndFourRaceColumns();
        Regatta regatta = setupRegatta();
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executePreSeries(yellow, blue, now);

        List<Competitor> preSeriesRankResult = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitorsBestToWorst = preSeriesRankResult.subList(0, 4);
        List<Pair<Competitor, Double>> preSeriesScoreRankResult = createCompetitorResultForTimestamp(later,
                leaderboard);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst, FINAL_CARRY_COLUMN_NAME);

        Competitor preFirst = medalCompetitorsBestToWorst.get(0);
        Competitor preSecond = medalCompetitorsBestToWorst.get(1);
        Competitor preThird = medalCompetitorsBestToWorst.get(2);
        Competitor preFourth = medalCompetitorsBestToWorst.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preFourth);
        m1Results.add(preFirst);
        m1Results.add(preSecond);
        m1Results.add(preThird);

        TimePoint m1now = new MillisecondsTimePoint(later.asMillis() + 1000);
        TimePoint m1later = new MillisecondsTimePoint(m1now.asMillis() + 1000);

        TrackedRace m1Default = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), m1Default);

        Assert.assertEquals(5, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(3.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(5, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(7, leaderboard.getNetPoints(preThird, later), EPSILON);
        
        
        List<Competitor> res = leaderboard.getCompetitorsFromBestToWorst(later);
        Assert.assertEquals(res.get(0), preFirst);
        Assert.assertEquals(res.get(1), preSecond);
        Assert.assertEquals(res.get(2), preFourth);

        RaceColumn m2 = leaderboard.getRaceColumnByName("M2");

        ArrayList<Competitor> m2Results = new ArrayList<>();
        m2Results.add(preThird);
        m2Results.add(preFirst);
        m2Results.add(preSecond);
        m2Results.add(preFourth);

        TimePoint m2now = new MillisecondsTimePoint(m1later.asMillis() + 1000);
        TimePoint m2later = new MillisecondsTimePoint(m2now.asMillis() + 1000);
        TrackedRace m2Default = new MockedTrackedRaceWithStartTimeAndRanks(m2now, m2Results);
        m2.setTrackedRace(m1.getFleetByName("Default"), m2Default);

        Assert.assertEquals(9, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(5.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(8, leaderboard.getNetPoints(preThird, m2later), EPSILON);

        List<Pair<Competitor, Double>> afterFinalResults = createCompetitorResultForTimestamp(m2later, leaderboard);
        assertNonFinalistsAreBehindFinalistsAndNotChanged(preSeriesScoreRankResult, afterFinalResults);
    }
}
