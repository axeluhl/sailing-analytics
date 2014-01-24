package com.sap.sailing.selenium.test.leaderboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.FlexibleLeaderboardCreationDialog;
import com.sap.sailing.selenium.test.adminconsole.pages.LeaderboardConfigurationPanel;

public class TestLeaderboardEditing extends AbstractSeleniumTest {
    @Test
    public void testSimpleLeaderboardEditing() throws UnsupportedEncodingException {
        final String leaderboardName = "Humba Humba";
        createNewLeaderboard(leaderboardName);
        LeaderboardEditingPage page = LeaderboardEditingPage.goToPage(leaderboardName, getWebDriver(), getContextRoot());
        LeaderboardTable table = page.getLeaderboardTable();
        assertNotNull(table);
    }

    private void createNewLeaderboard(String leaderboardName) {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.deleteLeaderboard(leaderboardName);
        {
            FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName(leaderboardName);
            assertTrue(dialog.isOkEnabled());
            dialog.pressOk();
        }
        {
            FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName(leaderboardName);
            assertFalse(dialog.isOkEnabled());
            String errorMessage = dialog.getErrorMessage();
            assertTrue(errorMessage.length() > 0);
            dialog.pressCancel();
        }
        // now create two race columns
        leaderboardConfiguration.selectLeaderboard(leaderboardName);
        leaderboardConfiguration.addRacesToFlexibleLeaderboard(2);
        leaderboardConfiguration.selectRaceColumn("R1", "Default");
    }
}
