package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorTablePO.CompetitorEntry;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorsPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestCompetitorCreation extends AbstractSeleniumTest {
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testOpenCreateCompetitorDialog() {
        final TrackedRacesCompetitorsPanelPO competitorsPanel = goToCompetitorsPanel();
        final TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddCompetitorButton(); // fails with an exception if the dialog is not found
        assertNotNull(dialog);
    }

    private TrackedRacesCompetitorsPanelPO goToCompetitorsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final TrackedRacesCompetitorsPanelPO competitorsPanel = adminConsole.goToTrackedRacesCompetitors();
        return competitorsPanel;
    }
    
    @Test
    public void testCompetitorCreation() {
        final TrackedRacesCompetitorsPanelPO competitorsPanel = goToCompetitorsPanel();
        final TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddCompetitorButton();
        final String name = ""+System.currentTimeMillis();
        final String shortName = "NRV";
        dialog.setNameTextBox(name);
        dialog.setShortNameTextBox(shortName);
        dialog.pressOk();
        boolean found = false;
        for (final CompetitorEntry it : competitorsPanel.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if (itName.equals(name)) {
                found = true;
                // found a candidate:
                assertEquals(shortName, it.getShortName());
            }
        }
        assertTrue(found);
    }

}
