package com.sap.sailing.selenium.test.adminconsole.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;

public class LeaderboardConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateFlexibleLeaderboardButton")
    private WebElement createFlexibleLeaderboardButton;
    
    public LeaderboardConfigurationPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public FlexibleLeaderboardCreationDialog startCreatingFlexibleLeaderboard() {
        createFlexibleLeaderboardButton.click();
        return new FlexibleLeaderboardCreationDialog(this.driver, findElementBySeleniumId(this.context, "CreateFlexibleLeaderboardDialog"));
    }
}
