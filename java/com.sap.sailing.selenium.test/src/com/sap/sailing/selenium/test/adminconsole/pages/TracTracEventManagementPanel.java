package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;




//import org.openqa.selenium.support.FindBy;
//import org.openqa.selenium.support.How;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;

/**
 * <p>The page object representing the TracTrac Events tab.</p>
 * 
 * @author
 *   D049941
 */
public class TracTracEventManagementPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "JSONURL")
    private WebElement jsonURLField;

    @FindBy(how = BySeleniumId.class, using = "LiveURI")
    private WebElement liveURIField;

    @FindBy(how = BySeleniumId.class, using = "StoredURI")
    private WebElement storedURIField;
    
    @FindBy(how = BySeleniumId.class, using = "ListRaces")
    private WebElement listRacesButton;
    
    @FindBy(how = BySeleniumId.class, using = "TrackableRacesSection")
    private WebElement startTrackingPanel;

    @FindBy(how = BySeleniumId.class, using = "TrackedRaces")
    private WebElement trackedRacesPanel;


//    private WebElement trackWindCheckBox;
//    private WebElement correctWindCheckbox;
//    private WebElement simulateWithNowCheckbox;
    
    /**
     * <p></p>
     * 
     * @param driver
     *   
     * @param element
     *   
     */
    protected TracTracEventManagementPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    /**
     * <p>Lists all available trackable races for the given URL. The list of the races can be obtained via
     *   {@link #getTrackableRaces()}.</p>
     * 
     * @param url
     *   The URL for which the races are to list.
     */
    public void listRaces(String url) {
        liveURIField.clear();
        storedURIField.clear();
        jsonURLField.clear();
        jsonURLField.sendKeys(url);
        listRacesButton.click();
        waitForAjaxRequests();
    }
    
    public TracTracStartTrackingPanel getStartTrackingPanel() {
        return new TracTracStartTrackingPanel(driver, startTrackingPanel);
    }

    /**
     * <p>Returns the list of all available trackable races. This list will be empty if no race is available or if no
     *   race was specified before.</p>
     * 
     * @return
     *   The list of all available trackable races.
     */
    public List<WebElement> getTrackableRaces() {
        return getStartTrackingPanel().getTrackableRaces();
    }
    
    /**
     * <p>Sets the filter for the trackable races. After the filter is set you can obtain the new resulting list via
     *   {@link #getTrackableRaces}</p>
     * 
     * @param filter
     *   The filter to apply to the trackable races.
     */
    public void setFilterForTrackableRaces(String filter) {
        getStartTrackingPanel().setFilterForTrackableRaces(filter);
    }
    
    public void startTracking(String regattaName, String raceName) {
        getStartTrackingPanel().startTracking(regattaName, raceName);
    }
    
    public void waitForTrackedRaceLoadingFinished(String regattaName, String raceName, long timeoutInMillis) throws InterruptedException {
        waitForAjaxRequests(); // wait for the Start Tracking request to succeed; then check Tracked races table and keep refreshing until we time out
        TrackedRacesPanel trp = getTrackedRacesPanel();
        long started = System.currentTimeMillis();
        WebElement raceRow = trp.getTrackedRace(regattaName, raceName);
        while ((raceRow == null || !"TRACKING".equals(raceRow.findElements(By.tagName("td")).get(6).getText()))
                && System.currentTimeMillis()-started < timeoutInMillis) {
            Thread.sleep(2000); // wait 2s for the race to appear
            trp.refresh();
            raceRow = trp.getTrackedRace(regattaName, raceName);
        }
    }

    private TrackedRacesPanel getTrackedRacesPanel() {
        return new TrackedRacesPanel(driver, trackedRacesPanel);
    }

    public void stopTracking(String regattaName, String raceName) {
        final TrackedRacesPanel trp = getTrackedRacesPanel();
        trp.getTrackedRace(regattaName, raceName).click();
        trp.stopTracking();
    }
}
