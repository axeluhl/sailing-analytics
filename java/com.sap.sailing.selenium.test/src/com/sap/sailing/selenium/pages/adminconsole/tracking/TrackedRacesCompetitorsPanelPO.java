package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class TrackedRacesCompetitorsPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "AddCompetitorButton")
    private WebElement addButton;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorsTable")
    private WebElement competitorsTabel;

    public TrackedRacesCompetitorsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public TrackedRacesCompetitorCreateDialogPO pushAddButton() {
        this.addButton.click();
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "CompetitorEditDialog");
        return new TrackedRacesCompetitorCreateDialogPO(this.driver, dialog);
    }
    
    public TrackedRacesCompetitorTablePO getCompetitorTable() {
        return new TrackedRacesCompetitorTablePO(this.driver, this.competitorsTabel);
    }
}
