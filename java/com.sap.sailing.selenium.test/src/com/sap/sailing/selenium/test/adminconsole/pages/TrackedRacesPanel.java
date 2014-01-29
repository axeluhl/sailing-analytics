package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;
import com.sap.sailing.selenium.test.gwt.widgets.CellTable;

public class TrackedRacesPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesFilter")
    private WebElement filterTestBox;

    @FindBy(how = BySeleniumId.class, using = "Refresh")
    private WebElement refreshButton;

    @FindBy(how = BySeleniumId.class, using = "RemoveRace")
    private WebElement removeButton;

    @FindBy(how = BySeleniumId.class, using = "UntrackRace")
    private WebElement stopTrackingButton;
    
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesTable")
    private WebElement trackedRacesTable;
    
    public TrackedRacesPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public Iterable<WebElement> getTrackedRaces() {
        return new CellTable(driver, trackedRacesTable).getRows();
    }
    
    public WebElement getTrackedRace(String regattaName, String raceName) {
        for (WebElement raceRow : getTrackedRaces()) {
            List<WebElement> fields = raceRow.findElements(By.tagName("td"));
            if (regattaName.equals(fields.get(0).getText()) && raceName.equals(fields.get(2).getText())) {
                return raceRow;
            }
        }
        return null;
    }

    public void refresh() {
        refreshButton.click();
    }
    
    public void remove() {
        removeButton.click();
    }
    
    public void stopTracking() {
        stopTrackingButton.click();
    }
}
