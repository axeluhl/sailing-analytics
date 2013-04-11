package com.sap.sailing.selenium.test.adminconsole.pages;

import java.text.MessageFormat;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.test.HostPage;

/**
 * <p>The page object representing the administration console. The console consists of multiple tabs with its content
 *   between you can switch.</p>
 * 
 * @author
 *   D049941
 */
public class AdminConsolePage extends HostPage {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Administration Console"; //$NON-NLS-1$
    
    private static final MessageFormat TAB_EXPRESSION = new MessageFormat(
            ".//div[@class=\"gwt-TabBarItem\" and @role=\"tab\"]/div[text()=\"{0}\"]/.."); //$NON-NLS-1$
    
    private static final String TRACTRAC_EVENTS_TAB_LABEL = "TracTrac Events"; //$NON-NLS-1$
    private static final String TRACTRAC_EVENTS_TAB_IDENTIFIER = "TracTracEventManagement"; //$NON-NLS-1$

    private static final String LEADERBOARD_CONFIGURATION_TAB_LABEL = "Leaderboard Configuration";
    private static final String LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER = "LeaderboardConfiguration";

    
    /**
     * <p>Goes to the administration console and returns the representing page object.</p>
     * 
     * @param driver
     *   The web driver to use.
     * @param root
     *   The context root of the application.
     * @return
     *   The page object for the administration console.
     */
    public static AdminConsolePage goToPage(WebDriver driver, String root) {
        driver.get(root + "gwt/AdminConsole.html?" + System.getProperty("gwt.codesvr", "")); //$NON-NLS-1$
        
        // TODO: As soon as the security API is available in Selenium we should use it to login into the admin console.
//        FluentWait<WebDriver> wait = new FluentWait<>(driver);
//        wait.withTimeout(5, TimeUnit.SECONDS);
//        wait.pollingEvery(100, TimeUnit.MILLISECONDS);
//        
//        Alert alert = wait.until(new Function<WebDriver, Alert>() {
//            @Override
//            public Alert apply(WebDriver context) {
//                TargetLocator locator = context.switchTo();
//                
//                return locator.alert();
//            }
//        });
//        alert.authenticateUsing(new UserAndPassword("user", "password"));
        
        return new AdminConsolePage(driver);
    }
    
    @FindBy(how = BySeleniumId.class, using = "AdministrationTabs")
    private WebElement tabPanel;
    
    private AdminConsolePage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * <p>Switch to the TracTrac Events tab and waits until the animation is finished. The returned page object,
     *   representing the tab, becomes invalid if you switch to another tab in the administration console.</p>
     * 
     * @return
     *   The page object for the TracTracEvents tab.
     */
    public TracTracEventManagementPanel goToTracTracEvents() {
        return new TracTracEventManagementPanel(this.driver, goToTab(TRACTRAC_EVENTS_TAB_LABEL,
                TRACTRAC_EVENTS_TAB_IDENTIFIER));
    }
    
    public LeaderboardConfigurationPanel goToLeaderboardConfiguration() {
        return new LeaderboardConfigurationPanel(this.driver, goToTab(LEADERBOARD_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER));
    }

    /**
     * <p>Verifies that the current page is the administration console by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if(!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console"); //$NON-NLS-1$
        }
    }
    
    private WebElement goToTab(String label, final String id) {
        String expression = TAB_EXPRESSION.format(new Object[] {label});
        WebElement tab = this.tabPanel.findElement(By.xpath(expression));
        
        tab.click();
        
        // Wait for the tab to become visible due to the used animations.
        FluentWait<WebElement> wait = new FluentWait<>(this.tabPanel);
        wait.withTimeout(30, TimeUnit.SECONDS);
        wait.pollingEvery(5, TimeUnit.SECONDS);
        
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
                
        return content;
    }
}
