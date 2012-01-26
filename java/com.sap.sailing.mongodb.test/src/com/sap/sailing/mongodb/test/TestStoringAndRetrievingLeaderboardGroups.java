package com.sap.sailing.mongodb.test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;

public class TestStoringAndRetrievingLeaderboardGroups extends AbstractMongoDBTest {
    
    @Test
    public void testStoringAndRetrievingSimpleLeaderboardGroup() {
        MongoObjectFactory mongoObjectFactory = new MongoObjectFactoryImpl(db);
        DomainObjectFactory domainObjectFactory = new DomainObjectFactoryImpl(db);
        
        final String[] leaderboardNames = {"Leaderboard 0", "Leaderboard 1", "Leaderboard 2", "Leaderboard 3"};
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName = "Leaderboard Group";
        final String groupDescription = "A leaderboard group";
        final ArrayList<Leaderboard> leaderboards = new ArrayList<>();

        Leaderboard leaderboard = new LeaderboardImpl(leaderboardNames[0], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        mongoObjectFactory.storeLeaderboard(leaderboard);
        leaderboard = new LeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        mongoObjectFactory.storeLeaderboard(leaderboard);
        leaderboard = new LeaderboardImpl(leaderboardNames[2], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        mongoObjectFactory.storeLeaderboard(leaderboard);
        leaderboard = new LeaderboardImpl(leaderboardNames[3], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        mongoObjectFactory.storeLeaderboard(leaderboard);
        
        final LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
        
        final LeaderboardGroup loadedLeaderboardGroup = domainObjectFactory.loadLeaderboardGroup(groupName);

        Assert.assertEquals(groupName, loadedLeaderboardGroup.getName());
        Assert.assertEquals(groupDescription, loadedLeaderboardGroup.getDescription());
        Assert.assertEquals(leaderboardNames.length, loadedLeaderboardGroup.getLeaderboards().size());
        
        for (int i = 0; i < leaderboardNames.length; i++) {
            Assert.assertEquals(leaderboardNames[i], loadedLeaderboardGroup.getLeaderboards().get(i).getName());
            mongoObjectFactory.removeLeaderboard(leaderboardNames[i]);
        }
    }

}
