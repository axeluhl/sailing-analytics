package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowerScoreIsBetter;
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
    
    /**
     * Bug 908: asserting that after loading two leaderboard groups referencing the same leaderbard the leaderboard is
     * loaded only once
     */
    @Test
    public void testStoringAndRetrievingTwoLeaderboardGroupsReferencingTheSameLeaderboard() {
        final String[] leaderboardNames = {"Leaderboard 0", "Leaderboard 1", "Leaderboard 2", "Leaderboard 3"};
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName1 = "Leaderboard Group 1";
        final String groupDescription1 = "A leaderboard group 1";
        final ArrayList<Leaderboard> leaderboards1 = new ArrayList<>();
        Leaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[0], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards1.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards1.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[2], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards1.add(leaderboard);
        final LeaderboardGroup leaderboardGroup1 = new LeaderboardGroupImpl(groupName1, groupDescription1, leaderboards1);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup1);
        
        final String groupName2 = "Leaderboard Group 2";
        final String groupDescription2= "A leaderboard group 2";
        final ArrayList<Leaderboard> leaderboards2 = new ArrayList<>();
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[2], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards2.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[3], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards2.add(leaderboard);
        final LeaderboardGroup leaderboardGroup2 = new LeaderboardGroupImpl(groupName2, groupDescription2, leaderboards2);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup2);
        
        // the leaderboard named leaderboardNames[2] occurs in both groups
        final LeaderboardGroup loadedLeaderboardGroup1 = domainObjectFactory.loadLeaderboardGroup(groupName1, /* regattaRegistry */ null);
        final LeaderboardGroup loadedLeaderboardGroup2 = domainObjectFactory.loadLeaderboardGroup(groupName2, /* regattaRegistry */ null);

        assertEquals(groupName1, loadedLeaderboardGroup1.getName());
        assertEquals(groupDescription1, loadedLeaderboardGroup1.getDescription());
        assertEquals(groupName2, loadedLeaderboardGroup2.getName());
        assertEquals(groupDescription2, loadedLeaderboardGroup2.getDescription());
        assertSame(Util.get(loadedLeaderboardGroup1.getLeaderboards(), 2), Util.get(loadedLeaderboardGroup2.getLeaderboards(), 0));
    }
    
    @Test
    public void testStoringAndRetrievingSimpleLeaderboardGroup() {
        final String[] leaderboardNames = {"Leaderboard 0", "Leaderboard 1", "Leaderboard 2", "Leaderboard 3"};
        final int[] discardIndexResultsStartingWithHowManyRaces = new int[] { 5, 8 };
        
        final String groupName = "Leaderboard Group";
        final String groupDescription = "A leaderboard group";
        final ArrayList<Leaderboard> leaderboards = new ArrayList<>();

        Leaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[0], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[2], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[3], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);
        
        final LeaderboardGroup loadedLeaderboardGroup = domainObjectFactory.loadLeaderboardGroup(groupName, /* regattaRegistry */ null,
                /* leaderboardRegistry */ null);

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
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        leaderboard = new FlexibleLeaderboardImpl(leaderboardNames[1], new ScoreCorrectionImpl(),
                new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(leaderboardGroup);

        final String[] ungroupedLeaderboardNames = {"Ungrouped Leaderboard 0", "Ungrouped Leaderboard 1", "Ungrouped Leaderboard 2"};

        final FlexibleLeaderboard[] ungroupedLeaderboards = {
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[0], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter()),
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[1], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter()),
                new FlexibleLeaderboardImpl(ungroupedLeaderboardNames[2], new ScoreCorrectionImpl(),
                        new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter()) };
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[0]);
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[1]);
        mongoObjectFactory.storeLeaderboard(ungroupedLeaderboards[2]);
        
        Iterable<Leaderboard> loadedUngroupedLeaderboards = domainObjectFactory.getLeaderboardsNotInGroup(/* regattaRegistry */ null,
                /* leaderboardRegistry */ null);
        
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
        
        final FlexibleLeaderboard leaderboard = new FlexibleLeaderboardImpl(leaderboardName, new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(discardIndexResultsStartingWithHowManyRaces), new LowerScoreIsBetter());
        final Fleet fleet = leaderboard.getFleet(null);
        final RaceColumn race = leaderboard.addRaceColumn(columnName, false, fleet);
        leaderboards.add(leaderboard);
        
        final LeaderboardGroup group = new LeaderboardGroupImpl(groupName, groupDescription, leaderboards);
        mongoObjectFactory.storeLeaderboardGroup(group);
        
        //Name change test
        final String newLeaderboardName = "Leaderboard ChangedName";
        mongoObjectFactory.renameLeaderboard(leaderboardName, newLeaderboardName);
        leaderboard.setName(newLeaderboardName);
        
        LeaderboardGroup loadedGroup = domainObjectFactory.loadLeaderboardGroup(groupName, /* regattaRegistry */ null, /* leaderboardRegistry */ null);
        String loadedLeaderboardName = loadedGroup.getLeaderboards().iterator().next().getName();
        Assert.assertEquals(newLeaderboardName, loadedLeaderboardName);
        
        //RaceIdentifier change test
        final String regattaName = "Event";
        final String raceName = "Race";
        leaderboard.getRaceColumnByName(columnName).setRaceIdentifier(fleet, new RegattaNameAndRaceName(regattaName, raceName));
        mongoObjectFactory.storeLeaderboard(leaderboard);
        
        //Check if the leaderboard updated correctly
        final Leaderboard loadedLeaderboard = domainObjectFactory.loadLeaderboard(leaderboard.getName(), /* regattaRegistry */ null);
        final RaceColumn loadedRaceColumnByName = loadedLeaderboard.getRaceColumnByName(columnName);
        Fleet loadedFleet = loadedRaceColumnByName.getFleetByName(fleet.getName());
        Assert.assertEquals(race.getRaceIdentifier(fleet), loadedRaceColumnByName.getRaceIdentifier(loadedFleet));
        
        // Check if the group received the changes
        loadedGroup = domainObjectFactory.loadLeaderboardGroup(groupName, /* regattaRegistry */ null, /* leaderboardRegistry */ null);
        final RaceColumn loadedRaceColumnFromGroupByName = loadedGroup.getLeaderboards().iterator().next().getRaceColumnByName(columnName);
        Fleet loadedGroupFleet = loadedRaceColumnFromGroupByName.getFleetByName(fleet.getName());
        RaceIdentifier loadedIdentifier = loadedRaceColumnFromGroupByName.getRaceIdentifier(loadedGroupFleet);
        Assert.assertEquals(race.getRaceIdentifier(fleet), loadedIdentifier);
    }

}
