package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.HostPage;
import com.sap.sailing.selenium.pages.common.CSSConstants;
import com.sap.sailing.selenium.pages.common.HTMLConstants;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

/**
 * <p>The page object representing the leaderboard.</p>
 * 
 * @author
 *   D049941
 */
public class LeaderboardPage extends HostPage {
    
    private static final String LEADERBOAR_PARAMTER_NAME = "name"; //$NON-NLS-1$
    private static final String SHOW_RACE_DETAILS_PARAMTER_NAME = "showRaceDetails"; //$NON-NLS-1$
    
    private static final String AUTO_REFRESH_ENABLED_IMAGE = "url(\"data:image/png;base64," +           //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABkAAAAZCAYAAADE6YVjAAAAR0lEQVR42mOoqan5T2vMMGoJxZZUV1fPB9IOWDC" + //$NON-NLS-1$
            "ymgJ0eagY0T5pYMAC0NQ4YJF3GLVk1JJRS0YtGbVkSFhCr/pktI4fOEsA89uCDGg41QoAAAAASUVORK5CYII=\")"; //$NON-NLS-1$
    
    private static final String AUTO_REFRESH_DISABLED_IMAGE = "url(\"data:image/png;base64," +          //$NON-NLS-1$
            "iVBORw0KGgoAAAANSUhEUgAAABkAAAAZCAYAAADE6YVjAAAAhElEQVR42mOoqan5T2vMMGoJVSyprq6eD6TX09o" + //$NON-NLS-1$
            "nDQxAAKQDgBbep6klIFBfXy9QW1vbT1NLYKCqqsoAKH6eppbAADD4CoDy72lqCTQIFYhNGGRbAgPEJAyKLSEmYV" + //$NON-NLS-1$
            "DFEhgAWpQwdH1C0ziheeqiaT6haY6nedlF01KYXvXJaB0/MJYAANIIBwANoTb3AAAAAElFTkSuQmCC\")";        //$NON-NLS-1$
    
    public static LeaderboardPage goToPage(WebDriver driver, String root, String leaderboard) {
        return goToPage(driver, root, leaderboard, false);
    }
    
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
    public static LeaderboardPage goToPage(WebDriver driver, String root, String leaderboard, boolean raceDetails) {
        driver.get(root + "gwt/Leaderboard.html?" + getLeaderboard(leaderboard) + //$NON-NLS-1$
                "&" + getGWTCodeServer() + "&" + getShowRaceDeatails(raceDetails)); //$NON-NLS-1$
        
        return new LeaderboardPage(driver, leaderboard);
    }
    
    private static String getLeaderboard(String leaderboard) {
        return LEADERBOAR_PARAMTER_NAME + "=" + leaderboard; //$NON-NLS-1$
    }
    
    private static String getShowRaceDeatails(boolean raceDetails) {
        return SHOW_RACE_DETAILS_PARAMTER_NAME + "=" + raceDetails; //$NON-NLS-1$
    }
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardDisplayCheckBox")
    private WebElement leaderboardDisplayCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardSettingsButton")
    private WebElement leaderboardSettingsButton;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorChartsDisplayCheckBox")
    private WebElement competitorChartsDisplayCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorChartsSettingsButton")
    private WebElement competitorChartsSettingsButton;
    
    @FindBy(how = BySeleniumId.class, using = "PlayAndPauseAnchor")
    private WebElement playAndPauseAnchor;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardCellTable")
    private WebElement leaderboardCellTable;
    
    private LeaderboardPage(WebDriver driver, String leaderboard) {
        super(driver);
        
        if(!this.driver.getTitle().equals(leaderboard)) {
            throw new IllegalStateException("This is not the leaderboard"); //$NON-NLS-1$
        }
    }
    
    public boolean isAutoRefreshEnabled() {
        WebElement image = this.playAndPauseAnchor.findElement(By.tagName(HTMLConstants.IMAGE_TAG_NAME));
        String background = image.getCssValue(CSSConstants.CSS_BACKGROUND_IMAGE);
        
        if(AUTO_REFRESH_ENABLED_IMAGE.equals(background))
            return true;
        
        if(AUTO_REFRESH_DISABLED_IMAGE.equals(background))
            return false;
        
        throw new RuntimeException("Can not determine auto refresh state"); //$NON-NLS-1$
    }
    
    public void refreshOnes() {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        Long finishedCalls = (Long) executor.executeScript(
                "return window.PENDING_AJAX_CALLS.numberOfFinishedCalls(arguments[0])", "loadLeaderboardData");
        System.out.println(finishedCalls.intValue());
        this.playAndPauseAnchor.click();
        
        waitForAjaxRequestsExecuted("loadLeaderboardData", finishedCalls.intValue() + 1);
        
        this.playAndPauseAnchor.click();
    }
    
    public CompetitorChartsSettingsDialogPO getCompetitorChartsSettings() {
        this.competitorChartsSettingsButton.click();
        
        return new CompetitorChartsSettingsDialogPO(this.driver,
                findElementBySeleniumId(this.driver, "CompetitorChartsSettingsDialog"));
    }
    
    public LeaderboardSettingsDialogPO getLeaderboardSettings() {
        this.leaderboardSettingsButton.click();
        
        return new LeaderboardSettingsDialogPO(this.driver,
                findElementBySeleniumId(this.driver, "LeaderboardSettingsDialog"));
    }
    
    public LeaderboardTablePO getLeaderboardTable() {
        return new LeaderboardTablePO(this.driver, this.leaderboardCellTable);
    }

    public void showCompetitorChart(String chart) {
        this.competitorChartsSettingsButton.click();
        CompetitorChartsSettingsDialogPO dialog = new CompetitorChartsSettingsDialogPO(this.driver,
                findElementBySeleniumId(this.driver, "CompetitorChartsSettingsDialog"));
        dialog.selectChartType(chart);
        
        CheckBoxPO checkbox = new CheckBoxPO(this.driver, this.competitorChartsDisplayCheckBox);
        checkbox.setSelected(true);
    }
    
    public void hideCompetitorChart() {
        CheckBoxPO checkbox = new CheckBoxPO(this.driver, this.competitorChartsDisplayCheckBox);
        checkbox.setSelected(false);
    }
    
    
    
//    public Chart getCompetitorChart() {
//        return new Chart(this.driver, findElementBySeleniumId("CompetitorChart"));
//    }

//    public void setAutoRefreshEnabled(boolean enabled) {
//        if(isAutoRefreshEnabled() != enabled)
//            this.autoRefreshToggleButton.click();
//    }
    
    @Override
    protected void initElements() {
        super.initElements();
        
        // Wait for the initial loading of the data
        waitForAjaxRequestsExecuted("loadLeaderboardData", 1);
    }
}
