package com.sap.sailing.selenium.pages.home.event.regatta;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.PageObject;
import com.sap.sailing.selenium.pages.home.event.EventPage;

public class RegattaEventPage extends EventPage {
    
    private static final String REGATTA_INFO_IDENTIFIER = "RegattaListItemPanel";
    
    /**
     * Navigates to the given regatta event URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param url the desired destination URL
     * @return the {@link PageObject} for the regatta event page
     */
    public static RegattaEventPage goToRegattaEventUrl(WebDriver driver, String url) {
        return HostPage.goToUrl(RegattaEventPage::new, driver, url);
    }

    private RegattaEventPage(WebDriver driver) {
        super(driver);
    }
    
    @Override
    protected void initElements() {
        super.initElements();
        waitForElement(REGATTA_INFO_IDENTIFIER);
    }

}
