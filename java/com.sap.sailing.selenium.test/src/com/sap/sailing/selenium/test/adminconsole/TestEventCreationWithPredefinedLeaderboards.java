package com.sap.sailing.selenium.test.adminconsole;

import static com.sap.sailing.selenium.pages.common.DateHelper.getFutureDate;
import static com.sap.sailing.selenium.pages.common.DateHelper.getPastDate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO.EventEntryPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupConfigurationPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestEventCreationWithPredefinedLeaderboards extends AbstractSeleniumTest {
    private AdminConsolePage adminConsolePage;
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
    }
    
    @Test
    public void createTestEventLinkingToExistingLeaderboardGroups() {
        LeaderboardGroupConfigurationPanelPO leaderboardGroupsConfigPanel = adminConsolePage.goToLeaderboardGroupConfiguration();
        leaderboardGroupsConfigPanel.deleteLeaderboardGroup("A");
        leaderboardGroupsConfigPanel.deleteLeaderboardGroup("B");
        leaderboardGroupsConfigPanel.deleteLeaderboardGroup("C");
        EventConfigurationPanelPO eventConfigPanel = adminConsolePage.goToEvents();
        eventConfigPanel.createEventWithExistingLeaderboardGroups("My Event", "My Description", "My Venue", getPastDate(3), getFutureDate(3), true, "A", "C");
        EventConfigurationPanelPO eventsPanel = adminConsolePage.goToEvents();
        EventEntryPO eventEntry = eventsPanel.getEventEntry("My Event");
        String leaderboardGroupsListedInEventsTable = eventEntry.getColumnContent("Leaderboard groups");
        assertTrue(leaderboardGroupsListedInEventsTable.contains("A"));
        assertTrue(leaderboardGroupsListedInEventsTable.contains("C"));
        assertFalse(leaderboardGroupsListedInEventsTable.contains("B"));

    }

}
