package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class TrackedRacesBoatsPanelPO extends PageArea {    
    @FindBy(how = BySeleniumId.class, using = "RefreshButton")
    private WebElement refreshButton;
    
    @FindBy(how = BySeleniumId.class, using = "AddBoatButton")
    private WebElement addButton;
    
    @FindBy(how = BySeleniumId.class, using = "BoatsTable")
    private WebElement boatsTable;
    
    public final WebDriver driver;

    public TrackedRacesBoatsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
        this.driver = driver;
    }
    
    public TrackedRacesBoatEditDialogPO pushAddButton() {
        this.addButton.click();
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "BoatEditDialog");
        return new TrackedRacesBoatEditDialogPO(this.driver, dialog);
    }
    
    public void pushRefreshButton() {
        this.refreshButton.click();
        waitForAjaxRequests();
    }
    
    public TrackedRacesBoatTablePO getBoatsTable() {
        return new TrackedRacesBoatTablePO(this.driver, this.boatsTable);
    }
}
