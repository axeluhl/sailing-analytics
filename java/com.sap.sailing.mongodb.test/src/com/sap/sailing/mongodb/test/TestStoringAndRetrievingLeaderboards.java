package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.test.MockedTrackedRaceWithFixedRank;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.mongodb.impl.DomainObjectFactoryImpl;
import com.sap.sailing.mongodb.impl.MongoObjectFactoryImpl;
import com.sap.sailing.util.Util;

public class TestStoringAndRetrievingLeaderboards extends AbstractMongoDBTest {
    @Test
    public void testStoreAndRetrieveSimpleLeaderboard() {
        final String leaderboardName = "TestLeaderboard";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        LeaderboardImpl leaderboard = new LeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName);
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(Arrays.equals(discardIndexResultsStartingWithHowManyRaces, loadedLeaderboard.getResultDiscardingRule()
                .getDiscardIndexResultsStartingWithHowManyRaces()));
    }
    
    @Test
    public void testStoreAndRetrieveLeaderboardWithCarryColumn() {
        final String leaderboardName = "TestLeaderboard";
        final String raceColumnName = "My First Race";
        final int carriedPointsForWolfgangHunger = 3;
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        LeaderboardImpl leaderboard = new LeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        Competitor competitor = createCompetitor();
        TrackedRace raceWithOneCompetitor = new MockedTrackedRaceWithFixedRank(competitor, /* rank */ 1, /* started */ true);
        leaderboard.addRace(raceWithOneCompetitor, raceColumnName, /* medalRace */ false);
        leaderboard.setCarriedPoints(competitor, carriedPointsForWolfgangHunger);
        new MongoObjectFactoryImpl(db).storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = new DomainObjectFactoryImpl(db).loadLeaderboard(leaderboardName);
        // attach tracked race to leaderboard to ensure that competitor object is assigned properly
        loadedLeaderboard.addRace(raceWithOneCompetitor, raceColumnName, /* medalRace, ignored */ false);
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(Arrays.equals(discardIndexResultsStartingWithHowManyRaces, loadedLeaderboard.getResultDiscardingRule()
                .getDiscardIndexResultsStartingWithHowManyRaces()));
        assertEquals(1, Util.size(loadedLeaderboard.getCompetitors()));
        assertEquals(competitor, loadedLeaderboard.getCompetitors().iterator().next());
        assertEquals(carriedPointsForWolfgangHunger, loadedLeaderboard.getCarriedPoints(competitor));
    }

    private Competitor createCompetitor() {
        Competitor competitor = new CompetitorImpl(123, "Wolfgang Hunger", new TeamImpl("STG", Collections.singleton(
                new PersonImpl("Wolfgang Hunger", new NationalityImpl("Germany", "GER"),
                /* dateOfBirth */ null, "This is famous Wolfgang Hunger")), new PersonImpl("Rigo van Maas", new NationalityImpl("The Netherlands", "NED"),
                        /* dateOfBirth */ null, "This is Rigo, the coach")), new BoatImpl("Wolfgang Hunger's boat", new BoatClassImpl("505")));
        return competitor;
    }

}
