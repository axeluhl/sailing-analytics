package com.sap.sailing.selenium.test.raceboard;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
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
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
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
import com.sap.sailing.selenium.pages.regattaoverview.RegattaOverviewPage;
import com.sap.sailing.selenium.pages.regattaoverview.RegattaOverviewSettingsDialogPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SettingsTest extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    private static final String BMW_CUP_EVENT = "BMW Cup";
    private static final String AUDI_CUP_EVENT = "Audi Business Cup";
    private static final String BMW_CUP_BOAT_CLASS = "J80";
    private static final String AUDI_CUP_BOAT_CLASS = "J70";
    private static final String BMW_CUP_REGATTA = "BMW Cup (J80)"; //$NON-NLS-1$
    private static final String AUDI_CUP_REGATTA = "Audi Business Cup (J70)"; //$NON-NLS-1$
    private static final String BMW_RACE = "BMW Cup Race %d";
    private static final String BMW_CUP_EVENTS_DESC = "BMW Cup Description";
    private static final String AUDI_CUP_EVENTS_DESC = "";
    private static final String BMW_VENUE = "Somewhere";
    private static final String AUDI_VENUE = "Somewhere else";
    private static final Date BMW_START_EVENT_TIME = DatatypeConverter.parseDateTime("2012-04-08T10:09:00-05:00")
            .getTime();
    private static final Date AUDI_START_EVENT_TIME = DatatypeConverter.parseDateTime("2017-04-05T10:09:00-05:00")
            .getTime();
    private static final Date BMW_STOP_EVENT_TIME = DatatypeConverter.parseDateTime("2017-04-08T10:50:00-05:00")
            .getTime();
    private static final Date AUDI_STOP_EVENT_TIME = DatatypeConverter.parseDateTime("2017-04-05T10:50:00-05:00")
            .getTime();

    private static final String BMW_CUP_RACE_NAME = "R1";

    private static final String CUSTOM_COURSE_AREA = "Custom X";

    private static final String URL_PARAMETER_IGNORE_LOCAL_SETTINGS = "ignoreLocalSettings=true";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    // This test is currently ignored due to changes in the RaceBoardMod-based settings semantic.
    // Modes settings were formerly patched on the user defaults but are now patched on top of the system defaults.
    // In addition, there are now different settigns keys per mode so that settigns for one mode do not have an effect on other modes.
    // To verify this, there are new tests below but these do not test ReceBoard's embedded Leaderboard.
    /**
     * Verifies the settings storage of the raceboard. Checks the precedences of url, context specific settings, mode
     * settings and global settings.
     */
    @Test
    public void testRaceBoardPageSettingsStorage() throws InterruptedException, UnsupportedEncodingException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);

        initTrackingForBmwCupRace(adminConsole);

        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(BMW_RACE, 1), "PLAYER");
        
        DetailCheckboxInfo[] detailsToSelect = new DetailCheckboxInfo[] {
                // Overall details

                // Race details
                DetailCheckboxInfo.RACE_GAP_TO_LEADER, DetailCheckboxInfo.RACE_CURRENT_SPEED_OVER_GROUND,
                DetailCheckboxInfo.DISPLAY_LEGS,

                // Leg Details
                DetailCheckboxInfo.AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.DISTANCE, DetailCheckboxInfo.RANK_GAIN,

                // Maneuvers
                DetailCheckboxInfo.TACK, DetailCheckboxInfo.JIBE, DetailCheckboxInfo.PENALTY_CIRCLE

        };

        LeaderboardSettingsDialogPO leaderboardSettingsDialog = raceboard.openLeaderboardSettingsDialog();
        LeaderboardSettingsPanelPO leaderboardSettingsPanelPO = leaderboardSettingsDialog
                .getLeaderboardSettingsPanelPO();
        DetailCheckboxInfo[] selectedDetails = leaderboardSettingsPanelPO.getSelectedDetails();
        Assert.assertArrayEquals(detailsToSelect, selectedDetails);
        leaderboardSettingsPanelPO.setRefreshInterval(2);
        leaderboardSettingsDialog.pressMakeDefault();
        leaderboardSettingsDialog.pressCancel();

        MapSettingsPO mapSettings = raceboard.openMapSettings();
        // Verify initial mode settings
        Assert.assertFalse(mapSettings.isWindUp());
        Assert.assertFalse(mapSettings.isShowOnlySelectedCompetitors());
        mapSettings.setTransparentHoverlines(true);
        mapSettings.setWindUp(false);
        mapSettings.pressMakeDefault();

        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "WINNING_LANES");

        detailsToSelect = new DetailCheckboxInfo[] {
                // Race details
                DetailCheckboxInfo.RACE_DISTANCE, DetailCheckboxInfo.RACE_TIME,
                DetailCheckboxInfo.RACE_AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR,
                DetailCheckboxInfo.RACE_AVERAGE_SIGNED_CROSS_TRACK_ERROR,
                // Leg Details
                DetailCheckboxInfo.AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.DISTANCE, DetailCheckboxInfo.RANK_GAIN,
                // Maneuvers
                DetailCheckboxInfo.TACK, DetailCheckboxInfo.JIBE, DetailCheckboxInfo.PENALTY_CIRCLE

        };

        leaderboardSettingsDialog = raceboard.openLeaderboardSettingsDialog();
        leaderboardSettingsPanelPO = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        selectedDetails = leaderboardSettingsPanelPO.getSelectedDetails();
        Assert.assertArrayEquals(detailsToSelect, selectedDetails);

        detailsToSelect = new DetailCheckboxInfo[] {
                // Race details
                DetailCheckboxInfo.RACE_GAP_TO_LEADER, DetailCheckboxInfo.RACE_DISTANCE, DetailCheckboxInfo.RACE_TIME,
                DetailCheckboxInfo.RACE_CURRENT_SPEED_OVER_GROUND,
                DetailCheckboxInfo.RACE_RATIO_BETWEEN_TIME_SINCE_LAST_POSITION_FIX_AND_AVERAGE_SAMPLING_INTERVAL,
                DetailCheckboxInfo.DISPLAY_LEGS,
                // Leg Details
                DetailCheckboxInfo.AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.DISTANCE, DetailCheckboxInfo.RANK_GAIN,
                // Maneuvers
                DetailCheckboxInfo.TACK, DetailCheckboxInfo.JIBE, DetailCheckboxInfo.PENALTY_CIRCLE

        };
        leaderboardSettingsPanelPO.selectDetailsAndDeselectOther(detailsToSelect);
        leaderboardSettingsPanelPO.setRefreshInterval(1);
        leaderboardSettingsDialog.pressOk(false, false);

        mapSettings = raceboard.openMapSettings();
        // verify default mode settings override custom user settings
        Assert.assertTrue(mapSettings.isWindUp());
        // verify default mode settings override system defaults
        Assert.assertTrue(mapSettings.isShowOnlySelectedCompetitors());
        // Verify custom user settings are independent for modes
        Assert.assertFalse(mapSettings.isTransparentHoverlines());
        mapSettings.setWindUp(false);
        mapSettings.setTransparentHoverlines(false);
        
        mapSettings.pressOk(false, false);

        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "WINNING_LANES");

        leaderboardSettingsDialog = raceboard.openLeaderboardSettingsDialog();
        leaderboardSettingsPanelPO = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        selectedDetails = leaderboardSettingsPanelPO.getSelectedDetails();

        // verify highest precedence of document settings
        Assert.assertArrayEquals(detailsToSelect, selectedDetails);
        // verify that document settings are able to override custom user settings by a system default value
        Assert.assertEquals(1, leaderboardSettingsPanelPO.getRefreshInterval());

        leaderboardSettingsDialog.pressCancel();

        mapSettings = raceboard.openMapSettings();
        // Verify that mode settings are overridden by document settings
        Assert.assertFalse(mapSettings.isWindUp());
        // Verify custom user settings are independent for modes
        Assert.assertFalse(mapSettings.isTransparentHoverlines());

        //FIXME uncomment when START_ANALYSIS mode can be handled by remote CI server
         // verify that custom document settings override mode settings of other modes
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "START_ANALYSIS");
        leaderboardSettingsDialog = raceboard.openLeaderboardSettingsDialog();
        leaderboardSettingsPanelPO = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        selectedDetails = leaderboardSettingsPanelPO.getSelectedDetails();

        detailsToSelect = new DetailCheckboxInfo[] { DetailCheckboxInfo.RACE_GAP_TO_LEADER, // start analysis mode
                DetailCheckboxInfo.RACE_DISTANCE_TO_START_FIVE_SECONDS_BEFORE_START, // start analysis mode
                DetailCheckboxInfo.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START, // start analysis mode
                DetailCheckboxInfo.DISTANCE_TO_START_AT_RACE_START, // start analysis mode
                DetailCheckboxInfo.SPEED_OVER_GROUND_AT_RACE_START, // start analysis mode
                DetailCheckboxInfo.SPEED_OVER_GROUND_WHEN_STARTING, // start analysis mode
                DetailCheckboxInfo.DISTANCE_TO_STARBOARD_END_OF_STARTLINE_WHEN_STARTING, // start analysis mode
                DetailCheckboxInfo.START_TACK, // start analysis mode

                DetailCheckboxInfo.AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.DISTANCE, DetailCheckboxInfo.RANK_GAIN,

                DetailCheckboxInfo.TACK, DetailCheckboxInfo.JIBE, DetailCheckboxInfo.PENALTY_CIRCLE };
        Assert.assertArrayEquals(detailsToSelect, selectedDetails);
        leaderboardSettingsDialog.pressCancel();

        mapSettings = raceboard.openMapSettings();
        Assert.assertTrue(mapSettings.isWindUp());
    }

    /**
     * Verifies that settings for different modes are distinct and do not have an effect on other modes. 
     */
    @Test
    public void testThatUserDefaultsForOneModeDoNotHaveAnEffectOnAnotherMode() throws InterruptedException, UnsupportedEncodingException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);
        
        initTrackingForBmwCupRace(adminConsole);
        
        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(BMW_RACE, 1), "PLAYER");
        
        MapSettingsPO mapSettings = raceboard.openMapSettings();
        // Verify initial settings for mode PLAYER
        Assert.assertFalse(mapSettings.isShowWindStreamletOverlay());
        
        mapSettings.setTransparentHoverlines(true);
        mapSettings.setWindUp(false);
        mapSettings.pressMakeDefault();
        
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "WINNING_LANES");

        mapSettings = raceboard.openMapSettings();
        // Verify initial settings for mode WINNING_LANES
        Assert.assertFalse(mapSettings.isShowWindStreamletOverlay());
        
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(BMW_RACE, 1), "PLAYER");
        
        // Map settings for mode PLAYER are changed
        mapSettings = raceboard.openMapSettings();
        mapSettings.setShowWindStreamletOverlay(true);
        mapSettings.pressMakeDefault();
        
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "WINNING_LANES");

        mapSettings = raceboard.openMapSettings();
        // Verify settings for mode WINNING_LANES are unchanged
        Assert.assertFalse(mapSettings.isShowWindStreamletOverlay());
    }
    
    /**
     * Verifies that settings are stored for raceboard.
     */
    @Test
    public void testThatSettingsAreStoredForOneMode() throws InterruptedException, UnsupportedEncodingException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);
        
        initTrackingForBmwCupRace(adminConsole);
        
        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(BMW_RACE, 1), "PLAYER");
        
        MapSettingsPO mapSettings = raceboard.openMapSettings();
        // Verify initial mode settings
        Assert.assertFalse(mapSettings.isWindUp());
        Assert.assertFalse(mapSettings.isShowOnlySelectedCompetitors());
        mapSettings.setWindUp(true);
        mapSettings.setShowOnlySelectedCompetitors(true);
        mapSettings.pressMakeDefault();
        
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "PLAYER");
        
        mapSettings = raceboard.openMapSettings();
        // verify default settings work
        Assert.assertTrue(mapSettings.isWindUp());
        Assert.assertTrue(mapSettings.isShowOnlySelectedCompetitors());
    }
    
    /**
     * Verifies that settings are stored for raceboard.
     */
    @Test
    public void testThatModeDependentSettingsAreStoredForOneMode() throws InterruptedException, UnsupportedEncodingException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);
        
        initTrackingForBmwCupRace(adminConsole);
        
        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA,
                BMW_CUP_REGATTA, String.format(BMW_RACE, 1), "WINNING_LANES");
        
        MapSettingsPO mapSettings = raceboard.openMapSettings();
        // The following options are false in the system default but activated by the WINNING_LANES mode
        Assert.assertTrue(mapSettings.isWindUp());
        Assert.assertTrue(mapSettings.isShowOnlySelectedCompetitors());
        mapSettings.setWindUp(false);
        mapSettings.setShowOnlySelectedCompetitors(false);
        mapSettings.pressMakeDefault();
        
        raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), BMW_CUP_REGATTA, BMW_CUP_REGATTA,
                String.format(BMW_RACE, 1), "WINNING_LANES");
        
        mapSettings = raceboard.openMapSettings();
        // verify mode settigns have been overwritten
        Assert.assertFalse(mapSettings.isWindUp());
        Assert.assertFalse(mapSettings.isShowOnlySelectedCompetitors());
    }

    private void initTrackingForBmwCupRace(AdminConsolePage adminConsole) {
        TrackableRaceDescriptor trackableRace = new TrackableRaceDescriptor(BMW_CUP_EVENT, String.format(BMW_RACE, 1),
                BMW_CUP_BOAT_CLASS);
        TrackedRaceDescriptor trackedRace = new TrackedRaceDescriptor(BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                String.format(BMW_RACE, 1));
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
        RegattaDescriptor bmwCupDescriptor = new RegattaDescriptor(BMW_CUP_EVENT, BMW_CUP_BOAT_CLASS);
        tracTracEvents.setReggataForTracking(bmwCupDescriptor);
        tracTracEvents.setTrackSettings(true, false, false);
        tracTracEvents.startTrackingForRace(trackableRace);
        TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        trackedRacesList.waitForTrackedRace(trackedRace, Status.FINISHED, 600); // with the TracAPI, REPLAY races
        // status FINISHED when done loading
        LeaderboardConfigurationPanelPO leaderboard = adminConsole.goToLeaderboardConfiguration();
        LeaderboardDetailsPanelPO details = leaderboard.getLeaderboardDetails(BMW_CUP_REGATTA);
        Assert.assertTrue(details != null);
        details.linkRace(new RaceDescriptor(BMW_CUP_RACE_NAME, "Default", false, false, 0), trackedRace);
    }

    /**
     * Verifies that the url serialisation of leaderboard settings is working well between admin console and leaderboard
     * panel. At first, a leaderboard with custom refresh interval, one deselected race and all details selected is
     * tested. Secondly, a leaderboard with last two races and none details selected is tested. None details selection
     * is correctly not supported, thats why the corresponding assertion has been commented out.
     * 
     */
    @Test
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
        
        DetailCheckboxInfo[] detailsToSelect = {
                DetailCheckboxInfo.SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED
        };

        leaderboardSettingsPanel.selectDetailsAndDeselectOther(detailsToSelect);
        leaderboardSettingsPanel.setCheckboxValue("R2CheckBox", false);
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
        
        detailsToSelect = new DetailCheckboxInfo[] {
                // Overall details
                DetailCheckboxInfo.REGATTA_RANK, DetailCheckboxInfo.TOTAL_DISTANCE,
                DetailCheckboxInfo.TOTAL_AVERAGE_SPEED_OVER_GROUND, DetailCheckboxInfo.TIME_ON_TIME_FACTOR,
                DetailCheckboxInfo.TIME_ON_DISTANCE_ALLOWANCE,

                // Race details
                DetailCheckboxInfo.RACE_AVERAGE_SPEED_OVER_GROUND,
                DetailCheckboxInfo.RACE_DISTANCE_INCLUDING_GATE_START, DetailCheckboxInfo.RACE_TIME,
                DetailCheckboxInfo.RACE_CALCULATED_TIME,
                DetailCheckboxInfo.RACE_CALCULATED_TIME_AT_ESTIMATED_ARRIVAL_AT_COMPETITOR_FARTHEST_AHEAD,
                DetailCheckboxInfo.RACE_CURRENT_SPEED_OVER_GROUND,
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
                DetailCheckboxInfo.CURRENT_SPEED_OVER_GROUND,
                DetailCheckboxInfo.WINDWARD_DISTANCE_TO_GO, DetailCheckboxInfo.NUMBER_OF_MANEVEURS,
                DetailCheckboxInfo.ESTIMATED_TIME_TO_NEXT_WAYPOINT, DetailCheckboxInfo.VELOCITY_MADE_GOOD,
                DetailCheckboxInfo.TIME, DetailCheckboxInfo.CORRECTED_TIME,
                DetailCheckboxInfo.AVERAGE_ABSOLUTE_CROSS_TRACK_ERROR,

                // Maneuvers
                DetailCheckboxInfo.TACK, DetailCheckboxInfo.AVERAGE_TACK_LOSS, DetailCheckboxInfo.JIBE,
                DetailCheckboxInfo.AVERAGE_JIBE_LOSS, DetailCheckboxInfo.PENALTY_CIRCLE

        };

        leaderboardSettingsPanel.selectDetailsAndDeselectOther(detailsToSelect);
        leaderboardSettingsPanel.setNumberOfRacesToDisplay(2);

        // TODO the URL generated here is too long for IE11. IE has a URL size limit of 2083 chars. This causes the test to fail.

        // open settings dialog of configurated leaderboard and match the set values with forwarded values
        leaderboardPage = urlConfigurationDialog.openLeaderboard();
        leaderboardSettingsPanel = leaderboardPage.getLeaderboardSettings().getLeaderboardSettingsPanelPO();
        Assert.assertTrue(leaderboardSettingsPanel.isNumberOfRacesToDisplaySelected());
        Assert.assertEquals(2, leaderboardSettingsPanel.getNumberOfRacesToDisplaySelected());
        Assert.assertEquals(3, leaderboardSettingsPanel.getRefreshInterval());
        selectedDetails = leaderboardSettingsPanel.getSelectedDetails();
        Assert.assertArrayEquals(detailsToSelect, selectedDetails);

    }

    /**
     * Verifies that the leaderboard displays the configured details in the leaderboard table as columns. Furthermore,
     * the chart presence is verified when the corresponding settings in admin console has been checked. In order to get
     * the race details displayed, a tracked race is used.
     */
    @Test
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

                // TODO how to get leaderboard to show leg details and maneuvers? Is it required?
                // Leg Details
                // DetailCheckboxInfo.AVERAGE_SIGNED_CROSS_TRACK_ERROR, DetailCheckboxInfo.RANK_GAIN,

                // Maneuvers
                // DetailCheckboxInfo.AVERAGE_MANEUVER_LOSS

        };

        leaderboardSettingsPanel.setCheckboxValue("R2CheckBox", false);
        leaderboardSettingsPanel.setCheckboxValue("R3CheckBox", false);
        leaderboardSettingsPanel.selectDetailsAndDeselectOther(detailsToSelect);

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

        // test showRaceDetails and showChart configuration options
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

    /**
     * Verifies the presence and the own settings of the overall leaderboard when the corresponding setting in the admin
     * console has been checked and the settings for overall leaderboard has been set differently than the settings of
     * the regatta leaderboard. The settings of both leaderboards get asserted separately.
     */
    @Test
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
        leaderboardSettingsPanel.selectDetailsAndDeselectOther(detailsToSelect);

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
        leaderboardSettingsPanel.selectDetailsAndDeselectOther(overallDetailsToSelect);

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

    /**
     * Verifies the settings storage of the leaderboard. Checks the precedences of url, context specific settings, and
     * global settings. Verifies also the {@code ignoreLocalSettings} flag for correctness which is used to deactivate
     * settings storage support when it is set to {@code true}.
     */
    @Test
    public void testLeaderboardPageSettingsStorage() {

        // create event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false);

        // open leaderboard
        LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        LeaderboardEntryPO leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        LeaderboardUrlConfigurationDialogPO urlConfigurationDialog = leaderboardEntry
                .getLeaderboardPageUrlConfigurationDialog();
        String bmwCupDefaultLeaderboardLink = urlConfigurationDialog.getLeaderboardLink();
        LeaderboardPage leaderboardPage = urlConfigurationDialog.openLeaderboard();

        // open settings dialog of configurated leaderboard with default values
        LeaderboardSettingsDialogPO leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        LeaderboardSettingsPanelPO leaderboardSettingsPanel = leaderboardSettingsDialog
                .getLeaderboardSettingsPanelPO();

        // store default values for checks later
        DetailCheckboxInfo[] defaultDetails = leaderboardSettingsPanel.getSelectedDetails();
        int defaultRefreshInterval = leaderboardSettingsPanel.getRefreshInterval();

        // modify leaderboard settings
        DetailCheckboxInfo[] newDetails = new DetailCheckboxInfo[] {
                // Overall details
                DetailCheckboxInfo.TOTAL_TIME, DetailCheckboxInfo.MAXIMUM_SPEED_OVER_GROUND,

                // Race details
                DetailCheckboxInfo.RACE_GAP_TO_LEADER, DetailCheckboxInfo.RACE_DISTANCE,

                // Race Start Analysis
                DetailCheckboxInfo.RACE_SPEED_OVER_GROUND_FIVE_SECONDS_BEFORE_START,
                DetailCheckboxInfo.DISTANCE_TO_START_AT_RACE_START,

                // Leg Details
                DetailCheckboxInfo.AVERAGE_SIGNED_CROSS_TRACK_ERROR, DetailCheckboxInfo.RANK_GAIN,

                // Maneuvers
                DetailCheckboxInfo.AVERAGE_MANEUVER_LOSS

        };
        leaderboardSettingsPanel.selectDetailsAndDeselectOther(newDetails);
        int newRefreshInterval = 2;
        leaderboardSettingsPanel.setRefreshInterval(newRefreshInterval);

        // save new settings with ok (context specific storage)
        leaderboardSettingsDialog.pressOk();

        // reload leaderboard and ensure new settings were stored
        leaderboardPage = LeaderboardPage.goToPage(getWebDriver(), bmwCupDefaultLeaderboardLink);
        leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();

        Assert.assertArrayEquals(newDetails, leaderboardSettingsPanel.getSelectedDetails());
        Assert.assertEquals(newRefreshInterval, leaderboardSettingsPanel.getRefreshInterval());

        // ensure that url has priority
        adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(BMW_CUP_REGATTA);
        urlConfigurationDialog = leaderboardEntry.getLeaderboardPageUrlConfigurationDialog();
        leaderboardSettingsPanel = urlConfigurationDialog.goToLeaderboardSettings();
        leaderboardSettingsPanel.setRefreshInterval(4);
        leaderboardPage = urlConfigurationDialog.openLeaderboard();
        leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        Assert.assertEquals(4, leaderboardSettingsPanel.getRefreshInterval());

        // ensure that context specific settings are stored only for the context
        adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(AUDI_CUP_EVENT, AUDI_CUP_EVENTS_DESC,
                AUDI_VENUE, AUDI_START_EVENT_TIME, AUDI_STOP_EVENT_TIME, true, AUDI_CUP_REGATTA, AUDI_CUP_BOAT_CLASS,
                AUDI_START_EVENT_TIME, AUDI_STOP_EVENT_TIME, false);
        leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardEntry = leaderboardConfiguration.getLeaderboardTable().getEntry(AUDI_CUP_REGATTA);
        urlConfigurationDialog = leaderboardEntry.getLeaderboardPageUrlConfigurationDialog();
        String audiCupDefaultLeaderboardLink = urlConfigurationDialog.getLeaderboardLink();
        leaderboardPage = urlConfigurationDialog.openLeaderboard();
        leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        Assert.assertArrayEquals(defaultDetails, leaderboardSettingsPanel.getSelectedDetails());
        Assert.assertEquals(defaultRefreshInterval, leaderboardSettingsPanel.getRefreshInterval());

        // set old default values to default again and check that the context values of other leaderboard have priority
        leaderboardSettingsDialog.pressMakeDefault();
        leaderboardPage = LeaderboardPage.goToPage(getWebDriver(), bmwCupDefaultLeaderboardLink);
        leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        Assert.assertArrayEquals(newDetails, leaderboardSettingsPanel.getSelectedDetails());
        Assert.assertEquals(newRefreshInterval, leaderboardSettingsPanel.getRefreshInterval());

        // set new values to default and check that the other leaderboard which does not have context specific settings
        // stored, applies that values
        leaderboardSettingsDialog.pressMakeDefault();
        leaderboardPage = LeaderboardPage.goToPage(getWebDriver(), audiCupDefaultLeaderboardLink);
        leaderboardSettingsDialog = leaderboardPage.getLeaderboardSettings();
        leaderboardSettingsPanel = leaderboardSettingsDialog.getLeaderboardSettingsPanelPO();
        Assert.assertArrayEquals(newDetails, leaderboardSettingsPanel.getSelectedDetails());
        Assert.assertEquals(newRefreshInterval, leaderboardSettingsPanel.getRefreshInterval());
    }

    /**
     * Verifies settings storage support for regatta overview. Checks the precedences of context specific and global
     * settings. Verifies also the {@code ignoreLocalSettings} flag for correctness which is used to deactivate settings
     * storage support when it is set to {@code true}.
     */
    @Test
    public void testRegattaOverviewSettingsHandling() {
        // create event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false, CUSTOM_COURSE_AREA);

        // add a second regatta to the event and link its leaderboard to the event leaderboard
        RegattaStructureManagementPanelPO regattas = adminConsole.goToRegattaStructure();
        regattas.createRegattaAndAddToEvent(new RegattaDescriptor(AUDI_CUP_REGATTA, AUDI_CUP_BOAT_CLASS), BMW_CUP_EVENT,
                CUSTOM_COURSE_AREA);

        // open regatta overview for the created event
        events = adminConsole.goToEvents();
        RegattaOverviewPage regattaOverviewPage = events.goToRegattaOverviewOfEvent(BMW_CUP_EVENT);
        RegattaOverviewSettingsDialogPO regattaOverviewSettingsDialog = regattaOverviewPage
                .getRegattaOverviewSettingsDialog();

        // assert state
        String regattaOverviewUrl = regattaOverviewPage.getCurrentUrl();
        Assert.assertTrue(regattaOverviewUrl.contains(URL_PARAMETER_IGNORE_LOCAL_SETTINGS));
        Assert.assertFalse(regattaOverviewSettingsDialog.isMakeDefaultButtonVisible());
        // per default all course areas and regatta names are selected => selectedValues equals availableValues
        List<String> initiallySelectedCourseAreas = regattaOverviewSettingsDialog.getSelectedCourseAreas();
        List<String> initiallySelectedRegattaNames = regattaOverviewSettingsDialog.getSelectedRegattaNames();
        Assert.assertEquals(2, initiallySelectedCourseAreas.size());
        Assert.assertEquals(2, initiallySelectedRegattaNames.size());

        // save initial state
        boolean initialShowOnlyCurrentlyRunningRaces = regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces();
        boolean initialShowOnlyRacesOfSameDay = regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay();

        // apply new state
        List<String> newSelectedCourseAreas = initiallySelectedCourseAreas.subList(1, 2);
        List<String> newSelectedRegattaNames = initiallySelectedRegattaNames.subList(0, 1);
        regattaOverviewSettingsDialog.selectCourseAreasAndDeselectOther(newSelectedCourseAreas);
        regattaOverviewSettingsDialog.selectRegattaNamesAndDeselectOther(newSelectedRegattaNames);
        regattaOverviewSettingsDialog.setShowOnlyCurrentlyRunningRaces(!initialShowOnlyCurrentlyRunningRaces);
        regattaOverviewSettingsDialog.setShowOnlyRacesOfSameDay(!initialShowOnlyRacesOfSameDay);
        regattaOverviewSettingsDialog.pressOk();

        // ensure the regatta overview has still the initial state after reload due to ignoreLocalSettings flag
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(), regattaOverviewUrl);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        Assert.assertEquals(initiallySelectedCourseAreas, regattaOverviewSettingsDialog.getSelectedCourseAreas());
        Assert.assertEquals(initiallySelectedRegattaNames, regattaOverviewSettingsDialog.getSelectedRegattaNames());
        Assert.assertEquals(initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

        // verify settings storage support without ignoreLocalSettingsParameter
        String regattaOverviewUrlWithoutIgnoreLocalSettings = regattaOverviewUrl
                .replace(URL_PARAMETER_IGNORE_LOCAL_SETTINGS, "");
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(),
                regattaOverviewUrlWithoutIgnoreLocalSettings);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        Assert.assertTrue(regattaOverviewSettingsDialog.isMakeDefaultButtonVisible());
        regattaOverviewSettingsDialog.selectCourseAreasAndDeselectOther(newSelectedCourseAreas);
        regattaOverviewSettingsDialog.selectRegattaNamesAndDeselectOther(newSelectedRegattaNames);
        regattaOverviewSettingsDialog.setShowOnlyCurrentlyRunningRaces(!initialShowOnlyCurrentlyRunningRaces);
        regattaOverviewSettingsDialog.setShowOnlyRacesOfSameDay(!initialShowOnlyRacesOfSameDay);
        regattaOverviewSettingsDialog.pressOk();

        // reload and verify the regatta overview has previous values set
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(),
                regattaOverviewUrlWithoutIgnoreLocalSettings);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        Assert.assertEquals(newSelectedCourseAreas, regattaOverviewSettingsDialog.getSelectedCourseAreas());
        Assert.assertEquals(newSelectedRegattaNames, regattaOverviewSettingsDialog.getSelectedRegattaNames());
        Assert.assertEquals(!initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(!initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

        // create second event
        adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(AUDI_CUP_EVENT, AUDI_CUP_EVENTS_DESC,
                AUDI_VENUE, AUDI_START_EVENT_TIME, AUDI_STOP_EVENT_TIME, true, AUDI_CUP_REGATTA, AUDI_CUP_BOAT_CLASS,
                AUDI_START_EVENT_TIME, AUDI_STOP_EVENT_TIME, false, CUSTOM_COURSE_AREA);

        // add a second regatta to the event and link its leaderboard to the event leaderboard
        regattas = adminConsole.goToRegattaStructure();
        regattas.createRegattaAndAddToEvent(new RegattaDescriptor(BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS), AUDI_CUP_EVENT,
                CUSTOM_COURSE_AREA);

        // open regatta overview without ignoreLocalSettings flag for the recently created event
        events = adminConsole.goToEvents();
        String audiCupRegattaOverviewUrl = events.getRegattaOverviewUrlOfEvent(AUDI_CUP_EVENT);
        String audiCupRegattaOverviewUrlWithoutIgnoreLocalSettings = audiCupRegattaOverviewUrl
                .replace(URL_PARAMETER_IGNORE_LOCAL_SETTINGS, "");
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(),
                audiCupRegattaOverviewUrlWithoutIgnoreLocalSettings);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();

        // verify the initial values are untouched
        List<String> initiallySelectedCourseAreasForAudi = regattaOverviewSettingsDialog.getSelectedCourseAreas();
        List<String> initiallySelectedRegattaNamesForAudi = regattaOverviewSettingsDialog.getSelectedRegattaNames();
        Assert.assertEquals(2, initiallySelectedCourseAreasForAudi.size());
        Assert.assertEquals(2, initiallySelectedRegattaNamesForAudi.size());
        Assert.assertEquals(initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

        // apply new state
        List<String> newSelectedCourseAreasForAudi = initiallySelectedCourseAreasForAudi.subList(0, 1);
        List<String> newSelectedRegattaNamesForAudi = initiallySelectedRegattaNamesForAudi.subList(1, 2);
        regattaOverviewSettingsDialog.selectCourseAreasAndDeselectOther(newSelectedCourseAreasForAudi);
        regattaOverviewSettingsDialog.selectRegattaNamesAndDeselectOther(newSelectedRegattaNamesForAudi);
        regattaOverviewSettingsDialog.setShowOnlyCurrentlyRunningRaces(!initialShowOnlyCurrentlyRunningRaces);
        regattaOverviewSettingsDialog.setShowOnlyRacesOfSameDay(initialShowOnlyRacesOfSameDay);
        regattaOverviewSettingsDialog.pressMakeDefault();

        // reload first event and verify the regatta overview has previous values set
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(),
                regattaOverviewUrlWithoutIgnoreLocalSettings);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        Assert.assertEquals(newSelectedCourseAreas, regattaOverviewSettingsDialog.getSelectedCourseAreas());
        Assert.assertEquals(newSelectedRegattaNames, regattaOverviewSettingsDialog.getSelectedRegattaNames());
        Assert.assertEquals(!initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(!initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

        // verify global settings with make default button
        regattaOverviewSettingsDialog.pressMakeDefault();
        // open second event again and verify that the global settings of the first event have applied
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(),
                audiCupRegattaOverviewUrlWithoutIgnoreLocalSettings);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        // context specific settings must stay initial
        Assert.assertEquals(2, regattaOverviewSettingsDialog.getSelectedCourseAreas().size());
        Assert.assertEquals(2, regattaOverviewSettingsDialog.getSelectedRegattaNames().size());
        // global settings must match with the settings of the first event
        Assert.assertEquals(!initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(!initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

        // verify that the stored values are ignored with ignoreLocalSettings flag set
        regattaOverviewPage = RegattaOverviewPage.goToPage(getWebDriver(), audiCupRegattaOverviewUrl);
        regattaOverviewSettingsDialog = regattaOverviewPage.getRegattaOverviewSettingsDialog();
        Assert.assertEquals(initiallySelectedCourseAreasForAudi,
                regattaOverviewSettingsDialog.getSelectedCourseAreas());
        Assert.assertEquals(initiallySelectedRegattaNamesForAudi,
                regattaOverviewSettingsDialog.getSelectedRegattaNames());
        Assert.assertEquals(initialShowOnlyCurrentlyRunningRaces,
                regattaOverviewSettingsDialog.isShowOnlyCurrentlyRunningRaces());
        Assert.assertEquals(initialShowOnlyRacesOfSameDay, regattaOverviewSettingsDialog.isShowOnlyRacesOfSameDay());

    }

}
