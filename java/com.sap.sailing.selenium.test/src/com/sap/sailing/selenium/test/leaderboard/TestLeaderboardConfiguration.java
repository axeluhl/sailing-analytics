package com.sap.sailing.selenium.test.leaderboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;

import com.sap.sailing.selenium.core.WebDriverWindow;
import com.sap.sailing.selenium.core.WindowManager;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;

import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreationDialog;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaCreationDialog;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanel;

import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList.Status;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel.TrackableRaceDescriptor;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardPage;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * <p>Tests for creation of leader boards.</p>
 * 
 * @author
 *   D049941
 */
public class TestLeaderboardConfiguration extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL =
            "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    
    private static final String REGATTA = "TestLeaderboardConfiguration Regatta"; //$NON-NLS-1$
    
    private static final String LEADERBOARD = "TestLeaderboardConfiguration Leaderboard"; //$NON-NLS-1$
    
    private static final String EVENT = "BMW Cup"; //$NON-NLS-1$
    private static final String RACE = "BMW Cup Race %d"; //$NON-NLS-1$
    private static final String BOAT_CLASS = "J80"; //$NON-NLS-1$
    
    private RegattaDescriptor regatta;

    //private LeaderboardDescriptor leaderboard;

    private List<TrackableRaceDescriptor> trackableRaces;
    private List<TrackedRaceDescriptor> trackedRaces;
    
    
    
    @Before
    public void setUp() {
        this.trackableRaces = new ArrayList<>();
        this.trackedRaces = new ArrayList<>();
        this.regatta = new RegattaDescriptor(REGATTA, BOAT_CLASS);
        
        for(int i = 1; i <= 5; i++) {
            TrackableRaceDescriptor trackableRace = new TrackableRaceDescriptor(EVENT,  String.format(RACE,  i), BOAT_CLASS);
            TrackedRaceDescriptor trackedRace = new TrackedRaceDescriptor(this.regatta.toString(), BOAT_CLASS, String.format(RACE,  i));
            
            this.trackableRaces.add(trackableRace);
            this.trackedRaces.add(trackedRace);
        }
        
        
        // TODO: This should not be done via the UI
//        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
//        
//        RegattaStructureManagementPanel regattaPanel = adminConsole.goToRegattaStructure();
//        regattaPanel.removeRegatta(this.regatta);
        
        //LeaderboardConfigurationPanel leaderboardPanel = adminConsole.goToLeaderboardConfiguration();
        //leaderboardPanel.deleteLeaderboard(leaderboard)
    }
    
    @Test
    public void testDynamicLeaderboardConfiguration() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        // Create a regatta and a leaderborad
        RegattaStructureManagementPanel regattaStructure = adminConsole.goToRegattaStructure();
        regattaStructure.createRegatta(this.regatta);
        
        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
        leaderboardConfiguration.createRegattaLeaderboard(this.regatta);
        
        // Open the leaderboard in a new window and check for empty leaderboard
        //String url = leaderboardConfiguration.getLeaderboardURL(LEADERBOARD);
        WindowManager manager = this.environment.getWindowManager();
        WebDriverWindow adminConsoleWindow = manager.getCurrentWindow();
        WebDriverWindow leaderboardWindow = manager.openNewWindow();
        
        // TODO: Check for empty leaderboard
        LeaderboardPage leaderboard = LeaderboardPage.goToPage(getWebDriver(), getContextRoot(), this.regatta.toString());
        
        
        adminConsoleWindow.switchToWindow();
        // Start the tracking for some races and wait until they are ready to use
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
        tracTracEvents.setTrackSettings(false, false, false);
        tracTracEvents.startTrackingForRaces(this.trackableRaces);
        
        TrackedRacesManagementPanel trackedRaces = adminConsole.goToTrackedRaces();
        
        FluentWait<TrackedRacesList> wait = new FluentWait<>(trackedRaces.getTrackedRacesList());
        wait.pollingEvery(5, TimeUnit.SECONDS);
        wait.withTimeout(30, TimeUnit.SECONDS);
        wait.until(new Function<TrackedRacesList, Object>() {
            @Override
            public Object apply(TrackedRacesList list) {
                list.refresh();
                
                for(Status status : list.getStatus(TestLeaderboardConfiguration.this.trackedRaces)) {
                    if(status != Status.TRACKING)
                        return Boolean.FALSE;
                }
                
                return Boolean.TRUE;
            }
        });
        
        // TODO: Link the races and check the leaderboard
        
    }
}
