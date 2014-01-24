package com.sap.sailing.selenium.test.leaderboard;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
            logger.log(Level.SEVERE, "Exception during switchTo", e);
        }
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
        
        return new LeaderboardEditingPage(driver);
    }
    
    @FindBy(how = BySeleniumId.class, using = "EditableLeaderboardPanel")
    private WebElement editableLeaderboardPanel;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardTable")
    private WebElement leaderboardTable;
    
    private LeaderboardEditingPage(WebDriver driver) {
        super(driver);
    }
    
    public LeaderboardTable getLeaderboardTable() {
        return new LeaderboardTable(this.driver, this.leaderboardTable);
    }
    
    /**
     * <p>Verifies that the current page is the leaderboard editing page by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if(!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the administration console: " + this.driver.getTitle()); //$NON-NLS-1$
        }
    }
}
