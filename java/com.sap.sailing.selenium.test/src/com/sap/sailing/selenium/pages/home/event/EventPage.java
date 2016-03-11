package com.sap.sailing.selenium.pages.home.event;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;

public class EventPage extends HostPageWithAuthentication {
    
    /**
     * Navigates to the given home URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param url the desired destination URL
     * @return the {@link PageObject} for the home page
     */
    public static EventPage goToEventUrl(WebDriver driver, String url) {
        return HostPage.goToUrl(EventPage::new, driver, url);
    }

    private EventPage(WebDriver driver) {
        super(driver);
    }
    
    public EventHeaderPO getEventHeader() {
        return getPO(EventHeaderPO::new, "EventHeaderPanel");
    }

}
