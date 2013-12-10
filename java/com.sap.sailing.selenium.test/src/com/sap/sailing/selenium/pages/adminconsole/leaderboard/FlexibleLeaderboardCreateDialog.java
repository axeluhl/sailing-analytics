package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.common.DataEntryDialog;

public class FlexibleLeaderboardCreateDialog extends DataEntryDialog {

    @FindBy(how = BySeleniumId.class, using = "LeaderboardNameField")
    private WebElement nameField;

    public FlexibleLeaderboardCreateDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        this.nameField.clear();
        this.nameField.sendKeys(name);
    }
}
