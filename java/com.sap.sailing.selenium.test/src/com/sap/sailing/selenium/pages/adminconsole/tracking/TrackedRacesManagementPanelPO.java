package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class TrackedRacesManagementPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListCoposite;
    
    public TrackedRacesManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public TrackedRacesListPO getTrackedRacesList() {
        return new TrackedRacesListPO(this.driver, this.trackedRacesListCoposite);
    }
}
