package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesBoatEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesBoatTablePO.BoatEntry;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesBoatsPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestBoatCreation extends AbstractSeleniumTest {
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testOpenCreateBoatDialog() {
        final TrackedRacesBoatsPanelPO competitorsPanel = goToBoatsPanel();
        final TrackedRacesBoatEditDialogPO dialog = competitorsPanel.pushAddButton(); // fails with an exception if the dialog is not found
        assertNotNull(dialog);
    }

    private TrackedRacesBoatsPanelPO goToBoatsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final TrackedRacesBoatsPanelPO boatsPanel = adminConsole.goToTrackedRacesBoats();
        return boatsPanel;
    }
    
    @Test
    public void testBoatCreation() {
        final TrackedRacesBoatsPanelPO boatsPanel = goToBoatsPanel();
        final TrackedRacesBoatEditDialogPO dialog = boatsPanel.pushAddButton();
        final String name = ""+System.currentTimeMillis();
        dialog.setNameTextBox(name);
        String sailId = ""+System.currentTimeMillis();
        dialog.setSailIdTextBox(sailId);
        final String boatClassName = "49er";
        dialog.setBoatClassNameSuggestBox(boatClassName);
        dialog.pressOk();
        boolean found = false;
        for (final BoatEntry it : boatsPanel.getBoatsTable().getEntries()) {
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
