package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreateDialog;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupConfigurationPanel;
import com.sap.sailing.selenium.pages.gwt.CellTable2;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestLeaderboardFilterInLeaderboardGroupConfig extends AbstractSeleniumTest {
    @Test
    public void testLeaderboardFilterInLeaderboardGroupConfig() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        {
            LeaderboardGroupConfigurationPanel leaderboardGroupConfiguration = adminConsole
                    .goToLeaderboardGroupConfiguration();
            leaderboardGroupConfiguration.deleteLeaderboardGroup("Test");
            leaderboardGroupConfiguration.createLeaderboardGroup("Test", "Test");
            List<DataEntry> selectedEntries = leaderboardGroupConfiguration.getLeaderboardGroupsTable()
                    .getSelectedEntries();
            assertEquals(1, selectedEntries.size());
            assertEquals("Test", selectedEntries.get(0).getColumnContent("Name"));
        }
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.deleteLeaderboard("Test");
        {
            FlexibleLeaderboardCreateDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName("Test");
            assertTrue(dialog.isOkButtonEnabled());
            dialog.pressOk();
        }
        {
            LeaderboardGroupConfigurationPanel leaderboardGroupConfiguration = adminConsole.goToLeaderboardGroupConfiguration();
            leaderboardGroupConfiguration.refreshLeaderboards();
            DataEntry leaderboardEntry = leaderboardGroupConfiguration.findLeaderboardEntry("Test");
            assertNotNull(leaderboardEntry);
            leaderboardGroupConfiguration.filterLeaderboards("Test");
            CellTable2<DataEntry> leaderboardsTable = leaderboardGroupConfiguration.getLeaderboardsTable();
            assertNotNull(leaderboardGroupConfiguration.findLeaderboardEntry("Test"));
        }
    }
}
