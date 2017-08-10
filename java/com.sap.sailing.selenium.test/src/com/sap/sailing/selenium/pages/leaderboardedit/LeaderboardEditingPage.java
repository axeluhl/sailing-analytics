package com.sap.sailing.selenium.pages.leaderboardedit;

import java.io.UnsupportedEncodingException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;

/**
 * <p>The page object representing the administration console. The console consists of multiple tabs with its content
 *   between you can switch.</p>
 * 
 * @author
 *   D049941
 */
public class LeaderboardEditingPage extends HostPage {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Leaderboard Editing"; //$NON-NLS-1$
    
    @FindBy(how = BySeleniumId.class, using = "EditableLeaderboardPanel")
    private WebElement editableLeaderboardPanel;
    
    /**
     * <p>Goes to the administration console and returns the representing page object.</p>
     * 
     * @param driver
     *   The web driver to use.
     * @param root
     *   The context root of the application.
     * @return
     *   The page object for the administration console.
     * @throws UnsupportedEncodingException 
     */
    public static LeaderboardEditingPage goToPage(String leaderboardName, WebDriver driver, String root) {
        driver.get(root + "gwt/LeaderboardEditing.html?name=" + leaderboardName + "&" + getGWTCodeServerAndLocale()); //$NON-NLS-1$
        
        return new LeaderboardEditingPage(driver);
    }
    
    private LeaderboardEditingPage(WebDriver driver) {
        super(driver);
    }
    
    public LeaderboardTable getLeaderboardTable() {
        return new LeaderboardTable(this.driver, findElementBySeleniumId(context, "LeaderboardCellTable"));
    }
    
    @Override
    protected void initElements() {
        super.initElements();
        
        // Wait for the initial loading of the data
        waitForAjaxRequestsExecuted("loadLeaderboardData", 1);
    }
    
    /**
     * <p>Verifies that the current page is the leaderboard editing page by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if (!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console: " + this.driver.getTitle()); //$NON-NLS-1$
        }
    }
}
