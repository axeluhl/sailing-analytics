package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.DelegatingRegattaLeaderboardWithCompetitorElimination;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import junit.framework.Assert;

public class LeaderboardScoringAndRankingTestForLowPoints extends LeaderboardScoringAndRankingTestBase {
    private static final double EPSILON = 0.000001;

    protected DelegatingRegattaLeaderboardWithCompetitorElimination createDelegatingRegattaLeaderboardWithCompetitorElimination(
            Regatta regatta, String leaderboardName, int[] discardingThresholds) {
        return new DelegatingRegattaLeaderboardWithCompetitorElimination(
                () -> createLeaderboard(regatta, discardingThresholds), leaderboardName);
    }

    private void executeRandomPreSeries(List<Competitor> yellow, List<Competitor> blue, TimePoint now) {
        RaceColumn qColumn = series.get(0).getRaceColumnByName("Q");
        TrackedRace qYellow = new MockedTrackedRaceWithStartTimeAndRanks(now, yellow);
        qColumn.setTrackedRace(qColumn.getFleetByName("Yellow"), qYellow);
        TrackedRace qBlue = new MockedTrackedRaceWithStartTimeAndRanks(now, blue);
        qColumn.setTrackedRace(qColumn.getFleetByName("Blue"), qBlue);
    }

    private void manuallyTransferCarry(Leaderboard leaderboard, List<Competitor> medalCompetitors) {
        double carryScore = 0;
        for (Competitor medalCompetitor : medalCompetitors) {
            leaderboard.getScoreCorrection().correctScore(medalCompetitor, leaderboard.getRaceColumnByName("Carry"),
                    carryScore);
            carryScore += 0.0001;
        }
    }

    private Regatta setupRegatta(boolean useFirstTwoWins) {
        final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("470",
                /* typicallyStartsUpwind */ true);
        Regatta regatta = new RegattaImpl(RegattaImpl.getDefaultName("Test Regatta", boatClass.getName()), boatClass,
                /* canBoatsOfCompetitorsChangePerRace */ true, /* startDate */ null, /* endDate */ null, series,
                /* persistent */false,
                DomainFactory.INSTANCE
                        .createScoringScheme(useFirstTwoWins ? ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_TWO_RACES
                                : ScoringSchemeType.LOW_POINT),
                "123", /* course area */null, OneDesignRankingMetric::new);
        return regatta;
    }

    private void setupMedalSeries() {
        // -------- medal series ------------
        {
            Set<? extends Fleet> medalFleets = Collections.singleton(new FleetImpl("Default"));
            List<String> medalRaceColumnNames = new ArrayList<String>();
            medalRaceColumnNames.add("Carry");
            medalRaceColumnNames.add("M1");
            medalRaceColumnNames.add("M2");
            medalRaceColumnNames.add("M3");
            medalRaceColumnNames.add("M4");
            Series medalSeries = new SeriesImpl("Medal", /* isMedal */true, /* isFleetsCanRunInParallel */ true,
                    medalFleets, medalRaceColumnNames, /* trackedRegattaRegistry */ null);
            series.add(medalSeries);
        }
    }

    private void setupPreSeries() {
        {
            List<Fleet> qualificationFleets = new ArrayList<Fleet>();
            for (String qualificationFleetName : new String[] { "Yellow", "Blue" }) {
                qualificationFleets.add(new FleetImpl(qualificationFleetName));
            }
            List<String> qualificationRaceColumnNames = new ArrayList<String>();
            qualificationRaceColumnNames.add("Q");
            Series qualificationSeries = new SeriesImpl("Qualification", /* isMedal */false,
                    /* isFleetsCanRunInParallel */ true, qualificationFleets, qualificationRaceColumnNames,
                    /* trackedRegattaRegistry */ null);
            // discard the one and only qualification race; it doesn't score
            qualificationSeries.setResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(new int[] { 1 }));
            series.add(qualificationSeries);
        }
    }

    @Test
    public void testFirstPreseriesWinsAgain() throws NoWindException {
        series = new ArrayList<Series>();
        setupPreSeries();

        setupMedalSeries();
        Regatta regatta = setupRegatta(true);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> bestQualifing = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitors = bestQualifing.subList(0, 4);

        manuallyTransferCarry(leaderboard, medalCompetitors);

        Competitor preFirst = medalCompetitors.get(0);
        Competitor preSecond = medalCompetitors.get(1);
        Competitor preThird = medalCompetitors.get(2);
        Competitor preFourth = medalCompetitors.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preFirst);
        m1Results.add(preSecond);
        m1Results.add(preThird);
        m1Results.add(preFourth);

        TrackedRace mDefault = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), mDefault);

        Assert.assertEquals(2.0, leaderboard.getNetPoints(medalCompetitors.get(0), later), EPSILON);
        Assert.assertEquals(2.0001, leaderboard.getNetPoints(medalCompetitors.get(1), later), EPSILON);
        Assert.assertEquals(3.0002, leaderboard.getNetPoints(medalCompetitors.get(2), later), EPSILON);
        Assert.assertEquals(4.0003, leaderboard.getNetPoints(medalCompetitors.get(3), later), EPSILON);
    }

    @Test
    public void testSecondPreSeriesWinsTwice() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(true);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(1.0001, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(3.0002, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(4.0003, leaderboard.getNetPoints(preFourth, later), EPSILON);

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

        Assert.assertEquals(2.0, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(6.0002, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(8.0003, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
    }

    @Test
    public void testFirstWinsSecondRace() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(true);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(1.0001, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(3.0002, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(4.0003, leaderboard.getNetPoints(preFourth, later), EPSILON);

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

        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(3.0001, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(6.0002, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(8.0003, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
    }

    @Test
    public void testLastWinsTwoRaces() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(true);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(1.0003, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(3.0001, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(4.0002, leaderboard.getNetPoints(preThird, later), EPSILON);

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

        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(6.0001, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(8.0002, leaderboard.getNetPoints(preThird, m2later), EPSILON);
    }

    @Test
    public void noTwoWinsAbortAfterTwoRacesAndTieBraker() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(true);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(1.0003, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(2.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(3.0001, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(4.0002, leaderboard.getNetPoints(preThird, later), EPSILON);

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

        Assert.assertEquals(5.0003, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(6.0001, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(5.0002, leaderboard.getNetPoints(preThird, m2later), EPSILON);
    }
    
    
    @Test
    public void negativeTestFirstPreseriesWinsAgain() throws NoWindException {
        series = new ArrayList<Series>();
        setupPreSeries();

        setupMedalSeries();
        Regatta regatta = setupRegatta(false);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> bestQualifing = leaderboard.getCompetitorsFromBestToWorst(later);
        List<Competitor> medalCompetitors = bestQualifing.subList(0, 4);

        manuallyTransferCarry(leaderboard, medalCompetitors);

        Competitor preFirst = medalCompetitors.get(0);
        Competitor preSecond = medalCompetitors.get(1);
        Competitor preThird = medalCompetitors.get(2);
        Competitor preFourth = medalCompetitors.get(3);

        RaceColumn m1 = leaderboard.getRaceColumnByName("M1");

        ArrayList<Competitor> m1Results = new ArrayList<>();
        m1Results.add(preFirst);
        m1Results.add(preSecond);
        m1Results.add(preThird);
        m1Results.add(preFourth);

        TrackedRace mDefault = new MockedTrackedRaceWithStartTimeAndRanks(now, m1Results);
        m1.setTrackedRace(m1.getFleetByName("Default"), mDefault);

        Assert.assertEquals(2.0, leaderboard.getNetPoints(medalCompetitors.get(0), later), EPSILON);
        Assert.assertEquals(4.0002, leaderboard.getNetPoints(medalCompetitors.get(1), later), EPSILON);
        Assert.assertEquals(6.0004, leaderboard.getNetPoints(medalCompetitors.get(2), later), EPSILON);
        Assert.assertEquals(8.0006, leaderboard.getNetPoints(medalCompetitors.get(3), later), EPSILON);
    }

    @Test
    public void negativeTestSecondPreSeriesWinsTwice() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(false);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(2.0002, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6.0004, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(8.0006, leaderboard.getNetPoints(preFourth, later), EPSILON);

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

        Assert.assertEquals(4.0002, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(8.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(12.0004, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(16.0006, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
    }

    @Test
    public void negativeTestFirstWinsSecondRace() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(false);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(2.0002, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6.0004, leaderboard.getNetPoints(preThird, later), EPSILON);
        Assert.assertEquals(8.0006, leaderboard.getNetPoints(preFourth, later), EPSILON);

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

        Assert.assertEquals(6.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(6.0002, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(12.0004, leaderboard.getNetPoints(preThird, m2later), EPSILON);
        Assert.assertEquals(16.0006, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
    }

    @Test
    public void negativeTestLastWinsTwoRaces() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(false);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(2.0006, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6.0002, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(8.0004, leaderboard.getNetPoints(preThird, later), EPSILON);

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

        Assert.assertEquals(4.0006, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(8.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(12.0002, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(16.0004, leaderboard.getNetPoints(preThird, m2later), EPSILON);
    }

    @Test
    public void negativeNoTwoWinsAbortAfterTwoRacesAndTieBraker() throws NoWindException {
        series = new ArrayList<Series>();
        // -------- qualification series ------------
        setupPreSeries();
        setupMedalSeries();
        Regatta regatta = setupRegatta(false);
        List<Competitor> competitors = createCompetitors(12);
        List<Competitor> yellow = new ArrayList<>(competitors.subList(0, 6));
        List<Competitor> blue = new ArrayList<>(competitors);
        blue.removeAll(yellow);
        Collections.shuffle(yellow);
        Collections.shuffle(blue);

        Leaderboard leaderboard = createLeaderboard(regatta, /* discarding thresholds */ new int[0]);
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint later = new MillisecondsTimePoint(now.asMillis() + 1000);
        executeRandomPreSeries(yellow, blue, now);

        List<Competitor> medalCompetitorsBestToWorst = leaderboard.getCompetitorsFromBestToWorst(later).subList(0, 4);
        manuallyTransferCarry(leaderboard, medalCompetitorsBestToWorst);

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

        Assert.assertEquals(2.0006, leaderboard.getNetPoints(preFourth, later), EPSILON);
        Assert.assertEquals(4.0, leaderboard.getNetPoints(preFirst, later), EPSILON);
        Assert.assertEquals(6.0002, leaderboard.getNetPoints(preSecond, later), EPSILON);
        Assert.assertEquals(8.0004, leaderboard.getNetPoints(preThird, later), EPSILON);

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

        Assert.assertEquals(10.0006, leaderboard.getNetPoints(preFourth, m2later), EPSILON);
        Assert.assertEquals(8.0, leaderboard.getNetPoints(preFirst, m2later), EPSILON);
        Assert.assertEquals(12.0002, leaderboard.getNetPoints(preSecond, m2later), EPSILON);
        Assert.assertEquals(10.0004, leaderboard.getNetPoints(preThird, m2later), EPSILON);
    }
    
}
