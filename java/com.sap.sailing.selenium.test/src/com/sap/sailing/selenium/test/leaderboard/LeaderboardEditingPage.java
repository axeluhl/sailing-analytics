package com.sap.sailing.selenium.test.leaderboard;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.HostPage;

/**
 * <p>The page object representing the administration console. The console consists of multiple tabs with its content
 *   between you can switch.</p>
 * 
 * @author
 *   D049941
 */
public class LeaderboardEditingPage extends HostPage {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Leaderboard Editing"; //$NON-NLS-1$
    
    private static final Logger logger = Logger.getLogger(LeaderboardEditingPage.class.getName());

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
    public static LeaderboardEditingPage goToPage(String leaderboardName, WebDriver driver, String root) throws UnsupportedEncodingException {
        driver.get(root + "gwt/LeaderboardEditing.html?name="+leaderboardName + "&" + System.getProperty("gwt.codesvr", "")); //$NON-NLS-1$
        try {
            Alert alert = driver.switchTo().alert();
            if (alert != null) {
                alert.accept();
            }
        } catch (NoAlertPresentException e) {
            logger.log(Level.INFO, "No alert message found; probably already logged in: "+e.getMessage());
        }
        return new LeaderboardEditingPage(driver);
    }
    
    private LeaderboardEditingPage(WebDriver driver) {
        super(driver);
    }
    
    public LeaderboardTable getLeaderboardTable() {
        return new LeaderboardTable(this.driver, findElementBySeleniumId(context, "LeaderboardTable"));
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
