package com.sap.sailing.selenium.test.adminconsole;

import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * <p>Test for starting and stopping the tracking of TracTrac races.</p>
 * 
 * @author
 *   D049941
 */
public class TestStartAndStopTrackingForTracTracEvents extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$

    // TODO see below: Complete the test case
//    private static final String NO_REGATTA = "No regatta";
//    private static final String BMW_CUP_REGATTA = "BMW Cup (J80)";
//    private static final String BMW_CUP_RACE_8 = "BMW Cup Race 8";
    
    
    /**
     * <p>Test for the correct start and stop of a tracking.</p>
     */
    @Test
    public void testStartAndStopTrackingWithCorrectRegatta() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listTrackableRaces(BMW_CUP_JSON_URL);
        
        // TODO: Complete the test case!
        //tracTracEvents.startTracking(BMW_CUP_RACE_8, BMW_CUP_REGATTA, false, false, false);
        
        //assertThat(tracTracEvents.getTrackedRaces(), hasItem(12));
        
        //tracTracEvents.stopTracking("BMW_CUP_RACE_8");
        
        //assertThat(tracTracEvents.getTrackedRaces(), not(hasItem(12)));
    }
}
