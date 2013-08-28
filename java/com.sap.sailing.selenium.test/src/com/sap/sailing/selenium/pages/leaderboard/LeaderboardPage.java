package com.sap.sailing.selenium.pages.leaderboard;

import java.text.MessageFormat;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.ElementSearchConditions;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanel;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesManagementPanel;
import com.sap.sailing.selenium.pages.adminconsole.tractrac.TracTracEventManagementPanel;

/**
 * <p>The page object representing the leaderboard.</p>
 * 
 * @author
 *   D049941
 */
public class LeaderboardPage extends HostPage {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Leaderboard"; //$NON-NLS-1$

    
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
    public static LeaderboardPage goToPage(WebDriver driver, String root, String leaderboard) {
        driver.get(root + "gwt/Leaderboard.html?" + getLeaderboard(leaderboard) + "&" + getGWTCodeServer()); //$NON-NLS-1$
        
        return new LeaderboardPage(driver);
    }
    
    private static String getLeaderboard(String leaderboard) {
        return "name=" + leaderboard;
    }
    
    @FindBy(how = BySeleniumId.class, using = "AdministrationTabs")
    private WebElement tabPanel;
    
    private LeaderboardPage(WebDriver driver) {
        super(driver);
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
}
