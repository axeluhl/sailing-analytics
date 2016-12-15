package com.sap.sailing.selenium.pages.raceboard;

import java.util.function.BooleanSupplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} representing the SAP Sailing home page.
 */
public class RaceBoardPage extends HostPageWithAuthentication {
    
    @FindBy(how = BySeleniumId.class, using = "raceMapSettingsButton")
    private WebElement raceMapSettingsButton;

    /**
     * Navigates to the given home URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param url the desired destination URL
     * @return the {@link PageObject} for the home page
     */
    public static RaceBoardPage goToRaceboardUrl(WebDriver driver, String url) {
        // workaround for hostpage removing query from url, required for evenntid in this case
        driver.get(url);
        return new RaceBoardPage(driver);
    }
    
    private RaceBoardPage(WebDriver driver) {
        super(driver);
    }
    public MapSettingsPO openMapSettings() {
        raceMapSettingsButton.click();
        waitUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() {
                return context.findElement(new BySeleniumId("raceMapSettings")) != null;
            }
        });
        return new MapSettingsPO(driver, context.findElement(new BySeleniumId("raceMapSettings")));
    }
    
}
