package com.sap.sailing.selenium.pages.autoplay;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.ListBoxPO;

public class AutoPlayConfiguration extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "eventSelectionBox")
    private WebElement eventSelectionBox;
    
    @FindBy(how = BySeleniumId.class, using = "configurationSelectionBox")
    private WebElement configurationSelectionBox;
    
    @FindBy(how = BySeleniumId.class, using = "leaderboardSelectionBox")
    private WebElement leaderboardSelectionBox;
    
    @FindBy(how = BySeleniumId.class, using = "startURL")
    private WebElement startURL;
    
    public AutoPlayConfiguration(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void select(String mode, String event, String leaderboard) {
        ListBoxPO.create(driver, configurationSelectionBox).selectOptionByLabel(mode);
        ListBoxPO.create(driver, eventSelectionBox).selectOptionByLabel(event);
        ListBoxPO.create(driver, leaderboardSelectionBox).selectOptionByLabel(leaderboard);
        //wait till the page processed the changes
        waitUntil(() -> getConfiguredUrl() != null && !getConfiguredUrl().isEmpty());
    }

    public String getConfiguredUrl() {
        return startURL.getAttribute("href");
    }

}
