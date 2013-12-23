package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreateDialog;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * <p>Tests for creation of leader boards.</p>
 * 
 * @author
 *   D049941
 */
public class TestLeaderboardCreation extends AbstractSeleniumTest {
    @Before
    public void clearDatabase() {
        clearState(getContextRoot());
    }
    
    @Test
    public void testCreateFlexibleLeaderboardWithDuplicateName() {
        FlexibleLeaderboardCreateDialog dialog;
        
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();

        dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
        dialog.setName("Humba Humba");
        assertTrue(dialog.isOkButtonEnabled());
        dialog.pressOk();

        dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
        dialog.setName("Humba Humba");
        assertFalse(dialog.isOkButtonEnabled());
        String errorMessage = dialog.getStatusMessage();
        assertTrue(errorMessage.length() > 0);
    }
}
