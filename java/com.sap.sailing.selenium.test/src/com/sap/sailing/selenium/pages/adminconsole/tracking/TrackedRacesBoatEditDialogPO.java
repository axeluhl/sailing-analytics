package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class TrackedRacesBoatEditDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "SailIdTextBox")
    private WebElement sailIdTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassNameSuggestBox")
    private WebElement boatClassNameSuggestBox;
    
    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;

    public TrackedRacesBoatEditDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setNameTextBox(String name) {
        this.nameTextBox.clear();
        this.nameTextBox.sendKeys(name);
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
