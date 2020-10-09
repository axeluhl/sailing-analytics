package com.sap.sailing.selenium.test.adminconsole;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.connectors.ResultImportUrlsPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestConnectorsResultImportUrls extends AbstractSeleniumTest {

    private final static String TEST_URL = "http://test.test.de";
    private final static String TEST_URL_PROVIDER_LABEL = "FREG HTML Score Importer";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    private ResultImportUrlsPanelPO goToResultImportUrlsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final ResultImportUrlsPanelPO resultImportUrlsPanel = adminConsole.goToResultImportUrlsPanel();
        return resultImportUrlsPanel;
    }

    @Test
    public void testUrlCreationAndDeletion() {
        final ResultImportUrlsPanelPO resultImportUrlsPanel = goToResultImportUrlsPanel();
        resultImportUrlsPanel.selectUrlProviderByLabel(TEST_URL_PROVIDER_LABEL);
        // add
        resultImportUrlsPanel.addUrl(TEST_URL);
        resultImportUrlsPanel.removeUrl(TEST_URL);
        
    }

    @Test
    public void testUrlInlineDeletion() {
        final ResultImportUrlsPanelPO resultImportUrlsPanel = goToResultImportUrlsPanel();
        resultImportUrlsPanel.selectUrlProviderByLabel(TEST_URL_PROVIDER_LABEL);
        // add
        resultImportUrlsPanel.addUrl(TEST_URL);
        resultImportUrlsPanel.removeWithInlineButton(TEST_URL);
    }
}
