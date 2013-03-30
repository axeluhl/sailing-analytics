package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.FlexibleLeaderboardCreationDialog;
import com.sap.sailing.selenium.test.adminconsole.pages.LeaderboardConfigurationPanel;

/**
 * <p>Tests for creation of leader boards.</p>
 * 
 * @author
 *   D049941
 */
public class TestLeaderboardCreation extends AbstractSeleniumTest {
    @Test
    public void testCreateFlexibleLeaderboardWithDublicatedName() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        {
            FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName("Humba Humba");
            assertTrue(dialog.isOkEnabled());
            dialog.pressOk();
        }
        {
            FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName("Humba Humba");
            assertFalse(dialog.isOkEnabled());
            String errorMessage = dialog.getErrorMessage();
            assertTrue(errorMessage.length() > 0);
        }
    }
}
