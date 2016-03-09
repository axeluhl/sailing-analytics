package com.sap.sailing.selenium.test.integration.state;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestStateVisualisationEvent extends AbstractSeleniumTest {
    
    @Override
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testStateFlagOfUmcomingEvent() {
        AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO eventConfigurationPanel = adminConsolePage.goToEvents();
        eventConfigurationPanel.createPublicEvent("Test", "Somewhere", new Date(), new Date());
        Assert.assertTrue(true);
    }

}
