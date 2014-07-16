package com.sap.sailing.selenium.pages.adminconsole;

import java.text.MessageFormat;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.ui.FluentWait;

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
    private enum ScrollDirection {
        Left {
            @Override
            public By getBy() {
                return By.className("gwt-ScrolledTabLayoutPanel-scrollLeft");
            }
        },
        Rigth {
            @Override
            public By getBy() {
                return By.className("gwt-ScrolledTabLayoutPanel-scrollRight");
            }
        };
        
        public abstract By getBy();
    }
    
    private static final String PAGE_TITLE = "SAP Sailing Analytics Administration Console"; //$NON-NLS-1$
    
    private static final MessageFormat TAB_EXPRESSION = new MessageFormat(
            ".//div[contains(@class, \"gwt-TabLayoutPanelTabInner\")]/div[text()=\"{0}\"]/../..");
    
    private static final String REGATTA_STRUCTURE_TAB_LABEL = "Regattas"; //$NON-NLS-1$
    private static final String REGATTA_STRUCTURE_TAB_IDENTIFIER = "RegattaStructureManagement"; //$NON-NLS-1$
    
    private static final String TRACTRAC_EVENTS_TAB_PARENT_LABEL = "Tracking Provider";
    private static final String TRACTRAC_EVENTS_TAB_PARENT_IDENTIFIER = "TrackingProviderPanel";
    
    private static final String TRACTRAC_EVENTS_TAB_LABEL = "TracTrac Events"; //$NON-NLS-1$
    private static final String TRACTRAC_EVENTS_TAB_IDENTIFIER = "TracTracEventManagement"; //$NON-NLS-1$
    
    private static final String TRACKED_RACES_TAB_PARENT_LABEL = "Races"; //$NON-NLS-1$
    private static final String TRACKED_RACES_TAB_PARENT_IDENTIFIER = "RacesPanel"; //$NON-NLS-1$
    
    private static final String TRACKED_RACES_TAB_LABEL = "Tracked races"; //$NON-NLS-1$
    private static final String TRACKED_RACES_TAB_IDENTIFIER = "TrackedRacesManagement"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL = "Leaderboard Configuration"; //$NON-NLS-1$
    private static final String LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER = "LeaderboardPanel"; //$NON-NLS-1$
    
    private static final String LEADERBOARD_CONFIGURATION_TAB_LABEL = "Leaderboards"; //$NON-NLS-1$
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
        
        // TODO: As soon as the security API is available in Selenium we should use it to login into the admin console.
//        FluentWait<WebDriver> wait = createFluentWait(driver, 5, 100);
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
    
    @CacheLookup
    @FindBy(how = ByXPath.class, using = ".//div[contains(@class, \"gwt-TabLayoutPanelTabs\")]")
    private WebElement tabsContainer;
    
    @CacheLookup
    @FindBy(how = ByXPath.class, using = ".//div[contains(@class, \"gwt-TabLayoutPanelTabInner\")]")
    private List<WebElement> tabs;
    
    private AdminConsolePage(WebDriver driver) {
        super(driver);
    }
    
    public RegattaStructureManagementPanelPO goToRegattaStructure() {
        return new RegattaStructureManagementPanelPO(this.driver, goToTab(REGATTA_STRUCTURE_TAB_LABEL,
                REGATTA_STRUCTURE_TAB_IDENTIFIER));
    }
    
    /**
     * <p>Switch to the TracTrac Events tab and waits until the animation is finished. The returned page object,
     *   representing the tab, becomes invalid if you switch to another tab in the administration console.</p>
     * 
     * @return
     *   The page object for the TracTracEvents tab.
     */
    public TracTracEventManagementPanelPO goToTracTracEvents() {
        goToTab(TRACTRAC_EVENTS_TAB_PARENT_LABEL, TRACTRAC_EVENTS_TAB_PARENT_IDENTIFIER);
        return new TracTracEventManagementPanelPO(this.driver, goToTab(TRACTRAC_EVENTS_TAB_LABEL,
                TRACTRAC_EVENTS_TAB_IDENTIFIER));
    }
    
    public TrackedRacesManagementPanelPO goToTrackedRaces() {
        goToTab(TRACKED_RACES_TAB_PARENT_LABEL, TRACKED_RACES_TAB_PARENT_IDENTIFIER);
        return new TrackedRacesManagementPanelPO(this.driver, goToTab(TRACKED_RACES_TAB_LABEL,
                TRACKED_RACES_TAB_IDENTIFIER));
    }
    
    public LeaderboardConfigurationPanelPO goToLeaderboardConfiguration() {
        goToTab(LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL, LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER);
        return new LeaderboardConfigurationPanelPO(this.driver, goToTab(LEADERBOARD_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_CONFIGURATION_TAB_IDENTIFIER));
    }
    
    public LeaderboardGroupConfigurationPanelPO goToLeaderboardGroupConfiguration() {
        goToTab(LEADERBOARD_CONFIGURATION_TAB_PARENT_LABEL, LEADERBOARD_CONFIGURATION_TAB_PARENT_IDENTIFIER);
        return new LeaderboardGroupConfigurationPanelPO(this.driver, goToTab(LEADERBOARD_GROUP_CONFIGURATION_TAB_LABEL,
                LEADERBOARD_GROUP_CONFIGURATION_TAB_IDENTIFIER));
    }
    
    /**
     * <p>Verifies that the current page is the administration console by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if(!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console: " + this.driver.getTitle()); //$NON-NLS-1$
        }
    }
    
    private WebElement goToTab(String label, final String id) {
        String expression = TAB_EXPRESSION.format(new Object[] {label});
        WebElement tab = this.tabPanel.findElement(By.xpath(expression));
        ScrollDirection direction = getScrollDirection(tab);
        if (direction != null) {
            WebElement scroller = this.tabPanel.findElement(direction.getBy());
            while (!tab.isDisplayed() && scroller.isDisplayed()) {
                scrollAndWait(scroller);
            }
            // Workaround for "Offset within element cannot be scrolled into view: (-1, 5)".
            // We try to scroll one more time to make the tab completely visible
            //
            // TODO: Create a bug report for this!
            int tabIndex = (direction == ScrollDirection.Left ? getFirstVisibleTabIndex() : getLastVisibleTabIndex());
            if(scroller.isDisplayed() && getTabIndex(tab) == tabIndex) {
                scrollAndWait(scroller);
            }
        }
        // We have to determine the location where we have to click at the tab for the case its not completely visible.
        // NOTE: We assume that the browser window is big enough to display at least 2 tabs!
        Actions actions = new Actions(this.driver);
        actions.moveToElement(tab, determineOffsetForClick(tab), 5);
        actions.click();
        actions.perform();
        // Wait for the tab to become visible due to the used animations.
        FluentWait<WebElement> wait = createFluentWait(this.tabPanel);
        WebElement content = wait.until(ElementSearchConditions.visibilityOfElementLocated(new BySeleniumId(id)));
        waitForAjaxRequests(); // switching tabs can trigger asynchronous updates, replacing UI elements
        return content;
    }
    
    private ScrollDirection getScrollDirection(WebElement tab) {
        int tabIndex = getTabIndex(tab);
        if (tabIndex < getFirstVisibleTabIndex()) {
            return ScrollDirection.Left;
        }
        if (tabIndex > getLastVisibleTabIndex()) {
            return ScrollDirection.Rigth;
        }
        return null;
    }
    
    private void scrollAndWait(WebElement scroller) {
        WebElement arrow = scroller.findElement(By.tagName("img"));
        arrow.click();
        // TODO: Find a better solution with ImplicitWait
        try {
            Thread.sleep(500L);
        } catch (InterruptedException exception) {
        }
    }
    
    private int getTabIndex(WebElement tab) {
        return tab.findElements(By.xpath("./preceding-sibling::div")).size();
    }
    
    private int getFirstVisibleTabIndex() {
        for (int i = 0; i <= this.tabs.size() - 1; i++) {
            if (this.tabs.get(i).isDisplayed()) {
                return i;
            }
        }
        return -1;
    }
    
    private int getLastVisibleTabIndex() {
        for (int i = this.tabs.size() - 1; i >= 0; i--) {
            if (this.tabs.get(i).isDisplayed()) {
                return i;
            }
        }
        return -1;
    }

    private int determineOffsetForClick(WebElement tab) {
        int tabIndex = getTabIndex(tab);
        if (tabIndex == getFirstVisibleTabIndex()) {
            return -1;
        }
        if (tabIndex == getLastVisibleTabIndex()) {
            return +1;
        }
        return tab.getSize().width / 2;
    }
}
