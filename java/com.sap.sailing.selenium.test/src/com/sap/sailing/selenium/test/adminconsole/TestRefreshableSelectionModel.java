package com.sap.sailing.selenium.test.adminconsole;

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
        final String sailId = ""+System.currentTimeMillis();
        dialog.setSailIdTextBox(sailId);
        final String boatClassName = "Laser Int.";
        dialog.setBoatClassNameSuggestBox(boatClassName);
        dialog.pressOk(); 
        
        boolean found = false;
        for (final CompetitorEntry it : competitorsPanel.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if (itName.equals(name)) {
                found = true;
                competitorEntry = it;
                break;
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
                competitorEntryToSelect = it;
                break;
            }
        }
        assertTrue(found);
        competitorPanelForSelection.getCompetitorTable().selectEntry(competitorEntryToSelect);

        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().size() == 1);
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getName().equals(competitorEntryToSelect.getName()));
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getSailId().equals(competitorEntryToSelect.getSailId()));
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getBoatClassName().equals(competitorEntryToSelect.getBoatClassName()));
        // change competitor
        windowForEdit.switchToWindow();
        dialog = competitorEntry.clickEditButton();
        final String changedName = ""+System.currentTimeMillis();
        dialog.setNameTextBox(changedName);
        final String changedSailId = ""+System.currentTimeMillis();
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
                competitorEntryToSelect = it;
                break;
            }
        }
        assertTrue(found);
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getName().equals(competitorEntryToSelect.getName()));
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getSailId().equals(competitorEntryToSelect.getSailId()));
        assertTrue(competitorPanelForSelection.getCompetitorTable().getSelectedEntries().get(0).getBoatClassName().equals(competitorEntryToSelect.getBoatClassName()));
    }
}
