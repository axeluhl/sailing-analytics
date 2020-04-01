package com.sap.sailing.selenium.test.raceboard;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.igtimi.IgtimiAccountsManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO.RaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.SeriesEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.wind.WindPanelPO;
import com.sap.sailing.selenium.pages.raceboard.MapSettingsPO;
import com.sap.sailing.selenium.pages.raceboard.RaceBoardPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SimulatorOverlayTest extends AbstractSeleniumTest {
    private static final String JSON_URL = "http://event.tractrac.com/events/event_20150616_KielerWoch/jsonservice.php"; //$NON-NLS-1$
    private static final String EVENT = "Kieler Woche 2015"; //$NON-NLS-1$
    private static final String BOAT_CLASS_49ER = "49er"; //$NON-NLS-1$
    private static final String REGATTA_49ER = "KW 2015 Olympic - 49er"; //$NON-NLS-1$
    private static final String REGATTA_49ER_WITH_SUFFIX = REGATTA_49ER + " ("+BOAT_CLASS_49ER+")"; //$NON-NLS-1$
    private static final String RACE_N_49ER = "R%d (49er)"; //$NON-NLS-1$
    private static final String MEDAL_RACE_49ER = "R12-M Medal (49er)"; //$NON-NLS-1$
    private static final String EVENT_DESC = "Kieler Woche 2015"; //$NON-NLS-1$
    private static final String VENUE = "Kieler F�rde"; //$NON-NLS-1$
    private static final Date EVENT_START_TIME = DatatypeConverter.parseDateTime("2015-06-20T08:00:00-00:00")
            .getTime();
    private static final Date EVENT_END_TIME = DatatypeConverter.parseDateTime("2015-06-28T20:00:00-00:00")
            .getTime();
    private static final String DEFAULT_FLEET = "Default";
    private static final String SERIES_DEFAULT = "Default";
    private static final String SERIES_QUALIFICATION = "Qualification";
    private static final String SERIES_MEDALS = "Medals";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    /**
     * The simulator overlay option in RaceBoard is only available if polar data is available for the specific boat
     * class. This test verifies that the checkbox isn't available for a race when wind data isn't loaded yet but
     * appears after successfully loading wind.
     */
    @Test
    public void testSimulatorOverlayIsAvailableFor49erAtKW2015() throws InterruptedException, UnsupportedEncodingException {
        final RegattaDescriptor regattaDescriptor = new RegattaDescriptor(REGATTA_49ER, BOAT_CLASS_49ER);
        {
            final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
            final EventConfigurationPanelPO events = adminConsole.goToEvents();
            events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(EVENT, EVENT_DESC,
                    VENUE, EVENT_START_TIME, EVENT_END_TIME, true, REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER,
                    EVENT_START_TIME, EVENT_END_TIME, false);
            final RegattaStructureManagementPanelPO regattaStructurePanel = adminConsole.goToRegattaStructure();
            final RegattaDetailsCompositePO regattaDetails = regattaStructurePanel.getRegattaDetails(regattaDescriptor);
            regattaDetails.deleteSeries(SERIES_DEFAULT);
            RegattaEditDialogPO editRegatta = regattaStructurePanel.getRegattaList().editRegatta(regattaDescriptor);
            editRegatta.addSeries(SERIES_QUALIFICATION);
            editRegatta.addSeries(SERIES_MEDALS);
            editRegatta.pressOk();
            
            final SeriesEditDialogPO editSeriesQualification = regattaDetails.editSeries(SERIES_QUALIFICATION);
            editSeriesQualification.addRaces(1, 11, "Q");
            editSeriesQualification.pressOk();
            
            final SeriesEditDialogPO editSeriesMedals = regattaDetails.editSeries(SERIES_MEDALS);
            editSeriesMedals.setMedalSeries(true);
            editSeriesMedals.addSingleRace("M");
            editSeriesMedals.pressOk();
    
            trackRacesFor49er(regattaDescriptor, adminConsole.goToTracTracEvents());
    
            final LeaderboardConfigurationPanelPO leaderboard = adminConsole.goToLeaderboardConfiguration();
            final LeaderboardDetailsPanelPO details = leaderboard.getLeaderboardDetails(REGATTA_49ER_WITH_SUFFIX);
            
            for(int i = 1; i<=11; i++) {
                details.linkRace(new RaceDescriptor("Q" + i, DEFAULT_FLEET, false, false, 0), new TrackedRaceDescriptor(REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, String.format(RACE_N_49ER, i)));
            }
            details.linkRace(new RaceDescriptor("M", DEFAULT_FLEET, true, false, 0), new TrackedRaceDescriptor(REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, MEDAL_RACE_49ER));
        }
        {
            RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), REGATTA_49ER_WITH_SUFFIX,
                    REGATTA_49ER_WITH_SUFFIX, String.format(RACE_N_49ER, 1));
            MapSettingsPO mapSettings = raceboard.openMapSettings();
            // Simulator overlay option must not be available without wind data
            Assert.assertFalse(mapSettings.isShowSimulationOverlayCheckBoxVisible());
        }
        {
            final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
            IgtimiAccountsManagementPanelPO igtimiAccountsManagementPanel = adminConsole.goToIgtimi();
            igtimiAccountsManagementPanel.addAccount(getIgtimiAccountUser(), getIgtimiAccountPassword());
            WindPanelPO windPanel = adminConsole.goToWind();
            windPanel.importWindFromIgtimi(/* waiting up to 10 min */ 10 * 60);
        }
        {
            RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot(), REGATTA_49ER_WITH_SUFFIX,
                    REGATTA_49ER_WITH_SUFFIX, String.format(RACE_N_49ER, 1));
            MapSettingsPO mapSettings = raceboard.openMapSettings();
            // Simulator overlay option must be available with the wind data being available
            Assert.assertTrue(mapSettings.isShowSimulationOverlayCheckBoxVisible());
        }
    }

    private void trackRacesFor49er(final RegattaDescriptor regattaDescriptor, final TracTracEventManagementPanelPO tracTracEvents) {
        tracTracEvents.addConnectionAndListTrackableRaces(JSON_URL);
        tracTracEvents.setReggataForTracking(regattaDescriptor);
        tracTracEvents.setTrackSettings(true, false, false);
        tracTracEvents.setFilterForTrackableRaces("(49er)");
        // races are filtered so that all shown entries belong to the correct regatta
        tracTracEvents.startTrackingForAllRaces();
        
        final TrackedRacesListPO trackedRacesList = tracTracEvents.getTrackedRacesList();
        final List<TrackedRaceDescriptor> racesToWaitLoadingFor = new ArrayList<>();
        for(int i = 1; i<=11; i++) {
            racesToWaitLoadingFor.add(new TrackedRaceDescriptor(REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, String.format(RACE_N_49ER, i)));
        }
        racesToWaitLoadingFor.add(new TrackedRaceDescriptor(REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, MEDAL_RACE_49ER));
        trackedRacesList.waitForTrackedRaces(racesToWaitLoadingFor, Status.FINISHED, 600);
    }
    
    private String getIgtimiAccountUser() {
        return decodeBase64("c2FpbGluZ19hbmFseXRpY3NAc2FwLmNvbQ==");
    }
    
    private String getIgtimiAccountPassword() {
        return decodeBase64("MTIzNDU2");
    }

    private String decodeBase64(String base64EncodedString) {
        return new String(Base64.getDecoder().decode(base64EncodedString.getBytes(StandardCharsets.US_ASCII)), StandardCharsets.UTF_8);
    }
}
