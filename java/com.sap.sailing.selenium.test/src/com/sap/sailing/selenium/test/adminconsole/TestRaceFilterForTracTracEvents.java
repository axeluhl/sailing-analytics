package com.sap.sailing.selenium.test.adminconsole;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

import org.junit.Before;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * <p>Tests for filtering of trackable TracTrac races.</p>
 * 
 * @author
 *   D049941
 */
public class TestRaceFilterForTracTracEvents extends AbstractSeleniumTest {
    private static final String BMW_CUP_JSON_URL =
            "http://kml.skitrac.traclive.dk/events/event_20120803_BMWCup/jsonservice.php"; //$NON-NLS-1$
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    /**
     * <p>Test with no filter at all, which means we expect all TracTrac races are displayed for a given event.</p>
     */
    @Test
    public void testNoFilter() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.addConnectionAndListTrackableRaces(BMW_CUP_JSON_URL);
        assertThat(tracTracEvents.getTrackableRaces().size(), is(12));
    }
    
    /**
     * <p>Test with partial filter, which should show multiple races but not all.</p>
     */
    @Test
    public void testPartialFilter() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.addConnectionAndListTrackableRaces(BMW_CUP_JSON_URL);
        tracTracEvents.setFilterForTrackableRaces("BMW Cup Race 1"); //$NON-NLS-1$
        assertThat(tracTracEvents.getTrackableRaces().size(), is(5));
    }
    
    /**
     * <p>Test with exact filter which should match only 1 races.</p>
     */
    @Test
    public void testExactFilter() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.addConnectionAndListTrackableRaces(BMW_CUP_JSON_URL);
        tracTracEvents.setFilterForTrackableRaces("BMW Cup Race 12"); //$NON-NLS-1$
        assertThat(tracTracEvents.getTrackableRaces().size(), is(1));
    }
    
    /**
     * <p>Test with filter which does not match anything.</p>
     */
    @Test
    public void testNoneMatchingFilter() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TracTracEventManagementPanelPO tracTracEvents = adminConsole.goToTracTracEvents();
        tracTracEvents.addConnectionAndListTrackableRaces(BMW_CUP_JSON_URL);
        tracTracEvents.setFilterForTrackableRaces("BMW Cup Race 18"); //$NON-NLS-1$
        assertThat(tracTracEvents.getTrackableRaces().size(), is(0));
    }
}
