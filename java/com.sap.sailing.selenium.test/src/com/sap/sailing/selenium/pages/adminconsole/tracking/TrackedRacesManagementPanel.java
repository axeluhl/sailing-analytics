package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class TrackedRacesManagementPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListCoposite;
    
    public TrackedRacesManagementPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public TrackedRacesList getTrackedRacesList() {
        return new TrackedRacesList(this.driver, this.trackedRacesListCoposite);
    }
}
