package com.sap.sailing.selenium.test.adminconsole.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;

public class FlexibleLeaderboardCreationDialog extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;
    @FindBy(how = BySeleniumId.class, using = "CancelButton")
    private WebElement cancelButton;
    @FindBy(how = BySeleniumId.class, using = "LeaderboardNameField")
    private WebElement nameField;
    @FindBy(how = BySeleniumId.class, using = "StatusLabel")
    private WebElement statusLabel;

    public FlexibleLeaderboardCreationDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        this.nameField.clear();
        this.nameField.sendKeys(name);
    }
    
    public boolean isOkEnabled() {
        return this.okButton.isEnabled();
    }
    
    public String getErrorMessage() {
        return this.statusLabel.getText();
    }
    
    public void pressOk() {
        this.okButton.click();
        
        // Wait, since we create the flexible leader board
        waitForAjaxRequests();
    }
    
    public void pressCancel() {
        this.cancelButton.click();
    }
}
