package com.sap.sailing.selenium.test.autoplay;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.autoplay.AutoPlayPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestAutoPlay extends AbstractSeleniumTest {
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testAutoPlayStartup() {
        AutoPlayPage page = AutoPlayPage.goToPage(getWebDriver(), getContextRoot());
        AutoPlayConfiguration autoPlayConfiguration = page.getAutoPlayConfiguration();
        assertNotNull(autoPlayConfiguration);
    }
    
}
