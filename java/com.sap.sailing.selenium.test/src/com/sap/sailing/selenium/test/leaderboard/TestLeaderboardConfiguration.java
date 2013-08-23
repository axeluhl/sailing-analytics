package com.sap.sailing.selenium.test.leaderboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;

import com.sap.sailing.selenium.pages.adminconsole.leaderboard.FlexibleLeaderboardCreationDialog;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaCreationDialog;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanel;

import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel.TrackableRaceDescriptor;

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
    
    private static final String REGATTA = "TestLeaderboardConfiguration Regatta";
    
    private static final String LEADERBOARD = "TestLeaderboardConfiguration Leaderboard";
    
    private static final String EVENT = "BMW Cup";
    private static final String RACE = "BMW Cup Race %d";
    private static final String BOAT_CLASS = "J80";
    
    private List<TrackableRaceDescriptor> races;
    
    private RegattaDescriptor regatta;
    
    @Before
    public void setUp() {
        this.races = new ArrayList<>();
        
        for(int i = 1; i <= 5; i++) {
            TrackableRaceDescriptor race = new TrackableRaceDescriptor(EVENT,  String.format(RACE,  i), BOAT_CLASS);
            this.races.add(race);
        }
        
        this.regatta = new RegattaDescriptor(REGATTA, BOAT_CLASS);
        
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
        
        RegattaStructureManagementPanel regattaStructure = adminConsole.goToRegattaStructure();
        regattaStructure.createRegatta(this.regatta);
        
//        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
//        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
//        tracTracEvents.setTrackSettings(false, false, false);
//        tracTracEvents.startTrackingForRaces(this.races);
        
//        FluentWait<TrackedRacesList> wait = new FluentWait<>(null);
//        wait.until(new Function<TrackedRacesList, Object>() {
//            public Object apply(TrackedRacesList list) {
//                list.refresh();
//                
//                return list.getTrackedRaces().size() == races.size();
//            }
//        });
        
//        LeaderboardConfigurationPanel leaderboardConfiguration = adminConsole.goToLeaderboardConfiguration();
//        leaderboardConfiguration.deleteLeaderboard("Test Leaderboard");
//
//        FlexibleLeaderboardCreationDialog dialog = leaderboardConfiguration.startCreatingFlexibleLeaderboard();
//        dialog.setName("Test Leaderboard");
//        dialog.pressOk();
        
    }
}
