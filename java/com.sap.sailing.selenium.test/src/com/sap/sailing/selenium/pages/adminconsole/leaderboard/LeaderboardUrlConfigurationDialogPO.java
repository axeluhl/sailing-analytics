package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.SettingsDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardSettingsPanelPO;

public class LeaderboardUrlConfigurationDialogPO extends SettingsDialogPO {
    
    private static final String LEADERBOARD_SETTINGS_TAB_LABEL = "Leaderboard"; //$NON-NLS-1$
    private static final String LEADERBOARD_SETTINGS_TAB_IDENTIFIER = "LeaderboardSettingsPanel"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_PAGE_SETTINGS_TAB_LABEL = "LeaderboardPage"; //$NON-NLS-1$
    private static final String LEADERBOARD_PAGE_SETTINGS_TAB_IDENTIFIER = "LeaderboardPageSettingsPanel"; //$NON-NLS-1$
    
    private static final String OVERALL_LEADERBOARD_SETTINGS_TAB_LABEL = "Overall leaderboard"; //$NON-NLS-1$
    private static final String OVERALL_LEADERBOARD_SETTINGS_TAB_IDENTIFIER = "OverallLeaderboardSettingsPanel"; //$NON-NLS-1$
    
    @FindBy(how = BySeleniumId.class, using = "TabbedSettingsDialogTabPanel")
    private WebElement tabbedSettingsDialogTabPanel;

    public LeaderboardUrlConfigurationDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public LeaderboardSettingsPanelPO goToLeaderboardSettings() {
        WebElement element = goToTab(tabbedSettingsDialogTabPanel, LEADERBOARD_SETTINGS_TAB_LABEL, LEADERBOARD_SETTINGS_TAB_IDENTIFIER, TabPanelType.TAB_PANEL);
        return new LeaderboardSettingsPanelPO(this.driver, element);
    }
    
    public LeaderboardPageConfigurationPanelPO goToLeaderboardPageSettings() {
        goToTab(tabbedSettingsDialogTabPanel, LEADERBOARD_PAGE_SETTINGS_TAB_LABEL, LEADERBOARD_PAGE_SETTINGS_TAB_IDENTIFIER, TabPanelType.TAB_PANEL);
        return new LeaderboardPageConfigurationPanelPO(this.driver, this.getWebElement());
    }
    
    public LeaderboardSettingsPanelPO goToOverallLeaderboardSettings() {
        WebElement element = goToTab(tabbedSettingsDialogTabPanel, OVERALL_LEADERBOARD_SETTINGS_TAB_LABEL, OVERALL_LEADERBOARD_SETTINGS_TAB_IDENTIFIER, TabPanelType.TAB_PANEL);
        return new LeaderboardSettingsPanelPO(this.driver, element);
    }

}
