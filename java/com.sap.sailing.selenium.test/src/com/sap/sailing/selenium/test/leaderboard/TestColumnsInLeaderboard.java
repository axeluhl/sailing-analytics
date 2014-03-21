package com.sap.sailing.selenium.test.leaderboard;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
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
    private static final String IDM_5O5_2013_JSON_URL =
            "http://traclive.dk/events/event_20130917_IDMO/jsonservice.php"; //$NON-NLS-1$
    
    private static final String REGATTA = "IDM 2013"; //$NON-NLS-1$
    
    private static final String LEADERBOARD = "IDM 2013 (505)"; //$NON-NLS-1$
    
    private static final String EVENT = "IDM 5O5 2013"; //$NON-NLS-1$
    private static final String BOAT_CLASS = "505"; //$NON-NLS-1$
    private static final String RACE = "Race %d"; //$NON-NLS-1$
    
    private RegattaDescriptor regatta;

    private List<TrackableRaceDescriptor> trackableRaces;
    private List<TrackedRaceDescriptor> trackedRaces;
    private List<RaceDescriptor> raceColumns;
    
    
    @Before
    public void setUp() {
        this.regatta = new RegattaDescriptor(REGATTA, BOAT_CLASS);

        this.trackableRaces = new ArrayList<>();
        this.trackedRaces = new ArrayList<>();
        this.raceColumns = new ArrayList<>();
        
        for(int i = 1; i <= 2; i++) {
            TrackableRaceDescriptor trackableRace = new TrackableRaceDescriptor(EVENT,  String.format(RACE, i), BOAT_CLASS);
            TrackedRaceDescriptor trackedRace = new TrackedRaceDescriptor(this.regatta.toString(), BOAT_CLASS, String.format(RACE, i));
            RaceDescriptor raceColumn = new RaceDescriptor(String.format("R%d", i), "Default", false, false, 0.0);
            
            this.trackableRaces.add(trackableRace);
            this.trackedRaces.add(trackedRace);
            this.raceColumns.add(raceColumn);
        }
        
        clearState(getContextRoot());
        configureLeaderboard();
    }
    
    @Test
    public void testCorrectDisplayOfAllColumns() {
        LeaderboardPage leaderboard = LeaderboardPage.goToPage(getWebDriver(), getContextRoot(), LEADERBOARD, true);
        LeaderboardTablePO leaderboardTabel = leaderboard.getLeaderboardTable();
        
        LeaderboardSettingsDialogPO settings = leaderboard.getLeaderboardSettings();
        settings.setRacesToDisplay(leaderboardTabel.getRaceNames());
        settings.showAllOverallDetails();
        settings.showAllRaceDetails();
        settings.showAllLegDetails();
        settings.showAllManeuverDetails();
        settings.pressOk();
        
        boolean stateHasChanged;
        
        do {
            List<String> headers = leaderboardTabel.getColumnHeaders();
            stateHasChanged = false;
            
            // Since this test is very expensive we iterate from the end to the beginning because the lower
            // indexes are still valid after the expansion of a column.
            for(int i = headers.size() - 1; i >= 0; i--) {
                if(leaderboardTabel.isColumnExpandable(i) && !leaderboardTabel.isColumnExpanded(i)) {
                    leaderboardTabel.expandColumn(i);
                    stateHasChanged = true;
                    
                    List<LeaderboardEntry> entries = leaderboardTabel.getEntries();
                    // NOTE: We have to resolve the headers again for the assertion since there should be more now
                    //       because of the expansion
                    assertThat(entries.get(0).getNumberOfColumns(), equalTo(leaderboardTabel.getColumnHeaders().size()));
                    
                    // TODO: Write matchers for this assertions
                    //LeaderboardEntry emptyEntry = leaderboardTabel.getEntry("");
                    //assertThat(emptyEntry, allColumnsEmpty());
                    //LeaderboardEntry filledEntry = leaderboardTabel.getEntry("");
                    //assertThat(emptyEntry, allColumnsFilled());
                }
            }
        } while(stateHasChanged);
    }
    
    private void configureLeaderboard() {
        // Open the admin console for some configuration steps
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
                
        // Create a regatta with 1 series and 5 races as well as a leaderborad
        RegattaStructureManagementPanelPO regattaStructure = adminConsole.goToRegattaStructure();
        regattaStructure.createRegatta(this.regatta);
        
        RegattaDetailsCompositePO regattaDetails = regattaStructure.getRegattaDetails(this.regatta);
        SeriesEditDialogPO seriesDialog = regattaDetails.editSeries(RegattaStructureManagementPanelPO.DEFAULT_SERIES_NAME);
        seriesDialog.addRaces(1, 2);
        seriesDialog.pressOk();
        
        // Start the tracking for the races and wait until they are ready to use
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(IDM_5O5_2013_JSON_URL);
        tracTracEvents.setReggataForTracking(this.regatta);
        tracTracEvents.setTrackSettings(false, false, false);
        // TODO: There exists a bug in Selenium with key modifiers (Issue 3734 and 6817), so we can't use multi
        //       selection (Firefox on Windows)
        //tracTracEvents.startTrackingForRaces(this.trackableRaces);
        tracTracEvents.startTrackingForRace(this.trackableRaces.get(0));
        tracTracEvents.startTrackingForRace(this.trackableRaces.get(1));
        
        TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        trackedRacesList.waitForTrackedRaces(this.trackedRaces, Status.TRACKING);
        // TODO: There exists a bug in Selenium with key modifiers (Issue 3734 and 6817), so we can't use multi
        //       selection (Firefox on Windows)
        //trackedRacesList.stopTracking(this.trackedRaces);
        trackedRacesList.stopTracking(this.trackedRaces.get(0));
        trackedRacesList.stopTracking(this.trackedRaces.get(1));
        
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.createRegattaLeaderboard(this.regatta);
        
        LeaderboardDetailsPanelPO leaderboardDetails = leaderboardConfiguration.getLeaderboardDetails(LEADERBOARD);
        leaderboardDetails.linkRace(this.raceColumns.get(0), this.trackedRaces.get(0));
        leaderboardDetails.linkRace(this.raceColumns.get(1), this.trackedRaces.get(1));
    }
}
