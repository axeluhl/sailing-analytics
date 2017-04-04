package com.sap.sailing.selenium.test.raceboard;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO.LeaderboardEntryPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardPageConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardUrlConfigurationDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO.TrackableRaceDescriptor;
import com.sap.sailing.selenium.pages.leaderboard.DetailCheckboxInfo;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardPage;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardSettingsDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardSettingsPanelPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO;
import com.sap.sailing.selenium.pages.raceboard.MapSettingsPO;
import com.sap.sailing.selenium.pages.raceboard.RaceBoardPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SettingsTest extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    private static final String BMW_CUP_EVENT = "BMW Cup";
    private static final String BMW_CUP_BOAT_CLASS = "J80";
    private static final String BMW_CUP_REGATTA = "BMW Cup (J80)"; //$NON-NLS-1$
    private static final String RACE = "BMW Cup Race %d";
    private static final String BMW_CUP_EVENTS_DESC = "BMW Cup Description";
    private static final String BMW_VENUE = "Somewhere";
    private static final Date BMW_START_EVENT_TIME = DatatypeConverter.parseDateTime("2012-04-08T10:09:00-05:00")
            .getTime();
    private static final Date BMW_STOP_EVENT_TIME = DatatypeConverter.parseDateTime("2012-04-08T10:50:00-05:00")
            .getTime();

    private static final String BMW_CUP_RACE_NAME = "R1";

    private TrackableRaceDescriptor trackableRace;

    private TrackedRaceDescriptor trackedRace;

    @Override
    @Before
    public void setUp() {
        this.trackableRace = new TrackableRaceDescriptor(BMW_CUP_EVENT, String.format(RACE, 1), BMW_CUP_BOAT_CLASS);
        this.trackedRace = new TrackedRaceDescriptor(BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS, String.format(RACE, 1));
        clearState(getContextRoot());
        super.setUp();
    }

    // TODO bug 2529: temporarily ignored because make default is deactivated
    @Ignore
    @Test
    public void createRaceAsAdminSetWindSettingToTrue() throws InterruptedException, UnsupportedEncodingException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);

        initTrackingForBmwCupRace(adminConsole);

        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(RACE, 1));
        MapSettingsPO mapSettings = raceboard.openMapSettings();
        mapSettings.setWindChart(true);
        mapSettings.makeDefault();
        // reload
        RaceBoardPage raceboard2 = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(RACE, 1));
        MapSettingsPO mapSettings2 = raceboard2.openMapSettings();
        boolean stillSelected = mapSettings2.isWindChartSelected();
        Assert.assertTrue(stillSelected);
    }

    private void initTrackingForBmwCupRace(AdminConsolePage adminConsole) {
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
        RegattaDescriptor bmwCupDescriptor = new RegattaDescriptor(BMW_CUP_EVENT, BMW_CUP_BOAT_CLASS);
        tracTracEvents.setReggataForTracking(bmwCupDescriptor);
        tracTracEvents.setTrackSettings(true, false, false);
        tracTracEvents.startTrackingForRace(this.trackableRace);
        TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        trackedRacesList.waitForTrackedRace(this.trackedRace, Status.FINISHED, 600); // with the TracAPI, REPLAY races
        // status FINISHED when done loading

        LeaderboardConfigurationPanelPO leaderboard = adminConsole.goToLeaderboardConfiguration();
        LeaderboardDetailsPanelPO details = leaderboard.getLeaderboardDetails(BMW_CUP_REGATTA);
        Assert.assertTrue(details != null);
        details.linkRace(new RaceDescriptor(BMW_CUP_RACE_NAME, "Default", false, false, 0), trackedRace);
    }

    @Test
    @Ignore
    public void testLeaderboardPageSettingsForwarding() {

        // create event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);

        // set custom leaderboard settings
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        LeaderboardEntryPO leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        LeaderboardUrlConfigurationDialogPO urlConfigurationDialog = leaderboardEntry
                .getLeaderboardPageUrlConfigurationDialog();
        LeaderboardSettingsPanelPO leaderboardSettingsPanel = urlConfigurationDialog.goToLeaderboardSettings();

        leaderboardSettingsPanel.setCheckboxValue("R2CheckBox", false);
        DetailCheckboxInfo[] detailsToSelect = {
                // Overall details
                DetailCheckboxInfo.REGATTA_RANK, DetailCheckboxInfo.TOTAL_DISTANCE,
                DetailCheckboxInfo.TOTAL_AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.TIME_ON_TIME_FACTOR,
                DetailCheckboxInfo.TIME_ON_DISTANCE_ALLOWANCE,

                // Race details
                DetailCheckboxInfo.RACE_AVERAGE_SPEED_OVER_GROUND,
                DetailCheckboxInfo.RACE_DISTANCE_INCLUDING_GATE_START, DetailCheckboxInfo.RACE_TIME,
                DetailCheckboxInfo.RACE_CALCULATED_TIME,
                DetailCheckboxInfo.RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD,
                DetailCheckboxInfo.RACE_CURRENT_SPEED_OVER_GROUND, DetailCheckboxInfo.RACE_CURRENT_RIDE_HEIGHT,
                DetailCheckboxInfo.RACE_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD, DetailCheckboxInfo.NUMBER_OF_MANEUVERS,
                DetailCheckboxInfo.DISPLAY_LEGS, DetailCheckboxInfo.CURRENT_LEG,
                DetailCheckboxInfo.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR,
                DetailCheckboxInfo.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR,
                DetailCheckboxInfo.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL,

                // Race Start Analysis
                DetailCheckboxInfo.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_START,
                DetailCheckboxInfo.TIME_BETWEEN_RACE_START_AND_COMPETITOR_START,
                DetailCheckboxInfo.SPEED_OVER_GROUND_AT_RACE_START, DetailCheckboxInfo.SPEED_OVER_GROUND_WHEN_STARTING,
                DetailCheckboxInfo.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_STARTING, DetailCheckboxInfo.START_TACK,

                // Leg Details
                DetailCheckboxInfo.AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.DISTANCE,
                DetailCheckboxInfo.DISTANCE_INCLUDING_START, DetailCheckboxInfo.GAP_TO_LEADER,
                DetailCheckboxInfo.GAP_CHANGE_SINCE_LEG_START,
                DetailCheckboxInfo.SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED,
                DetailCheckboxInfo.CURRENT_SPEED_OVER_GROUND, DetailCheckboxInfo.CURRENT_RIDE_HEIGHT,
                DetailCheckboxInfo.WINDWARD_DISTANCE_TO_GO, DetailCheckboxInfo.NUMBER_OF_MANEVEURS,
                DetailCheckboxInfo.ESTIMATED_TIME_TO_NEXT_WAYPOINT, DetailCheckboxInfo.VELOCITY_MADE_GOOD,
                DetailCheckboxInfo.TIME, DetailCheckboxInfo.CORRECTED_TIME,
                DetailCheckboxInfo.AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR,

                // Maneuvers
                DetailCheckboxInfo.TACK, DetailCheckboxInfo.AVERAGE_TACK_LOSS, DetailCheckboxInfo.JIBE,
                DetailCheckboxInfo.AVERAGE_JIBE_LOSS, DetailCheckboxInfo.PENALTY_CIRCLE

        };
        leaderboardSettingsPanel.selectDetailsAndUnselectOthers(detailsToSelect);

        leaderboardSettingsPanel.setRefreshInterval(2);

        // open settings dialog of configurated leaderboard and match the set values with forwarded values
        LeaderboardPage leaderboardPage = urlConfigurationDialog.openLeaderboard();
        leaderboardSettingsPanel = leaderboardPage.getLeaderboardSettings().getLeaderboardSettingsPanelPO();

        DetailCheckboxInfo[] selectedDetails = leaderboardSettingsPanel.getSelectedDetails();

        Assert.assertArrayEquals(detailsToSelect, selectedDetails);

        Assert.assertTrue(leaderboardSettingsPanel.getCheckboxValue("R1CheckBox"));
        Assert.assertTrue(!leaderboardSettingsPanel.getCheckboxValue("R2CheckBox"));
        Assert.assertTrue(leaderboardSettingsPanel.getCheckboxValue("R3CheckBox"));
        Assert.assertEquals(2, leaderboardSettingsPanel.getRefreshInterval());

        // set different custom leaderboard settings

        adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        urlConfigurationDialog = leaderboardEntry.getLeaderboardPageUrlConfigurationDialog();
        leaderboardSettingsPanel = urlConfigurationDialog.goToLeaderboardSettings();

        leaderboardSettingsPanel.setNumberOfRacesToDisplay(2);
        leaderboardSettingsPanel.selectDetailsAndUnselectOthers();

        // open settings dialog of configurated leaderboard and match the set values with forwarded values
        leaderboardPage = urlConfigurationDialog.openLeaderboard();
        leaderboardSettingsPanel = leaderboardPage.getLeaderboardSettings().getLeaderboardSettingsPanelPO();
        Assert.assertTrue(leaderboardSettingsPanel.isNumberOfRacesToDisplaySelected());
        Assert.assertEquals(2, leaderboardSettingsPanel.getNumberOfRacesToDisplaySelected());
        Assert.assertEquals(1, leaderboardSettingsPanel.getRefreshInterval());
        selectedDetails = leaderboardSettingsPanel.getSelectedDetails();
        //TODO fix leaderboard default values
        // Assert.assertEquals(0, selectedDetails.length);

    }
    
    @Test
    @Ignore
    public void testLeaderboardPageSettingsForwardingWithTrackedRace() {
        // create event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);
        
        initTrackingForBmwCupRace(adminConsole);

        // set custom leaderboard settings
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        LeaderboardEntryPO leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        LeaderboardUrlConfigurationDialogPO urlConfigurationDialog = leaderboardEntry
                .getLeaderboardPageUrlConfigurationDialog();
        
        LeaderboardPageConfigurationPanelPO leaderboardPageSettings = urlConfigurationDialog
                .goToLeaderboardPageSettings();
        leaderboardPageSettings.setAllowForRaceDetails(true);
        leaderboardPageSettings.setShowCharts(false);
        
        LeaderboardSettingsPanelPO leaderboardSettingsPanel = urlConfigurationDialog.goToLeaderboardSettings();

        leaderboardSettingsPanel.setCheckboxValue("R2CheckBox", false);
        DetailCheckboxInfo[] detailsToSelect = new DetailCheckboxInfo[] {
                // Overall details
                DetailCheckboxInfo.TOTAL_TIME, DetailCheckboxInfo.MAXIMUM_SPEED_OVER_GROUND,

                // Race details
                DetailCheckboxInfo.RACE_GAP_TO_LEADER, DetailCheckboxInfo.RACE_DISTANCE,

                // Race Start Analysis
                DetailCheckboxInfo.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START,
                DetailCheckboxInfo.DISTANCE_TO_START_AT_RACE_START,

                //TODO how to get leaderboard to show leg details and maneuvers? Is it required?
                // Leg Details
                // DetailCheckboxInfo.AVERAGE_SIGNED_CROSS_TRACK_ERROR, DetailCheckboxInfo.RANK_GAIN,

                // Maneuvers
                // DetailCheckboxInfo.AVERAGE_MANEUVER_LOSS

        };

        leaderboardSettingsPanel.setCheckboxValue("R2CheckBox", false);
        leaderboardSettingsPanel.setCheckboxValue("R3CheckBox", false);
        leaderboardSettingsPanel.selectDetailsAndUnselectOthers(detailsToSelect);

        // check the displaying leaderboard table columns of a tracked race
        LeaderboardPage leaderboardPage = urlConfigurationDialog.openLeaderboard();
        Assert.assertFalse(leaderboardPage.isCompetitorChartVisible());
        LeaderboardTablePO leaderboardTable = leaderboardPage.getLeaderboardTable();
        leaderboardTable.expandRace("R1");

        List<String> columnHeadersToCheck = Arrays.stream(detailsToSelect).map(detail -> detail.getLabel())
                .collect(Collectors.toList());

        for (String headerToCheck : columnHeadersToCheck) {
            Assert.assertTrue("Column header label \"" + headerToCheck + "\" not found",
                    leaderboardTable.containsColumnHeader(headerToCheck));
        }
        
        //test showRaceDetails and showChart configuration options
        adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        urlConfigurationDialog = leaderboardEntry.getLeaderboardPageUrlConfigurationDialog();
        
        leaderboardPageSettings = urlConfigurationDialog.goToLeaderboardPageSettings();
        leaderboardPageSettings.setAllowForRaceDetails(false);
        leaderboardPageSettings.setShowCharts(true);
        
        
        // open settings dialog of configurated leaderboard and match the set values with forwarded values
        leaderboardPage = urlConfigurationDialog.openLeaderboard();
        Assert.assertTrue(leaderboardPage.isCompetitorChartVisible());
    }
    
    @Test
    @Ignore
    public void testLeaderboardPageSettingsForwardingWithOverallLeaderboard() {
        // create event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true);
        
        // set custom leaderboard settings
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        LeaderboardEntryPO leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        LeaderboardUrlConfigurationDialogPO urlConfigurationDialog = leaderboardEntry
                .getLeaderboardPageUrlConfigurationDialog();
        
        LeaderboardSettingsPanelPO leaderboardSettingsPanel = urlConfigurationDialog.goToLeaderboardSettings();
        
        DetailCheckboxInfo[] detailsToSelect = {
                // Overall details
                DetailCheckboxInfo.TIME_ON_DISTANCE_ALLOWANCE,

                // Race details
                DetailCheckboxInfo.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL,

                // Race Start Analysis
                DetailCheckboxInfo.TIME_BETWEEN_RACE_START_AND_COMPETITOR_START,

                // Leg Details
                DetailCheckboxInfo.SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED,

                // Maneuvers
                DetailCheckboxInfo.AVERAGE_JIBE_LOSS, DetailCheckboxInfo.PENALTY_CIRCLE

        };
        leaderboardSettingsPanel.selectDetailsAndUnselectOthers(detailsToSelect);
        
        LeaderboardPageConfigurationPanelPO leaderboardPageSettings = urlConfigurationDialog
                .goToLeaderboardPageSettings();
        leaderboardPageSettings.setShowOverallLeaderboard(true);
        
        leaderboardSettingsPanel = urlConfigurationDialog.goToOverallLeaderboardSettings();
        
        DetailCheckboxInfo[] overallDetailsToSelect = {
                // Overall details
                DetailCheckboxInfo.TIME_ON_TIME_FACTOR,

                // Race details
                DetailCheckboxInfo.RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD,

                // Race Start Analysis
                DetailCheckboxInfo.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_STARTING,

                // Leg Details
                DetailCheckboxInfo.WINDWARD_DISTANCE_TO_GO,

                // Maneuvers
                DetailCheckboxInfo.AVERAGE_MANEUVER_LOSS

        };
        leaderboardSettingsPanel.selectDetailsAndUnselectOthers(overallDetailsToSelect);

        // open settings dialog of configurated leaderboard and match the set values with forwarded values
        LeaderboardPage leaderboardPage = urlConfigurationDialog.openLeaderboard();
        LeaderboardSettingsDialogPO leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();

        DetailCheckboxInfo[] selectedDetails = leaderboardSettingsPanel.getSelectedDetails();

        Assert.assertArrayEquals(detailsToSelect, selectedDetails);
        
        leaderboardSettingsDialog.pressCancel();
        
        // open settings dialog of configurated OVERALL leaderboard and match the set values with forwarded values
        leaderboardSettingsPanel = leaderboardPage.getOverallLeaderboardSettings().getLeaderboardSettingsPanelPO();

        selectedDetails = leaderboardSettingsPanel.getSelectedDetails();

        Assert.assertArrayEquals(overallDetailsToSelect, selectedDetails);
        
    }
}
