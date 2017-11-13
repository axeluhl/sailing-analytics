package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class TrackedRacesCompetitorsPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "RefreshButton")
    private WebElement refreshButton;
    
    @FindBy(how = BySeleniumId.class, using = "AddCompetitorButton")
    private WebElement addCompetitorButton;

    @FindBy(how = BySeleniumId.class, using = "CompetitorsTable")
    private WebElement competitorsTable;
    
    public final WebDriver driver;

    public TrackedRacesCompetitorsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
        this.driver = driver;
    }
    
    public TrackedRacesCompetitorEditDialogPO pushAddCompetitorButton() {
        this.addCompetitorButton.click();
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "CompetitorEditDialog");
        return new TrackedRacesCompetitorEditDialogPO(this.driver, dialog);
    }

    public void pushRefreshButton() {
        this.refreshButton.click();
        waitForAjaxRequests();
    }
    
    public TrackedRacesCompetitorTablePO getCompetitorTable() {
        return new TrackedRacesCompetitorTablePO(this.driver, this.competitorsTable);
    }
}
