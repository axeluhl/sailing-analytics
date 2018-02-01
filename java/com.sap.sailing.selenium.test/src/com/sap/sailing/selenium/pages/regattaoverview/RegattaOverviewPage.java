package com.sap.sailing.selenium.pages.regattaoverview;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;

/**
 * <p>The page object representing the leaderboard.</p>
 * 
 * @author
 *   D049941
 */
public class RegattaOverviewPage extends HostPage {
    
    @FindBy(how = BySeleniumId.class, using = "RegattaOverviewSettingsButton")
    private WebElement settingsButton;
    
    public static RegattaOverviewPage goToPage(WebDriver driver, String href) {
        driver.get(href);
        return new RegattaOverviewPage(driver);
    }
    
    private RegattaOverviewPage(WebDriver driver) {
        super(driver);
    }
    
    public RegattaOverviewSettingsDialogPO getRegattaOverviewSettingsDialog() {
        this.settingsButton.click();
        return new RegattaOverviewSettingsDialogPO(this.driver,
                findElementBySeleniumId(this.driver, "RegattaRacesStatesSettingsDialog"));
    }
    
}
