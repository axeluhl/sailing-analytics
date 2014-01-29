package com.sap.sailing.selenium.test.leaderboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.FlexibleLeaderboardCreationDialog;
import com.sap.sailing.selenium.test.adminconsole.pages.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.test.adminconsole.pages.TracTracEventManagementPanel;
import com.sap.sailing.selenium.test.adminconsole.pages.TracTracStartTrackingPanel;

public class TestLeaderboardEditing extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    private static final String BMW_CUP_RACE_8 = "BMW Cup Race 8";
    private static final String BMW_CUP_REGATTA = "BMW Cup";
    private static final String BMW_CUP_BOAT_CLASS = "J80";

    @Test
    public void testSimpleLeaderboardEditing() throws UnsupportedEncodingException, InterruptedException {
        final String leaderboardName = "Humba Humba";
        createNewLeaderboardLoadRaceAndLink(leaderboardName);
        LeaderboardEditingPage page = LeaderboardEditingPage.goToPage(leaderboardName, getWebDriver(), getContextRoot());
        LeaderboardTable table = page.getLeaderboardTable();
        assertNotNull(table);
        Iterable<WebElement> rows = table.getRows();
        assertEquals("Expected 6 rows for the six competitors but found only "+Util.size(rows), 6, Util.size(rows));
    }

    private void createNewLeaderboardLoadRaceAndLink(String leaderboardName) throws InterruptedException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanel leaderboardConfiguration = createLeaderboard(leaderboardName, adminConsole);
        startTrackingRaceAndWait(adminConsole, BMW_CUP_JSON_URL, BMW_CUP_REGATTA, BMW_CUP_RACE_8, 30000l /* 30s timeout */);
        // now create two race columns
        adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.selectLeaderboard(leaderboardName);
        leaderboardConfiguration.addRacesToFlexibleLeaderboard(2);
        leaderboardConfiguration.selectRaceColumn("R1", "Default");
        WebElement trackedRace = leaderboardConfiguration.getTrackedRacesPanel().getTrackedRace(BMW_CUP_REGATTA+" ("+BMW_CUP_BOAT_CLASS+")", BMW_CUP_RACE_8);
        trackedRace.findElements(By.tagName("td")).get(1).click(); // associates the race with the column
    }

    private void startTrackingRaceAndWait(AdminConsolePage adminConsole, String bmwCupJsonUrl, String bmwCupRegatta, String bmwCupRace8, long l) throws InterruptedException {
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listRaces(BMW_CUP_JSON_URL);
        Thread.sleep(500); // wait for list to appear; it seems that the waitForAjaxCalls isn't really reliable...
        TracTracStartTrackingPanel startTrackingPanel = tracTracEvents.getStartTrackingPanel();
        startTrackingPanel.setTrackWind(false);
        tracTracEvents.startTracking(BMW_CUP_REGATTA, BMW_CUP_RACE_8);
        final String trackedRegattaName = BMW_CUP_REGATTA+" ("+BMW_CUP_BOAT_CLASS+")";
        tracTracEvents.waitForTrackedRaceLoadingFinished(trackedRegattaName, BMW_CUP_RACE_8, 30000l /* 30s timeout */);
        tracTracEvents.stopTracking(trackedRegattaName, BMW_CUP_RACE_8);
    }

    private LeaderboardConfigurationPanel createLeaderboard(String leaderboardName, AdminConsolePage adminConsole) {
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.deleteLeaderboard(leaderboardName);
        {
            FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName(leaderboardName);
            assertTrue(dialog.isOkEnabled());
            dialog.pressOk();
        }
        return leaderboardConfiguration;
    }
}
