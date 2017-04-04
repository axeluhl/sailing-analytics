package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class LeaderboardSettingsDialogPO extends DataEntryDialogPO {
    
    private LeaderboardSettingsPanelPO leaderboardSettingsPanelPO;
    
    public LeaderboardSettingsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
        leaderboardSettingsPanelPO = new LeaderboardSettingsPanelPO(this.driver, this.getWebElement());
    }
    
    public LeaderboardSettingsPanelPO getLeaderboardSettingsPanelPO() {
        return leaderboardSettingsPanelPO;
    }
    
}
