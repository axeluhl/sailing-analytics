package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class TrackedRacesCompetitorEditDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "ShortNameTextBox")
    private WebElement shortNameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;

    public TrackedRacesCompetitorEditDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setNameTextBox(String name) {
        this.nameTextBox.clear();
        this.nameTextBox.sendKeys(name);
    }

    public void setShortNameTextBox(String name) {
        this.shortNameTextBox.clear();
        this.shortNameTextBox.sendKeys(name);
    }
}
