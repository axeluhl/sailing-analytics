package com.sap.sailing.selenium.pages.home;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.HostPageWithAuthentication;
import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} representing the SAP Sailing home page.
 */
public class HomePage extends HostPageWithAuthentication {
    
    @FindBy(how = BySeleniumId.class, using = "shortcutIcon")
    private WebElement favIcon;
    
    @FindBy(how = BySeleniumId.class, using = "logoImage")
    private WebElement sapSailingHeaderImage;

    @FindBy(how=BySeleniumId.class, using ="logoAnchor")
    private WebElement logoAnchor;

    @FindBy(how=BySeleniumId.class, using ="solutionsPageLink")
    private WebElement solutionsPageLink;

    @FindBy(how=BySeleniumId.class, using ="socialFooter")
    private WebElement socialMediaFooter;

    @FindBy(how=BySeleniumId.class, using ="copyrightDiv")
    private WebElement copyrightDiv;
    
    @FindBy(how=BySeleniumId.class, using = "imprintAnchorLink")
    private WebElement imprintAnchorLink;
    
    @FindBy(how=BySeleniumId.class, using = "privacyAnchorLink")
    private WebElement privacyAnchorLink;
    
    @FindBy(how=BySeleniumId.class, using = "supportAnchor")
    private WebElement sapSupportAnchor;

    @FindBy(how = BySeleniumId.class, using = "feedbackAnchor")
    private WebElement feedbackAnchor;
    
    @FindBy(how= BySeleniumId.class, using = "whatsNewAnchor")
    private WebElement whatsNewAnchor;
    
    @FindBy(how=BySeleniumId.class, using ="languageSelector")
    private WebElement languageSelector;
    
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
    
    public WebElement getFavicon() {
        return favIcon;
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
    
    public WebElement getSapSailingHeaderImage() {
        return sapSailingHeaderImage;
    }
    
    public WebElement getLogoAnchor() {
        return logoAnchor;
    }
    
    public WebElement getSolutionsPageLink() {
        return solutionsPageLink;
    }
    
    public WebElement getSocialmediaFooter() {
        return socialMediaFooter;
    }
    
    public WebElement getCopyrightDiv() {
        return copyrightDiv;
    }
    
    public WebElement getImprintLink() {
        return imprintAnchorLink;
    }

    public WebElement getPrivacyLink() {
        return privacyAnchorLink;
    }
    
    public WebElement getSupportLink() {
        return sapSupportAnchor;
    }
    
    public WebElement getFeedbackLink() {
        return feedbackAnchor;
    }
    
    public WebElement getNewsLink() {
        return whatsNewAnchor;
    }
    
    public WebElement getLanguageSelectionLabel() {
        return languageSelector;
    }
    
    public void clickOnEventsMenuItem() {
        driver.findElement(new BySeleniumId("eventsPage")).click();
    }
}
