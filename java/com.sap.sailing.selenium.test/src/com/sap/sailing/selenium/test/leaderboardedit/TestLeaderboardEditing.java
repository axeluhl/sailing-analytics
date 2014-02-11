package com.sap.sailing.selenium.test.leaderboardedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO.TrackableRaceDescriptor;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.leaderboardedit.LeaderboardEditingPage;
import com.sap.sailing.selenium.pages.leaderboardedit.LeaderboardTable;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestLeaderboardEditing extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    private static final String BMW_CUP_RACE_8 = "BMW Cup Race 8";
    private static final String BMW_CUP_REGATTA = "BMW Cup";
    private static final String BMW_CUP_BOAT_CLASS = "J80";
    
    @Before
    public void clearDatabase() {
        clearState(getContextRoot());
    }
    
    @Test
    public void testSimpleLeaderboardEditing() throws UnsupportedEncodingException, InterruptedException {
        final String leaderboardName = "Humba Humba";
        createNewLeaderboardLoadRaceAndLink(leaderboardName);
        LeaderboardEditingPage page = LeaderboardEditingPage.goToPage(leaderboardName, getWebDriver(), getContextRoot());
        LeaderboardTable table = page.getLeaderboardTable();
        assertNotNull(table);
        Iterable<DataEntryPO> rows = table.getRows();
        assertEquals("Expected 6 rows for the six competitors but found only "+Util.size(rows), 6, Util.size(rows));
    }

    private void createNewLeaderboardLoadRaceAndLink(String leaderboardName) throws InterruptedException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanelPO leaderboardConfiguration = createLeaderboard(leaderboardName, adminConsole);
        startTrackingRaceAndWait(adminConsole, BMW_CUP_JSON_URL, BMW_CUP_REGATTA, BMW_CUP_RACE_8, 30000l /* 30s timeout */);
        // now create two race columns
        adminConsole.goToLeaderboardConfiguration();
        LeaderboardDetailsPanelPO leaderboardDetails = leaderboardConfiguration.getLeaderboardDetails(leaderboardName);
        leaderboardDetails.addRacesToFlexibleLeaderboard(2);
        leaderboardDetails.linkRace(
                new RaceDescriptor("R1", "Default", false, false, 0.0),
                new TrackedRaceDescriptor(BMW_CUP_REGATTA + " (" + BMW_CUP_BOAT_CLASS + ")", BMW_CUP_BOAT_CLASS, BMW_CUP_RACE_8));
//        leaderboardConfiguration.selectLeaderboard(leaderboardName);
//        leaderboardConfiguration.addRacesToFlexibleLeaderboard(2);
//        leaderboardConfiguration.selectRaceColumn("R1", "Default");
//        WebElement trackedRace = leaderboardConfiguration.getTrackedRacesPanel().getTrackedRace(BMW_CUP_REGATTA+" ("+BMW_CUP_BOAT_CLASS+")", BMW_CUP_RACE_8);
//        trackedRace.findElements(By.tagName("td")).get(1).click(); // associates the race with the column
    }

    private void startTrackingRaceAndWait(AdminConsolePage adminConsole, String bmwCupJsonUrl, String bmwCupRegatta, String bmwCupRace8, long l) throws InterruptedException {
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
//        Thread.sleep(500); // wait for list to appear; it seems that the waitForAjaxCalls isn't really reliable...
        tracTracEvents.setTrackSettings(false, true, false);
        tracTracEvents.startTrackingForRace(new TrackableRaceDescriptor(BMW_CUP_REGATTA, BMW_CUP_RACE_8, BMW_CUP_BOAT_CLASS));
//        TracTracStartTrackingPanel startTrackingPanel = tracTracEvents.getStartTrackingPanel();
//        startTrackingPanel.setTrackWind(false);
//        tracTracEvents.startTracking(BMW_CUP_REGATTA, BMW_CUP_RACE_8);
        
        final String trackedRegattaName = BMW_CUP_REGATTA + " (" + BMW_CUP_BOAT_CLASS + ")";
        final List<TrackedRaceDescriptor> races = Arrays.asList(new TrackedRaceDescriptor(trackedRegattaName, BMW_CUP_BOAT_CLASS, BMW_CUP_RACE_8));
        TrackedRacesManagementPanelPO trackedRaces = adminConsole.goToTrackedRaces();
        TrackedRacesListPO trackedRacesList = trackedRaces.getTrackedRacesList();
        trackedRacesList.waitForTrackedRaces(races);
        trackedRacesList.stopTracking(races);
    }

    private LeaderboardConfigurationPanelPO createLeaderboard(String leaderboardName, AdminConsolePage adminConsole) {
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.deleteLeaderboard(leaderboardName);
        {
            FlexibleLeaderboardCreateDialogPO dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
            dialog.setName(leaderboardName);
            assertTrue(dialog.isOkButtonEnabled());
            dialog.pressOk();
        }
        return leaderboardConfiguration;
    }
}
