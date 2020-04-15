package com.sap.sailing.selenium.test.datamining;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.datamining.DataMiningPage;
import com.sap.sailing.selenium.pages.datamining.DataMiningPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SmokeTestDataminingEntrypoint extends AbstractSeleniumTest {
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testDataminingEntrypoint() {
        final DataMiningPage page = DataMiningPage.goToPage(getWebDriver(), getContextRoot());
        final DataMiningPanelPO dataMiningPanel = page.getDataMiningPanel();
        assertTrue(dataMiningPanel.isRunButtonAvailable());
        assertTrue(dataMiningPanel.isExtractionFunctionSuggestBoxAvailable());
    }

}
