package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.mongodb.DomainObjectFactory;
import com.sap.sailing.mongodb.MongoObjectFactory;

public class TestStoringAndRetrievingLeaderboards extends AbstractMongoDBTest {
    @Test
    public void testStoreAndRetrieveSimpleLeaderboard() {
        String leaderboardName = "TestLeaderboard";
        int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        LeaderboardImpl leaderboard = new LeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        MongoObjectFactory.INSTANCE.storeLeaderboard(leaderboard);
        Leaderboard loadedLeaderboard = DomainObjectFactory.INSTANCE.loadLeaderboard(leaderboardName);
        assertEquals(leaderboardName, loadedLeaderboard.getName());
        assertTrue(Arrays.equals(discardIndexResultsStartingWithHowManyRaces, loadedLeaderboard.getResultDiscardingRule()
                .getDiscardIndexResultsStartingWithHowManyRaces()));
    }
}
