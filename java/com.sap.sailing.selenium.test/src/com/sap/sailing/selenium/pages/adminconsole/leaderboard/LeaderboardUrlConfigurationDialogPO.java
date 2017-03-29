package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.SettingsDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardSettingsDialogPO;

public class LeaderboardUrlConfigurationDialogPO extends SettingsDialogPO {
    
    private static final String LEADERBOARD_SETTINGS_TAB_LABEL = "Leaderboard"; //$NON-NLS-1$
    private static final String LEADERBOARD_SETTINGS_TAB_IDENTIFIER = "LeaderboardSettingsPanel"; //$NON-NLS-1$
    
    @FindBy(how = BySeleniumId.class, using = "TabbedSettingsDialogTabPanel")
    private WebElement tabbedSettingsDialogTabPanel;

    public LeaderboardUrlConfigurationDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public LeaderboardSettingsDialogPO goToLeaderboardSettings() {
        goToTab(tabbedSettingsDialogTabPanel, LEADERBOARD_SETTINGS_TAB_LABEL, LEADERBOARD_SETTINGS_TAB_IDENTIFIER, TabPanelType.TAB_PANEL);
        return new LeaderboardSettingsDialogPO(this.driver, this.getWebElement());
    }

}
