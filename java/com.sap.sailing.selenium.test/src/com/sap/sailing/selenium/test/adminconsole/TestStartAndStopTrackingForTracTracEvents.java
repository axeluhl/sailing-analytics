package com.sap.sailing.selenium.test.adminconsole;

import org.junit.Test;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;

import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.TracTracEventManagementPanel;

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
     * <p>Test for start tracking with an incorrect regatta selected, which should lead to an dialog with a warning.</p>
     */
    @Test
    public void testStartTrackingWithoutCorrectRegatta() {
        
    }
    
    /**
     * <p>Test for the correct start and stop of a tracking.</p>
     */
    @Test
    public void testStartAndStopTrackingWithCorrectRegatta() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listRaces(BMW_CUP_JSON_URL);
        
        // TODO: Complete the test case!
        //tracTracEvents.startTracking(BMW_CUP_RACE_8, BMW_CUP_REGATTA, false, false, false);
        
        //assertThat(tracTracEvents.getTrackedRaces(), hasItem(12));
        
        //tracTracEvents.stopTracking("BMW_CUP_RACE_8");
        
        //assertThat(tracTracEvents.getTrackedRaces(), not(hasItem(12)));
    }
}
