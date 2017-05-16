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
        final TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddButton(); // fails with an exception if the dialog is not found
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
        final TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddButton();
        final String name = ""+System.currentTimeMillis();
        dialog.setNameTextBox(name);
        String sailId = ""+System.currentTimeMillis();
        dialog.setSailIdTextBox(sailId);
        final String boatClassName = "Laser Int.";
        dialog.setBoatClassNameSuggestBox(boatClassName);
        dialog.pressOk();
        boolean found = false;
        for (final CompetitorEntry it : competitorsPanel.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if (itName.equals(name)) {
                found = true;
                // found a candidate:
                assertEquals(sailId, it.getSailId());
                assertEquals(boatClassName, it.getBoatClassName());
            }
        }
        assertTrue(found);
    }
}
