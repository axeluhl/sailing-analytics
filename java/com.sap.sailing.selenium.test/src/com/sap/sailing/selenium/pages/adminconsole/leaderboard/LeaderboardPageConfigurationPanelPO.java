package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class LeaderboardPageConfigurationPanelPO extends PageArea {
    
    public LeaderboardPageConfigurationPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setAllowForRaceDetails(boolean selected) {
        WebElement element = findElementBySeleniumId("AllowForRaceDetailsCheckBox");
        CheckBoxPO checkbox = new CheckBoxPO(driver, element);
        checkbox.setSelected(selected);
    }
    
    public void setShowCharts(boolean selected) {
        WebElement element = findElementBySeleniumId("ShowChartsCheckBox");
        CheckBoxPO checkbox = new CheckBoxPO(driver, element);
        checkbox.setSelected(selected);
    }
    
    public void setShowOverallLeaderboard(boolean selected) {
        WebElement element = findElementBySeleniumId("ShowOverallLeaderboardCheckBox");
        CheckBoxPO checkbox = new CheckBoxPO(driver, element);
        checkbox.setSelected(selected);
    }

}
