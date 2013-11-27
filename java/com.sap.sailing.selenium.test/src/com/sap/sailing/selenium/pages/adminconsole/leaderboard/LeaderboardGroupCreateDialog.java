package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.common.DataEntryDialog;

public class LeaderboardGroupCreateDialog extends DataEntryDialog {

    @FindBy(how = BySeleniumId.class, using = "LeaderboardGroupNameField")
    private WebElement nameField;

    @FindBy(how = BySeleniumId.class, using = "LeaderboardGroupDescriptionField")
    private WebElement descriptionField;

    public LeaderboardGroupCreateDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        this.nameField.clear();
        this.nameField.sendKeys(name);
    }

    public void setDescription(String description) {
        this.descriptionField.clear();
        this.descriptionField.sendKeys(description);
    }
}
