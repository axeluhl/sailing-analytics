package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.HostPage;

import com.sap.sailing.selenium.pages.common.CSSConstants;
import com.sap.sailing.selenium.pages.common.HTMLConstants;

/**
 * <p>The page object representing the leaderboard.</p>
 * 
 * @author
 *   D049941
 */
public class LeaderboardPage extends HostPage {
    private static final String PAGE_TITLE = "SAP Sailing Analytics Leaderboard"; //$NON-NLS-1$
    
    private static final String LEADERBOAR_PARAMTER_NAME = "name"; //$NON-NLS-1$
    
    private static final String AUTO_REFRESH_ENABLED_IMAGE = "url(\"data:image/png;base64," +           //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABkAAAAZCAYAAADE6YVjAAAAR0lEQVR42mOoqan5T2vMMGoJxZZUV1fPB9IOWDC" + //$NON-NLS-1$
            "ymgJ0eagY0T5pYMAC0NQ4YJF3GLVk1JJRS0YtGbVkSFhCr/pktI4fOEsA89uCDGg41QoAAAAASUVORK5CYII=\")"; //$NON-NLS-1$
    
    private static final String AUTO_REFRESH_DISABLED_IMAGE = "url(\"data:image/png;base64," +          //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABkAAAAZCAYAAADE6YVjAAAAhElEQVR42mOoqan5T2vMMGoJVSyprq6eD6TX09o" + //$NON-NLS-1$
            "nDQxAAKQDgBbep6klIFBfXy9QW1vbT1NLYKCqqsoAKH6eppbAADD4CoDy72lqCTQIFYhNGGRbAgPEJAyKLSEmYV" + //$NON-NLS-1$
            "DFEhgAWpQwdH1C0ziheeqiaT6haY6nedlF01KYXvXJaB0/MJYAANIIBwANoTb3AAAAAElFTkSuQmCC\")";        //$NON-NLS-1$
    
    /**
     * <p>Goes to the specified leaderboard and returns the representing page object.</p>
     * 
     * @param driver
     *   The web driver to use.
     * @param root
     *   The context root of the application.
     * @param leaderboard
     *   The name of the leaderboard.
     * @return
     *   The page object for the administration console.
     */
    public static LeaderboardPage goToPage(WebDriver driver, String root, String leaderboard) {
        driver.get(root + "gwt/Leaderboard.html?" + getLeaderboard(leaderboard) + //$NON-NLS-1$
                "&" + getGWTCodeServer()); //$NON-NLS-1$
        
        return new LeaderboardPage(driver);
    }
    
    private static String getLeaderboard(String leaderboard) {
        return LEADERBOAR_PARAMTER_NAME + "=" + leaderboard; //$NON-NLS-1$
    }
    
    @FindBy(how = BySeleniumId.class, using = "AutoRefreshToggleButton")
    private WebElement autoRefreshToggleButton;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardTable")
    private WebElement leaderboardTable;
    
    private LeaderboardPage(WebDriver driver) {
        super(driver);
    }
    
    public boolean isAutoRefreshEnabled() {
        WebElement image = this.autoRefreshToggleButton.findElement(By.tagName(HTMLConstants.IMAGE_TAG_NAME));
        String background = image.getCssValue(CSSConstants.CSS_BACKGROUND_IMAGE);
        
        if(AUTO_REFRESH_ENABLED_IMAGE.equals(background))
            return true;
        
        if(AUTO_REFRESH_DISABLED_IMAGE.equals(background))
            return false;
        
        throw new RuntimeException("Can not determine auto refresh state"); //$NON-NLS-1$
    }
    
    public void setAutoRefreshEnabled(boolean enabled) {
        if(isAutoRefreshEnabled() != enabled)
            this.autoRefreshToggleButton.click();
    }
    
    /**
     * <p>Verifies that the current page is the leaderboard by checking the title of the page.</p>
     */
    @Override
    protected void verify() {
        if(!PAGE_TITLE.equals(this.driver.getTitle())) {
            throw new IllegalStateException("This is not the leaderboard"); //$NON-NLS-1$
        }
    }
}
