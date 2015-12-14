package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.connectors.SmartphoneTrackingEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.SeriesEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tracking.RaceColumnTableWrapperPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO.TrackableRaceDescriptor;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestSmartphoneTrackingEventManagementPanel extends AbstractSeleniumTest {
    private static final String IDM_5O5_2013_JSON_URL =
            "http://traclive.dk/events/event_20130917_IDMO/jsonservice.php"; //$NON-NLS-1$
    
    private static final String REGATTA = "IDM 2013"; //$NON-NLS-1$
    
    private static final String LEADERBOARD = "IDM 2013 (5O5)"; //$NON-NLS-1$
    
    private static final String EVENT = "IDM 5O5 2013"; //$NON-NLS-1$
    private static final String BOAT_CLASS = "5O5"; //$NON-NLS-1$
    private static final String RACE = "Race %d"; //$NON-NLS-1$
    
    private RegattaDescriptor regatta;
    
    private List<TrackableRaceDescriptor> trackableRaces;
    private List<TrackedRaceDescriptor> trackedRaces;
    private List<RaceDescriptor> leaderboardRaces;
    
    @Override
    @Before
    public void setUp() {
        this.regatta = new RegattaDescriptor(REGATTA, BOAT_CLASS);
        this.trackableRaces = new ArrayList<>();
        this.trackedRaces = new ArrayList<>();
        this.leaderboardRaces = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String raceName = String.format(RACE, i);
            TrackableRaceDescriptor trackableRace = new TrackableRaceDescriptor(EVENT,  raceName, BOAT_CLASS);
            TrackedRaceDescriptor trackedRace = new TrackedRaceDescriptor(this.regatta.toString(), BOAT_CLASS, raceName);
            RaceDescriptor leaderboardRace = new RaceDescriptor(String.format("R%s", i), "Default", false, false, 0);
            
            this.trackableRaces.add(trackableRace);
            this.trackedRaces.add(trackedRace);
            this.leaderboardRaces.add(leaderboardRace);
        }
        clearState(getContextRoot());
        super.setUp();
        configureLeaderboard();
    }
    
    @Test
    public void testAutomaticSelectionOfLinkedRaceInRaceTable() {
        /*
        LeaderboardPage leaderboard = LeaderboardPage.goToPage(getWebDriver(),getContextRoot(), LEADERBOARD, false);
        LeaderboardTablePO leaderboardTable = leaderboard.getLeaderboardTable();
        List<String> races = leaderboardTable.getRaceNames();
        
        assertThat(races.size(), equalTo(5));
        assertThat(leaderboardTable.getEntries().size(), equalTo(28));
        */
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = adminConsole.goToLeaderboardConfiguration();
        LeaderboardDetailsPanelPO leaderboardDetails = leaderboardConfigurationPanelPO.getLeaderboardDetails(this.regatta.toString());
        
        for (int i = 0; i < 5; i++) {
            leaderboardDetails.linkRace(this.leaderboardRaces.get(i), this.trackedRaces.get(i));
        }
        
        SmartphoneTrackingEventManagementPanelPO smartphoneTrackingPanel = adminConsole.goToSmartphoneTrackingPanel();
        CellTablePO<DataEntryPO> leaderboards = smartphoneTrackingPanel.getLeaderboardTable();
        // select leaderboard
        DataEntryPO entryToSelect = null;
        for(DataEntryPO entry : leaderboards.getEntries()) {
            if(entry.getColumnContent("Name") == LEADERBOARD) {
                entryToSelect = entry;
                break;
            }
        }
        assertNotNull(entryToSelect);
        leaderboards.selectEntry(entryToSelect);
        
        RaceColumnTableWrapperPO raceColumnTableWrapper = smartphoneTrackingPanel.getRaceColumnTableWrapper();
        CellTablePO<DataEntryPO> raceColumnTable = raceColumnTableWrapper.getRaceColumnTable();
        // select RaceColumn
        DataEntryPO raceToSelect = null;
        for (DataEntryPO entry : raceColumnTable.getEntries()) {
            if (entry.getColumnContent("Race") == leaderboardRaces.get(0).getName()) {
                raceToSelect = entry;
            }
        }
        assertNotNull(raceToSelect);
        raceColumnTable.selectEntry(raceToSelect);
        
        TrackedRacesListPO trackedRaces = smartphoneTrackingPanel.getTrackedRaceListComposite();
        List<DataEntryPO> seleceted = trackedRaces.getTrackedRacesTable().getSelectedEntries();
        assertEquals(seleceted.size(),1);
        assertEquals(this.trackedRaces.get(0).race, seleceted.get(0).getColumnContent("Race"));
    }
    
    private void configureLeaderboard() {
        // Open the admin console for some configuration steps
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        // Create a regatta with 1 series and 5 races as well as a leaderborad
        RegattaStructureManagementPanelPO regattaStructure = adminConsole.goToRegattaStructure();
        regattaStructure.createRegatta(this.regatta);
        
        RegattaDetailsCompositePO regattaDetails = regattaStructure.getRegattaDetails(this.regatta);
        SeriesEditDialogPO seriesDialog = regattaDetails.editSeries(RegattaStructureManagementPanelPO.DEFAULT_SERIES_NAME);
        seriesDialog.addRaces(1, 5);
        seriesDialog.pressOk();
        
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.createRegattaLeaderboard(this.regatta);
        
        // Start the tracking for the races and wait until they are ready to use
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(IDM_5O5_2013_JSON_URL);
        tracTracEvents.setReggataForTracking(this.regatta);
        tracTracEvents.setTrackSettings(false, false, false);
        // TODO: There exists a bug in Selenium with key modifiers (Issue 3734 and 6817), so we can't use multi
        //       selection (Firefox on Windows)
        //tracTracEvents.startTrackingForRaces(this.trackableRaces);
        for(TrackableRaceDescriptor race : this.trackableRaces) {
            tracTracEvents.startTrackingForRace(race);
        }
        
        TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        trackedRacesList.waitForTrackedRaces(this.trackedRaces, Status.FINISHED); // TracAPI puts REPLAY races into FINISHED mode when done loading
        // TODO: There exists a bug in Selenium with key modifiers (Issue 3734 and 6817), so we can't use multi
        //       selection (Firefox on Windows)
        //trackedRacesList.stopTracking(this.trackedRaces);
        for (TrackedRaceDescriptor race : this.trackedRaces) {
            trackedRacesList.stopTracking(race);
        }
    }
}