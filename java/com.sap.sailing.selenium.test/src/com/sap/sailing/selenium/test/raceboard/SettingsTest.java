package com.sap.sailing.selenium.test.raceboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.advanced.MasterDataImportPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;
import com.sap.sailing.selenium.pages.raceboard.MapSettingsPO;
import com.sap.sailing.selenium.pages.raceboard.RaceBoardPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class SettingsTest extends AbstractSeleniumTest {
    private static final String TRAC_LINK = "http://skitrac.traclive.dk/events/event_20150204_ESSSingapo/jsonservice.php";
    private static final String EVENT_LINK = "gwt/RaceBoard.html?eventId=ed3cf78d-45b4-416c-99a1-3df88608f629&leaderboardName=ESS+2015+Singapore&leaderboardGroupName=Extreme+Sailing+Series+2015&raceName=Race+1&showMapControls=true&viewShowNavigationPanel=true&regattaName=ESS+2015+Singapore&mode=PLAYER";
    private static final String EVENT_NAME = "Extreme Sailing Series 2015";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void createRaceAsAdminSetWindSettingToTrue() throws InterruptedException {
        AdminConsolePage adminpage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        MasterDataImportPO masterDataImportPO = adminpage.goToMasterDateImport();
        masterDataImportPO.fetchLeaderBoards();
        masterDataImportPO.importData(EVENT_NAME, true, false);
        TracTracEventManagementPanelPO tracking = adminpage.goToTracTracEvents();
        tracking.listTrackableRaces(TRAC_LINK);
        tracking.setReggataForTracking("ESS 2015 Singapore");
        tracking.startTrackingForRaces(tracking.getTrackableRaces());
        RaceBoardPage raceboard = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot()+EVENT_LINK);
        MapSettingsPO mapSettings = raceboard.openMapSettings();
        mapSettings.setWindChart(true);
        mapSettings.makeDefault();
        // reload
        RaceBoardPage raceboard2 = RaceBoardPage.goToRaceboardUrl(getWebDriver(), getContextRoot() + EVENT_LINK);
        MapSettingsPO mapSettings2 = raceboard2.openMapSettings();
        boolean stillSelected = mapSettings2.isWindChartSelected();
        Assert.assertTrue(stillSelected);
    }
}
