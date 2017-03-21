package com.sap.sailing.selenium.test.raceboard;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO.TrackableRaceDescriptor;
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
        RegattaDescriptor bmwCupDescriptor = new RegattaDescriptor(BMW_CUP_EVENT, BMW_CUP_BOAT_CLASS);

        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME);

        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
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
}
