package com.sap.sailing.selenium.test.adminconsole;

import org.junit.Test;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;

import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.TracTracEventManagementPanel;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import static org.junit.Assert.assertThat;

import static org.junit.matchers.JUnitMatchers.hasItem;

public class TestStartAndStopTrackingForTracTracEvents extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php";
    private static final String NO_REGATTA = "No regatta";
    private static final String BMW_CUP_REGATTA = "BMW Cup (J80)";
    private static final String BMW_CUP_RACE_8 = "BMW Cup Race 8";
    
    @Test
    public void testStartTrackingWithoutCorrectRegatta() {
        
    }
    
    @Test
    public void testStartAndStopTrackingWithCorrectRegatta() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listRaces(BMW_CUP_JSON_URL);
        //tracTracEvents.startTracking(BMW_CUP_RACE_8, BMW_CUP_REGATTA, false, false, false);
        
        //assertThat(tracTracEvents.getTrackedRaces(), hasItem(12));
        
        //tracTracEvents.stopTracking("BMW_CUP_RACE_8");
        
        //assertThat(tracTracEvents.getTrackedRaces(), not(hasItem(12)));
    }
}
