package com.sap.sailing.selenium.pages.home;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
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
        return goToPage(driver, root, false);
    }

    public static HomePage goToPage(WebDriver driver, String root, boolean debranded) {
        return goToHomeUrl(driver, root + "gwt/Home.html" + (debranded ? "?whitelabel" : ""));
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
    
    public WebElement getFavicon() {
        return driver.findElement(new BySeleniumId("shortcutIcon"));
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    public WebElement getSapSailingHeaderImage() {
        return driver.findElement(new BySeleniumId("logoImage"));
    }
    
    public WebElement getLogoAnchor() {
        return driver.findElement(new BySeleniumId("logoAnchor"));
    }
    
    public WebElement getSolutionsPageLink() {
        return driver.findElement(new BySeleniumId("solutionsPageLink"));
    }
    
    public WebElement getSocialmediaFooter() {
        throw new RuntimeException("");
    }
    
    public WebElement getCopyrightLink() {
        throw new RuntimeException("");
    }
    
    public WebElement getJobsLink() {
        throw new RuntimeException("");
    }
    
    public WebElement getFeedbackLink() {
        throw new RuntimeException("");    
    }
    
    public WebElement getNewsLink() {
        throw new RuntimeException("");
    }
    
    public WebElement getLanguageSelectionLabel() {
        throw new RuntimeException("");
    }
    
}
