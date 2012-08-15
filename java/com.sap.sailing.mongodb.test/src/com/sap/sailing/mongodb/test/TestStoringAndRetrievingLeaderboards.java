package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.HigherScoreIsBetter;
import com.sap.sailing.domain.leaderboard.impl.LowerScoreIsBetter;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.test.MockedTrackedRaceWithFixedRank;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TestStoringAndRetrievingLeaderboards extends AbstractMongoDBTest {
    public TestStoringAndRetrievingLeaderboards() throws UnknownHostException, MongoException {
        super();
    }

    @Test
    public void testStoreAndRetrieveLeaderboardWithCommentedScoreCorrection() {
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        SettableScoreCorrection scoreCorrection = new ScoreCorrectionImpl();
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, scoreCorrection,
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        scoreCorrection.setComment("Humba");
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        scoreCorrection.setTimePointOfLastCorrectionsValidity(now);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        assertEquals("Humba", loadedLeaderboard.getScoreCorrection().getComment());
        assertEquals(now, loadedLeaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity());
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboard() {
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(Arrays.equals(discardIndexResultsStartingWithHowManyRaces, loadedLeaderboard.getResultDiscardingRule()
                .getDiscardIndexResultsStartingWithHowManyRaces()));
    }
    
    @Test
    public void testStoreAndRetrieveSimpleLeaderboardWithHighPointScoringScheme() {
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new HigherScoreIsBetter());
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        assertSame(HigherScoreIsBetter.class, loadedLeaderboard.getScoringScheme().getClass());
        assertEquals(ScoringSchemeType.HIGH_POINT, loadedLeaderboard.getScoringScheme().getType());
    }
    
    @Test
    public void testStoreAndRetrieveLeaderboardWithCarryColumn() {
        final String leaderboardName = "TestLeaderboard";
        final String raceColumnName = "My First Race";
        final double carriedPointsForWolfgangHunger = 3.7;
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        Competitor competitor = createCompetitor();
        TrackedRace raceWithOneCompetitor = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 1, /* started */ true);
        leaderboard.addRace(raceWithOneCompetitor, raceColumnName, /* medalRace */ false, leaderboard.getFleet(null));
        leaderboard.setCarriedPoints(competitor, carriedPointsForWolfgangHunger);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        FlexibleLeaderboard loadedLeaderboard = (FlexibleLeaderboard) new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        // attach tracked race to leaderboard to ensure that competitor object is assigned properly
        loadedLeaderboard.addRace(raceWithOneCompetitor, raceColumnName, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(Arrays.equals(discardIndexResultsStartingWithHowManyRaces, loadedLeaderboard.getResultDiscardingRule()
                .getDiscardIndexResultsStartingWithHowManyRaces()));
        assertEquals(1, Util.size(loadedLeaderboard.getCompetitors()));
        assertEquals(competitor, loadedLeaderboard.getCompetitors().iterator().next());
        assertEquals(carriedPointsForWolfgangHunger, loadedLeaderboard.getCarriedPoints(competitor), 0.000000001);
    }

    @Test
    public void testStoreAndRetrieveLeaderboardWithScoreCorrections() {
        final String leaderboardName = "TestLeaderboard";
        final String raceColumnName1 = "My First Race 1";
        final String raceColumnName2 = "My First Race 2";
        final double correctedPoints = 2.75;
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        Competitor competitor = createCompetitor();
        TrackedRace raceWithOneCompetitor1 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 1, /* started */ true);
        TrackedRace raceWithOneCompetitor2 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 2, /* started */ true);
        leaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace */ false, leaderboard.getFleet(null));
        leaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace */ true, leaderboard.getFleet(null));
        leaderboard.getScoreCorrection().correctScore(competitor, leaderboard.getRaceColumnByName(raceColumnName1), correctedPoints);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        FlexibleLeaderboard loadedLeaderboard = (FlexibleLeaderboard) new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        // attach tracked race to leaderboard to ensure that competitor object is assigned properly
        RaceColumn loadedColumn1 = loadedLeaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        RaceColumn loadedColumn2 = loadedLeaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn1));
        assertEquals(correctedPoints, (double) loadedLeaderboard.getScoreCorrection().getExplicitScoreCorrection(competitor, loadedColumn1), 0.00000001);
        assertFalse(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn2));
    }

    @Test
    public void testStoreAndRetrieveLeaderboardWithMaxPointsReason() {
        final String leaderboardName = "TestLeaderboard";
        final String raceColumnName1 = "My First Race 1";
        final String raceColumnName2 = "My First Race 2";
        final MaxPointsReason maxPointsReason = MaxPointsReason.DNF;
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        Competitor competitor = createCompetitor();
        TrackedRace raceWithOneCompetitor1 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 1, /* started */ true);
        TrackedRace raceWithOneCompetitor2 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 2, /* started */ true);
        leaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace */ false, leaderboard.getFleet(null));
        leaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace */ true, leaderboard.getFleet(null));
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, leaderboard.getRaceColumnByName(raceColumnName2), maxPointsReason);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        FlexibleLeaderboard loadedLeaderboard = (FlexibleLeaderboard) new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        // attach tracked race to leaderboard to ensure that competitor object is assigned properly
        RaceColumn loadedColumn1 = loadedLeaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        RaceColumn loadedColumn2 = loadedLeaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertFalse(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn1));
        assertTrue(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn2));
        assertEquals(maxPointsReason, loadedLeaderboard.getScoreCorrection().getMaxPointsReason(competitor, loadedColumn2));
    }

    @Test
    public void testStoreAndRetrieveLeaderboardWithMaxPointsReasonAndScoreCorrection() {
        final String leaderboardName = "TestLeaderboard";
        final String raceColumnName1 = "My First Race 1";
        final String raceColumnName2 = "My First Race 2";
        final double correctedPoints = 2.55;
        final MaxPointsReason maxPointsReason = MaxPointsReason.DNF;
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        FlexibleLeaderboardImpl leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        Competitor competitor = createCompetitor();
        TrackedRace raceWithOneCompetitor1 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 1, /* started */ true);
        TrackedRace raceWithOneCompetitor2 = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 2, /* started */ true);
        leaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace */ false, leaderboard.getFleet(null));
        leaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace */ true, leaderboard.getFleet(null));
        leaderboard.getScoreCorrection().correctScore(competitor, leaderboard.getRaceColumnByName(raceColumnName1), correctedPoints);
        leaderboard.getScoreCorrection().setMaxPointsReason(competitor, leaderboard.getRaceColumnByName(raceColumnName2), maxPointsReason);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        FlexibleLeaderboard loadedLeaderboard = (FlexibleLeaderboard) new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName, /* regattaRegistry */ null);
        // attach tracked race to leaderboard to ensure that competitor object is assigned properly
        RaceColumn loadedColumn1 = loadedLeaderboard.addRace(raceWithOneCompetitor1, raceColumnName1, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        RaceColumn loadedColumn2 = loadedLeaderboard.addRace(raceWithOneCompetitor2, raceColumnName2, /* medalRace, ignored */ false, leaderboard.getFleet(null));
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn1));
        assertEquals(correctedPoints, (double) loadedLeaderboard.getScoreCorrection().getExplicitScoreCorrection(competitor, loadedColumn1), 0.000000001);
        assertTrue(loadedLeaderboard.getScoreCorrection().isScoreCorrected(competitor, loadedColumn2));
        assertEquals(maxPointsReason, loadedLeaderboard.getScoreCorrection().getMaxPointsReason(competitor, loadedColumn2));
    }

    private Competitor createCompetitor() {
        Competitor competitor = new CompetitorImpl(123, "$$$Dr. Wolfgang+Hunger$$$", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("$$$Dr. Wolfgang+Hunger$$$", new NationalityImpl("GER"),
                /* dateOfBirth */ null, "This is famous Dr. Wolfgang Hunger")), new PersonImpl("Rigo van Maas", new NationalityImpl("NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Dr. Wolfgang Hunger's boat", new BoatClassImpl("505", /* typicallyStartsUpwind */ true), null));
        return competitor;
    }

}
