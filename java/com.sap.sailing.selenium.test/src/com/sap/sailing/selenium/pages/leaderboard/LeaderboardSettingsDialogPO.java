package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class LeaderboardSettingsDialogPO extends DataEntryDialogPO {
    
    private LeaderboardSettingsPanelPO leaderboardSettingsPanelPO;
    
    public LeaderboardSettingsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
        leaderboardSettingsPanelPO = new LeaderboardSettingsPanelPO(this.driver, this.getWebElement());
    }
    
    public LeaderboardSettingsPanelPO getLeaderboardSettingsPanelPO() {
        return leaderboardSettingsPanelPO;
    }
    
    public void waitForRaceDetailsAverageSpeedUntil(boolean expected) {
        WebElement element = findElementBySeleniumId("RaceAverageSpeedOverGroundInKnotsCheckBox");
        new CheckBoxPO(driver, element).waitForElementUntil(expected);
    }
    
}
