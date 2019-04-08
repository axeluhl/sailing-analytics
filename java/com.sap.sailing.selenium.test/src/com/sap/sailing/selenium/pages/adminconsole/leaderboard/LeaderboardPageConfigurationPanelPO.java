package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class LeaderboardPageConfigurationPanelPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "AllowForRaceDetailsCheckBox")
    private WebElement allowForRaceDetailsCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "ShowChartsCheckBox")
    private WebElement showChartsCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "ShowOverallLeaderboardCheckBox")
    private WebElement showOverallLeaderboardCheckBox;
    
    public LeaderboardPageConfigurationPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setAllowForRaceDetails(boolean selected) {
        new CheckBoxPO(driver, allowForRaceDetailsCheckBox).setSelected(selected);
    }
    
    public void setShowCharts(boolean selected) {
        new CheckBoxPO(driver, showChartsCheckBox).setSelected(selected);
    }
    
    public void setShowOverallLeaderboard(boolean selected) {
        new CheckBoxPO(driver, showOverallLeaderboardCheckBox).setSelected(selected);
    }

}
