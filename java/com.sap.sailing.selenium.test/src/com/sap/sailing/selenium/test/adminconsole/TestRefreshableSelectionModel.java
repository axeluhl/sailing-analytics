package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.core.WebDriverWindow;
import com.sap.sailing.selenium.core.WindowManager;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorTablePO.CompetitorEntry;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestRefreshableSelectionModel extends AbstractSeleniumTest {
    private CompetitorEntry competitorEntry;
    private CompetitorEntry competitorEntryToSelect;
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    private TrackedRacesCompetitorsPanelPO goToCompetitorsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final TrackedRacesCompetitorsPanelPO competitorsPanel = adminConsole.goToTrackedRacesCompetitors();
        return competitorsPanel;
    }
    
    @Test
    public void testMaintenanceOfSelectionAfterDataChanges() {
        final TrackedRacesCompetitorsPanelPO competitorsPanel = goToCompetitorsPanel();

        for (int i = 0; i < 2; i++) {
            TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddButton();
            dialog.setNameTextBox(""+System.currentTimeMillis());
            dialog.setSailIdTextBox(""+System.currentTimeMillis());
            dialog.setBoatClassNameSuggestBox("RS-X");
            dialog.pressOk();
        }
        
        TrackedRacesCompetitorEditDialogPO dialog = competitorsPanel.pushAddButton();
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
                competitorEntry = it;
            }
        }
        assertTrue(found);
        
        // Open a second window & setup second window
        WindowManager manager = this.environment.getWindowManager();
        WebDriverWindow windowForEdit = manager.getCurrentWindow();
        WebDriverWindow windowForSelection = manager.openNewWindow();
        windowForSelection.switchToWindow();
        TrackedRacesCompetitorsPanelPO competitorPanelForSelection = goToCompetitorsPanel();
        found = false;
        for (final CompetitorEntry it : competitorPanelForSelection.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if (itName.equals(name)) {
                found = true;
                // found a candidate:
                assertEquals(sailId, it.getSailId());
                assertEquals(boatClassName, it.getBoatClassName());
                competitorEntryToSelect = it;
            }
        }
        assertTrue(found);
        competitorPanelForSelection.getCompetitorTable().selectEntry(competitorEntryToSelect);

        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().size() == 1);
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().iterator().next().equals(competitorEntryToSelect));
        // change competitor
        windowForEdit.switchToWindow();
        dialog = competitorEntry.clickEditButton();
        final String changedName = ""+System.currentTimeMillis();
        dialog.setNameTextBox(changedName);
        String changedSailId = ""+System.currentTimeMillis();
        dialog.setSailIdTextBox(changedSailId);
        dialog.pressOk();
        
        // assert selection
        windowForSelection.switchToWindow();
        competitorPanelForSelection.pushRefreshButton();
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().size() == 0);
        for (final CompetitorEntry it : competitorPanelForSelection.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if (itName.equals(changedName)) {
                found = true;
                // found a candidate:
                assertEquals(changedSailId, it.getSailId());
                assertEquals(boatClassName, it.getBoatClassName());
                competitorEntryToSelect = it;
            }
        }
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().iterator().next().equals(competitorEntryToSelect));
    }
}
