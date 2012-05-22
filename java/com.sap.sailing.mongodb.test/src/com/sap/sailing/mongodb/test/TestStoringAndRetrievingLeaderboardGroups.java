package com.sap.sailing.mongodb.test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RaceColumn;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;

public class TestStoringAndRetrievingLeaderboardGroups extends AbstractMongoDBTest {
    
    private MongoObjectFactory mongoObjectFactory = null;
    private DomainObjectFactory domainObjectFactory = null;
    
    @Before
    public void setUp() {
        mongoObjectFactory = new MongoObjectFactoryImpl(db);
        domainObjectFactory = new DomainObjectFactoryImpl(db);
    }
    
    @Test
    public void testStoringAndRetrievingSimpleLeaderboardGroup() {
        final String[] leaderboardNames = {"Leaderboard 0", "Leaderboard 1", "Leaderboard 2", "Leaderboard 3"};
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName = "Leaderboard Group";
        final String groupDescription = "A leaderboard group";
        final ArrayList<Leaderboard> leaderboards = new ArrayList<>();

        Leaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[0], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[2], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[3], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
        
        final LeaderboardGroup loadedLeaderboardGroup = domainObjectFactory.loadLeaderboardGroup(groupName);

        Assert.assertEquals(groupName, loadedLeaderboardGroup.getName());
        Assert.assertEquals(groupDescription, loadedLeaderboardGroup.getDescription());
        
        int c = 0;
        for (Leaderboard board : leaderboardGroup.getLeaderboards()) {
            Assert.assertEquals(leaderboardNames[c], board.getName());
            c++;
        }
    }
    
    @Test
    public void testGetLeaderboardsNotInGroup() {
        final String[] leaderboardNames = {"Leaderboard 0", "Leaderboard 1"};
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName = "Leaderboard Group";
        final String groupDescription = "A leaderboard group";
        final ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        
        Leaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[0], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);

        final String[] ungroupedLeaderboardNames = {"Ungrouped Leaderboard 0", "Ungrouped Leaderboard 1", "Ungrouped Leaderboard 2"};

        final Leaderboard[] ungroupedLeaderboards = {
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[0], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces)),
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[1], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces)),
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[2], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces)) };
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[0]);
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[1]);
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[2]);
        
        Iterable<Leaderboard> loadedUngroupedLeaderboards = domainObjectFactory.getLeaderboardsNotInGroup();
        
        Assert.assertTrue(loadedUngroupedLeaderboards.iterator().hasNext());
        
        int c = 0;
        for (int i = 0; i < ungroupedLeaderboardNames.length; i++) {
            boolean loadedBoardsContainsName = false;
            for (Leaderboard board : loadedUngroupedLeaderboards) {
                if (ungroupedLeaderboardNames[i].equals(board.getName())) {
                    loadedBoardsContainsName = true;
                    c++;
                    break;
                }
            }
            Assert.assertTrue(loadedBoardsContainsName);
        }
        
        Assert.assertTrue(c == ungroupedLeaderboards.length);
    }
    
    @Test
    public void testLeaderboardReferenceBreak() {
        //Set up
        final String leaderboardName = "Leaderboard 0";
        final String columnName = "Column";
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName = "Leaderboard Group";
        final String groupDescription = "A leaderboard group";
        final ArrayList<Leaderboard> leaderboards = new ArrayList<>();
        
        final FlexibleLeaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces));
        final Fleet fleet = leaderboard.getFleet(null);
        final RaceColumn race = leaderboard.addRaceColumn(columnName, false, fleet);
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup group = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(group);
        
        //Name change test
        final String newLeaderboardName = "Leaderboard ChangedName";
        mongoObjectFactory.renameLeaderboard(leaderboardName, newLeaderboardName);
        leaderboard.setName(newLeaderboardName);
        
        LeaderboardGroup loadedGroup = domainObjectFactory.loadLeaderboardGroup(groupName);
        String loadedLeaderboardName = loadedGroup.getLeaderboards().iterator().next().getName();
        Assert.assertEquals(newLeaderboardName, loadedLeaderboardName);
        
        //RaceIdentifier change test
        final String regattaName = "Event";
        final String raceName = "Race";
        leaderboard.getRaceColumnByName(columnName).setRaceIdentifier(fleet, new RegattaNameAndRaceName(regattaName, raceName));
        mongoObjectFactory.storeLeaderboard(leaderboard);
        
        //Check if the leaderboard updated correctly
        final Leaderboard loadedLeaderboard = domainObjectFactory.loadLeaderboard(leaderboard.getName());
        final RaceColumn loadedRaceColumnByName = loadedLeaderboard.getRaceColumnByName(columnName);
        Fleet loadedFleet = loadedRaceColumnByName.getFleetByName(fleet.getName());
        Assert.assertEquals(race.getRaceIdentifier(fleet), loadedRaceColumnByName.getRaceIdentifier(loadedFleet));
        
        // Check if the group received the changes
        loadedGroup = domainObjectFactory.loadLeaderboardGroup(groupName);
        final RaceColumn loadedRaceColumnFromGroupByName = loadedGroup.getLeaderboards().iterator().next().getRaceColumnByName(columnName);
        Fleet loadedGroupFleet = loadedRaceColumnFromGroupByName.getFleetByName(fleet.getName());
        RaceIdentifier loadedIdentifier = loadedRaceColumnFromGroupByName.getRaceIdentifier(loadedGroupFleet);
        Assert.assertEquals(race.getRaceIdentifier(fleet), loadedIdentifier);
    }

}
