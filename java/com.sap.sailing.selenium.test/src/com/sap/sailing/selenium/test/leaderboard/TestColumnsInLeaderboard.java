package com.sap.sailing.selenium.test.leaderboard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.SeriesEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO.TrackableRaceDescriptor;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardPage;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardSettingsDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO.LeaderboardEntry;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestColumnsInLeaderboard extends AbstractSeleniumTest {
    private static final String KIELER_WOCHE_2013_JSON_URL =
            "http://secondary.traclive.dk/events/event_20130621_KielerWoch/jsonservice.php"; //$NON-NLS-1$
    private static final String REGATTA = "KW 2013 Offshore Kaiser-Pokal"; //$NON-NLS-1$
    private static final String LEADERBOARD = "KW 2013 Offshore Kaiser-Pokal (ORC)"; //$NON-NLS-1$
    private static final String EVENT = "Kieler Woche 2013"; //$NON-NLS-1$
    private static final String BOAT_CLASS = "ORC"; //$NON-NLS-1$
    private static final String RACE = "Kaiserpokal"; //$NON-NLS-1$
    
    private RegattaDescriptor regatta;
    private TrackableRaceDescriptor trackableRace;
    private TrackedRaceDescriptor trackedRace;
    private RaceDescriptor raceColumn;
    
    
    @Before
    public void setUp() {
        this.regatta = new RegattaDescriptor(REGATTA, BOAT_CLASS);
        this.trackableRace = new TrackableRaceDescriptor(EVENT,  RACE, BOAT_CLASS);
        this.trackedRace = new TrackedRaceDescriptor(this.regatta.toString(), BOAT_CLASS, RACE);
        this.raceColumn = new RaceDescriptor("R1", "Default", false, false, 0.0);
        clearState(getContextRoot());
        configureLeaderboard();
    }
    
    @Test
    public void testCorrectDisplayOfAllColumns() {
        LeaderboardPage leaderboard = LeaderboardPage.goToPage(getWebDriver(), getContextRoot(), LEADERBOARD, true);
        LeaderboardTablePO leaderboardTable = leaderboard.getLeaderboardTable();
        LeaderboardSettingsDialogPO settings = leaderboard.getLeaderboardSettings();
        settings.setRacesToDisplay(leaderboardTable.getRaceNames());
        settings.showAllOverallDetails();
        settings.showAllRaceDetails();
        settings.showAllLegDetails();
        settings.showAllManeuverDetails();
        settings.pressOk();
        boolean stateHasChanged;
        do {
            List<String> headers = leaderboardTable.getColumnHeaders();
            stateHasChanged = false;
            // Since this test is very expensive we iterate from the end to the beginning because the lower
            // indexes are still valid after the expansion of a column.
            for (int i = headers.size() - 1; i >= 0; i--) {
                if (leaderboardTable.isColumnExpandable(i) && !leaderboardTable.isColumnExpanded(i)) {
                    leaderboardTable.expandColumn(i);
                    stateHasChanged = true;
                    List<LeaderboardEntry> entries = leaderboardTable.getEntries();
                    // NOTE: We have to resolve the headers again for the assertion since there should be more now
                    // because of the expansion
                    assertThat(entries.get(0).getNumberOfColumns(), equalTo(leaderboardTable.getColumnHeaders().size()));
                }
            }
        } while (stateHasChanged);
    }
    
    private void configureLeaderboard() {
        // Open the admin console for some configuration steps
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        // Create a regatta with 1 series and 5 races as well as a leaderboard
        RegattaStructureManagementPanelPO regattaStructure = adminConsole.goToRegattaStructure();
        regattaStructure.createRegatta(this.regatta);
        RegattaDetailsCompositePO regattaDetails = regattaStructure.getRegattaDetails(this.regatta);
        SeriesEditDialogPO seriesDialog = regattaDetails.editSeries(RegattaStructureManagementPanelPO.DEFAULT_SERIES_NAME);
        seriesDialog.addRaces(1, 1);
        seriesDialog.pressOk();
        // Start the tracking for the races and wait until they are ready to use
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(KIELER_WOCHE_2013_JSON_URL);
        tracTracEvents.setReggataForTracking(this.regatta);
        tracTracEvents.setTrackSettings(false, false, false);
        tracTracEvents.startTrackingForRace(this.trackableRace);
        TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        trackedRacesList.waitForTrackedRace(this.trackedRace, Status.FINISHED); // TracAPI puts REPLAY races into FINISHED mode when done loading
        trackedRacesList.stopTracking(this.trackedRace);
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.createRegattaLeaderboard(this.regatta);
        LeaderboardDetailsPanelPO leaderboardDetails = leaderboardConfiguration.getLeaderboardDetails(LEADERBOARD);
        leaderboardDetails.linkRace(this.raceColumn, this.trackedRace);
    }
}
