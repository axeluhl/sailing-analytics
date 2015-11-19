package com.sap.sailing.selenium.test.adminconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesCompetitorCreateDialogPO;
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
        TrackedRacesCompetitorCreateDialogPO dialog;
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TrackedRacesCompetitorsPanelPO competitorsPanel = adminConsole.goToTrackedRacesCompetitors();
        dialog = competitorsPanel.pushAddButton();
        assertNotNull(dialog);
    }
    
    @Test
    public void testCompetitorCreation() {
        TrackedRacesCompetitorCreateDialogPO dialog;
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        TrackedRacesCompetitorsPanelPO competitorsPanel = adminConsole.goToTrackedRacesCompetitors();
        dialog = competitorsPanel.pushAddButton();
        String name = ""+System.currentTimeMillis();
        dialog.setNameTextBox(name);
        String sailId = ""+System.currentTimeMillis();
        dialog.setSailIdTextBox(sailId);
        dialog.setBoatClassNameSuggestBox("Laser Int.");
        dialog.pressOk();
        String result = null;
        for(CompetitorEntry it : competitorsPanel.getCompetitorTable().getEntries()) {
            String itName = it.getName();
            if(itName.equals(name)) {
                result = it.getName();
            }
        }
        assertEquals(result,name);
    }
}
