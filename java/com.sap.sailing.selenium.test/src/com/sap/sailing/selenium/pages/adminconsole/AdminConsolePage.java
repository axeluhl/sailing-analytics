package com.sap.sailing.selenium.pages.adminconsole;

import java.text.MessageFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel;

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
            ".//div[contains(@class, \"gwt-TabLayoutPanelTab\")]/div/div[text()=\"{0}\"]/.."); //$NON-NLS-1$
    
    private static final String REGATTA_STRUCTURE_TAB_LABEL = "Regattas"; //$NON-NLS-1$
    private static final String REGATTA_STRUCTURE_TAB_IDENTIFIER = "RegattaStructureManagement"; //$NON-NLS-1$
    
    private static final String TRACTRAC_EVENTS_TAB_LABEL = "TracTrac Events"; //$NON-NLS-1$
    private static final String TRACTRAC_EVENTS_TAB_IDENTIFIER = "TracTracEventManagement"; //$NON-NLS-1$

    private static final String TRACKED_RACES_TAB_LABEL = "Tracked races"; //$NON-NLS-1$
    private static final String TRACKED_RACES_TAB_IDENTIFIER = "TrackedRacesManagement"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_CONFIGURATION_TAB_LABEL = "Leaderboard Configuration"; //$NON-NLS-1$
    private static final String LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER = "LeaderboardConfiguration"; //$NON-NLS-1$

    private static final String LEADERBOARD_GROUP_CONFIGURATION_TAB_LABEL = "Leaderboard Group Configuration"; //$NON-NLS-1$
    private static final String LEADERBOARD_GROUP_CONFIGURATION_TAB_IDENTIFIER = "LeaderboardGroupConfiguration"; //$NON-NLS-1$
    
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
        driver.get(root + "gwt/AdminConsole.html?" + getGWTCodeServer()); //$NON-NLS-1$
        
        // TODO: As soon as the security API is available in Selenium we should use it to login into the admin console.
//        FluentWait<WebDriver> wait = createFluentWait<>(driver);
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
    
    public RegattaStructureManagementPanel goToRegattaStructure() {
        return new RegattaStructureManagementPanel(this.driver, goToTab(REGATTA_STRUCTURE_TAB_LABEL,
                REGATTA_STRUCTURE_TAB_IDENTIFIER));
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
    
    public TrackedRacesManagementPanel goToTrackedRaces() {
        return new TrackedRacesManagementPanel(this.driver, goToTab(TRACKED_RACES_TAB_LABEL,
                TRACKED_RACES_TAB_IDENTIFIER));
    }
    
    public LeaderboardConfigurationPanel goToLeaderboardConfiguration() {
        return new LeaderboardConfigurationPanel(this.driver, goToTab(LEADERBOARD_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER));
    }

    public LeaderboardGroupConfigurationPanel goToLeaderboardGroupConfiguration() {
        return new LeaderboardGroupConfigurationPanel(this.driver, goToTab(LEADERBOARD_GROUP_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_GROUP_CONFIGURATION_TAB_IDENTIFIER));
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
        
        // Wait for the tab content to become visible due to the used animations.
        FluentWait<WebElement> wait = createFluentWait(this.tabPanel);
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
                
        return content;
    }
}
