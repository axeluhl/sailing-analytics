package com.sap.sailing.selenium.pages.adminconsole.connectors;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.tracking.RaceColumnTableWrapperPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.MapDevicesDialogPO;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.RegisterCompetitorsDialogPO;

public class SmartphoneTrackingEventManagementPanelPO extends PageArea {
    
    private static final String ACTION_COMPETITOR_REGISTRATIONS = "ACTION_COMPETITOR_REGISTRATIONS";
    private static final String ACTION_MAP_DEVICES = "ACTION_MAP_DEVICES";
    
    @FindBy(how = BySeleniumId.class, using = "AvailableLeaderboardsTable")
    private WebElement leaderboardTable;

    @FindBy(how = BySeleniumId.class, using = "RaceColumnTable")
    private WebElement raceColumnTableWrapper;
 
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListComposite;
 
    @FindBy(how = BySeleniumId.class, using = "LeaderboardRefreshButton")
    private WebElement leaderboardRefreshButton;
    
    public SmartphoneTrackingEventManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public CellTablePO<DataEntryPO> getLeaderboardTable() {
        return new GenericCellTablePO<>(this.driver, this.leaderboardTable, DataEntryPO.class);
    }
    
    public RaceColumnTableWrapperPO getRaceColumnTableWrapper() {
        return new RaceColumnTableWrapperPO(this.driver, this.raceColumnTableWrapper);
    }
    
    public TrackedRacesListPO getTrackedRaceListComposite() {
        return new TrackedRacesListPO(this.driver, this.trackedRacesListComposite);
    }
    
    public void refreshLeaderboardTable() {
        leaderboardRefreshButton.click();
        waitForAjaxRequests();
    }
    
    public RegisterCompetitorsDialogPO pushCompetitorRegistrationsActionButton(DataEntryPO aLeaderboard) {
        aLeaderboard.clickActionImage(ACTION_COMPETITOR_REGISTRATIONS);
        return this.waitForPO(RegisterCompetitorsDialogPO::new, "registerCompetitorsDialog");
    }
    
    public MapDevicesDialogPO pushMapDevicesActionButton(DataEntryPO aLeaderboard) {
        aLeaderboard.clickActionImage(ACTION_MAP_DEVICES);
        return this.waitForPO(MapDevicesDialogPO::new, "regattaLogTrackingDeviceMappingsDialog");
    }
}
