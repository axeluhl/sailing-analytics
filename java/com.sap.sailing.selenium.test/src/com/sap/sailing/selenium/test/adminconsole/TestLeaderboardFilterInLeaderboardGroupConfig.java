package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupDetailsPanel;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestLeaderboardFilterInLeaderboardGroupConfig extends AbstractSeleniumTest {
    @Before
    public void clearDatabase() {
        clearState(getContextRoot());
    }
    
    @Test
    public void testLeaderboardFilterInLeaderboardGroupConfig() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        LeaderboardGroupConfigurationPanel leaderboardGroupConfiguration = adminConsole.goToLeaderboardGroupConfiguration();
        leaderboardGroupConfiguration.createLeaderboardGroup("Test", "Test");
        List<DataEntry> selectedEntries = leaderboardGroupConfiguration.getLeaderboardGroupsTable().getSelectedEntries();
        assertEquals(1, selectedEntries.size());
        assertEquals("Test", selectedEntries.get(0).getColumnContent("Name"));
        
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.createFlexibleLeaderboard("Test");

        leaderboardGroupConfiguration = adminConsole.goToLeaderboardGroupConfiguration();
        LeaderboardGroupDetailsPanel leaderboardGroupDetails = leaderboardGroupConfiguration.getLeaderboardGroupDetails();
        leaderboardGroupDetails.refreshLeaderboards();
        DataEntry leaderboardEntry = leaderboardGroupDetails.findLeaderboardEntry("Test");
        assertNotNull(leaderboardEntry);
        
        leaderboardGroupDetails.filterLeaderboards("Test");
        assertNotNull(leaderboardGroupDetails.findLeaderboardEntry("Test"));
    }
}
