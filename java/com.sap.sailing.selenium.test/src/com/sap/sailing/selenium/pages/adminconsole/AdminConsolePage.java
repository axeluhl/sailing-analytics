package com.sap.sailing.selenium.pages.adminconsole;

import java.text.MessageFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardGroupConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanelPO;

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
            ".//div[contains(@class, \"gwt-TabLayoutPanelTabInner\")]/div[text()=\"{0}\"]/../..");
    
    private static final MessageFormat VERTICAL_TAB_EXPRESSION = new MessageFormat(
            ".//div[contains(@class, \"gwt-VerticalTabLayoutPanelTabInner\")]/div[text()=\"{0}\"]/../..");
    
    private static final String REGATTA_STRUCTURE_TAB_LABEL = "Regattas"; //$NON-NLS-1$
    private static final String REGATTA_STRUCTURE_TAB_IDENTIFIER = "RegattaStructureManagement"; //$NON-NLS-1$
    
    private static final String TRACTRAC_EVENTS_TAB_PARENT_LABEL = "Connectors";
    private static final String TRACTRAC_EVENTS_TAB_PARENT_IDENTIFIER = "TrackingProviderPanel";
    
    private static final String TRACTRAC_EVENTS_TAB_LABEL = "TracTrac Events"; //$NON-NLS-1$
    private static final String TRACTRAC_EVENTS_TAB_IDENTIFIER = "TracTracEventManagement"; //$NON-NLS-1$
    
    private static final String TRACKED_RACES_TAB_PARENT_LABEL = "Races"; //$NON-NLS-1$
    private static final String TRACKED_RACES_TAB_PARENT_IDENTIFIER = "RacesPanel"; //$NON-NLS-1$
    
    private static final String TRACKED_RACES_TAB_LABEL = "Tracked races"; //$NON-NLS-1$
    private static final String TRACKED_RACES_TAB_IDENTIFIER = "TrackedRacesManagement"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL = "Leaderboards"; //$NON-NLS-1$
    private static final String LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER = "LeaderboardPanel"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_CONFIGURATION_TAB_LABEL = "Leaderboard Configuration"; //$NON-NLS-1$
    private static final String LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER = "LeaderboardConfiguration"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_GROUP_CONFIGURATION_TAB_LABEL = "Leaderboard groups"; //$NON-NLS-1$
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
        return new AdminConsolePage(driver);
    }
    
    @FindBy(how = BySeleniumId.class, using = "AdministrationTabs")
    private WebElement administrationTabPanel;
    
    private AdminConsolePage(WebDriver driver) {
        super(driver);
    }
    
    public RegattaStructureManagementPanelPO goToRegattaStructure() {
        return new RegattaStructureManagementPanelPO(this.driver, goToTab(REGATTA_STRUCTURE_TAB_LABEL,
                REGATTA_STRUCTURE_TAB_IDENTIFIER, true));
    }
    
    /**
     * <p>Switch to the TracTrac Events tab and waits until the animation is finished. The returned page object,
     *   representing the tab, becomes invalid if you switch to another tab in the administration console.</p>
     * 
     * @return
     *   The page object for the TracTracEvents tab.
     */
    public TracTracEventManagementPanelPO goToTracTracEvents() {
        goToTab(TRACTRAC_EVENTS_TAB_PARENT_LABEL, TRACTRAC_EVENTS_TAB_PARENT_IDENTIFIER, true);
        return new TracTracEventManagementPanelPO(this.driver, goToTab(TRACTRAC_EVENTS_TAB_LABEL,
                TRACTRAC_EVENTS_TAB_IDENTIFIER, false));
    }
    
    public TrackedRacesManagementPanelPO goToTrackedRaces() {
        goToTab(TRACKED_RACES_TAB_PARENT_LABEL, TRACKED_RACES_TAB_PARENT_IDENTIFIER, true);
        return new TrackedRacesManagementPanelPO(this.driver, goToTab(TRACKED_RACES_TAB_LABEL,
                TRACKED_RACES_TAB_IDENTIFIER, false));
    }
    
    public LeaderboardConfigurationPanelPO goToLeaderboardConfiguration() {
        goToTab(LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL, LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER, true);
        return new LeaderboardConfigurationPanelPO(this.driver, goToTab(LEADERBOARD_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER, false));
    }
    
    public LeaderboardGroupConfigurationPanelPO goToLeaderboardGroupConfiguration() {
        goToTab(LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL, LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER, true);
        return new LeaderboardGroupConfigurationPanelPO(this.driver, goToTab(LEADERBOARD_GROUP_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_GROUP_CONFIGURATION_TAB_IDENTIFIER, false));
    }
    
    /**
     * <p>Verifies that the current page is the administration console by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if (!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console: " + this.driver.getTitle()); //$NON-NLS-1$
        }
    }
    
    private WebElement goToTab(String label, final String id, boolean isVertical) {
        String expression = TAB_EXPRESSION.format(new Object[] {label});
        if (isVertical) {
            expression = VERTICAL_TAB_EXPRESSION.format(new Object[] {label});
        }
        WebElement tab = this.administrationTabPanel.findElement(By.xpath(expression));
        WebDriverWait waitForTab = new WebDriverWait(driver, 20); // here, wait time is 20 seconds
        waitForTab.until(ExpectedConditions.visibilityOf(tab)); // this will wait for tab to be visible for 20 seconds
        tab.click();
        // Wait for the tab to become visible due to the used animations.
        FluentWait<WebElement> wait = createFluentWait(this.administrationTabPanel);
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
        waitForAjaxRequests(); // switching tabs can trigger asynchronous updates, replacing UI elements
        return content;
    }
}
