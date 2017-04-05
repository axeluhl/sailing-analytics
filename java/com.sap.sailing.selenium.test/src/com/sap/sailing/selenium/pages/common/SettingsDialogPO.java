package com.sap.sailing.selenium.pages.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardPage;

public class SettingsDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "ShareAnchor")
    private WebElement shareAnchor;

    public SettingsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public LeaderboardPage openLeaderboard() {
        String href = this.shareAnchor.getAttribute("href");
        return LeaderboardPage.goToPage(driver, href);
    }
    
    public String getLeaderboardLink() {
        return this.shareAnchor.getAttribute("href");
    }
}
