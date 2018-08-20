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

public class SmartphoneTrackingEventManagementPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "AvailableLeaderboardsTable")
    private WebElement leaderboardTable;

    @FindBy(how = BySeleniumId.class, using = "RaceColumnTable")
    private WebElement raceColumnTableWrapper;
 
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListComposite;
    
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
}
