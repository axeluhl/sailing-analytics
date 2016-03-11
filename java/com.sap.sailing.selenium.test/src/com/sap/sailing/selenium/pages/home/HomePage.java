package com.sap.sailing.selenium.pages.home;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} representing the SAP Sailing home page.
 */
public class HomePage extends HostPageWithAuthentication {
    
    /**
     * Navigates to the home page and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param root the context root of the application
     * @return the {@link PageObject} for the home page
     */
    public static HomePage goToPage(WebDriver driver, String root) {
        return goToHomeUrl(driver, root + "gwt/Home.html");
    }
    
    /**
     * Navigates to the given home URL and provides the corresponding {@link PageObject}.
     * 
     * @param driver the {@link WebDriver} to use
     * @param url the desired destination URL
     * @return the {@link PageObject} for the home page
     */
    public static HomePage goToHomeUrl(WebDriver driver, String url) {
        return HostPage.goToUrl(HomePage::new, driver, url);
    }
    
    private HomePage(WebDriver driver) {
        super(driver);
    }
    
}
