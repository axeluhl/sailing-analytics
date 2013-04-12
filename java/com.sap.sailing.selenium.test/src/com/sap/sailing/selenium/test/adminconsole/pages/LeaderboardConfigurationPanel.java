package com.sap.sailing.selenium.test.adminconsole.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;

public class LeaderboardConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateFlexibleLeaderboardButton")
    private WebElement createFlexibleLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateRegattaLeaderboardButton")
    private WebElement createRegattaLeaderboardButton;
    
    protected LeaderboardConfigurationPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public FlexibleLeaderboardCreationDialog startCreatingFlexibleLeaderboard() {
        this.createFlexibleLeaderboardButton.click();
        
        // Wait, since we trigger an AJAX-request to get the available events
        waitForAjaxRequests();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "CreateFlexibleLeaderboardDialog");
        
        return new FlexibleLeaderboardCreationDialog(this.driver, dialog);
    }
}
