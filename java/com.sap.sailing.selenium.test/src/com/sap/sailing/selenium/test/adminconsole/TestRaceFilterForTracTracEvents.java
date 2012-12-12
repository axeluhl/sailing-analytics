package com.sap.sailing.selenium.test.adminconsole;

import java.io.IOException;

import org.junit.Test;

import static org.hamcrest.core.Is.*;

import static org.junit.Assert.*;

import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.selenium.test.adminconsole.pages.AdminConsolePage;
import com.sap.sailing.selenium.test.adminconsole.pages.TracTracEventManagementPanel;

public class TestRaceFilterForTracTracEvents extends AbstractSeleniumTest {
    protected static final String BMW_CUP_JSON_URL = "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php";
    
    @Test
    public void testNoFilter() throws IOException {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listRaces(BMW_CUP_JSON_URL);
        
        assertThat(tracTracEvents.getTrackableRaces().size(), is(12));
        
    }
    
    @Test
    @SuppressWarnings("boxing")
    public void testPartialFilter() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        
        TracTracEventManagementPanel tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.listRaces(BMW_CUP_JSON_URL);
        tracTracEvents.setFilterForTrackableRaces("BMW Cup Race 1");
        
        assertThat(tracTracEvents.getTrackableRaces().size(), is(5));
    }
    
    @Test
    public void testStratRacking() {
        
    }
}
