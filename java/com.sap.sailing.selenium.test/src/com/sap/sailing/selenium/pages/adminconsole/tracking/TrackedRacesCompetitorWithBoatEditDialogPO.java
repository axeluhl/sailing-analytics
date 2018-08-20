package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

public class TrackedRacesCompetitorWithBoatEditDialogPO extends TrackedRacesCompetitorEditDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "SailIdTextBox")
    private WebElement sailIdTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassNameSuggestBox")
    private WebElement boatClassNameSuggestBox;
    
    public TrackedRacesCompetitorWithBoatEditDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setSailIdTextBox(String sailId) {
        this.sailIdTextBox.clear();
        this.sailIdTextBox.sendKeys(sailId);
    }

    public void setBoatClassNameSuggestBox(String boatClassName) {
        this.boatClassNameSuggestBox.clear();
        this.boatClassNameSuggestBox.sendKeys(boatClassName);
    }
}
