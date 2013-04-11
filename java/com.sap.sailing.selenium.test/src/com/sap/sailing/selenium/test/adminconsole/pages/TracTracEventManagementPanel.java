package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

//import org.openqa.selenium.support.FindBy;
//import org.openqa.selenium.support.How;

import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.core.BySeleniumId;

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
    
    @FindBy(how = BySeleniumId.class, using = "ListRaces")
    private WebElement listRacesButton;
    
    @FindBy(how = BySeleniumId.class, using = "FilterRaces")
    private WebElement filterTrackableRacesField;
    
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
        this.jsonURLField.clear();
        this.jsonURLField.sendKeys(url);
        this.listRacesButton.click();
        
        waitForAjaxRequests();
    }
    
    /**
     * <p>Returns the list of all available trackable races. This list will be empty if no race is available or if no
     *   race was specified before.</p>
     * 
     * @return
     *   The list of all available trackable races.
     */
    // QUESTION: Should we return something different instead of WebElements since it is not recommended to do so?
    public List<WebElement> getTrackableRaces() {
        WebElement availableRacesTabel = findElementBySeleniumId(this.context, "RacesTable"); //$NON-NLS-1$
        List<WebElement> elements = availableRacesTabel.findElements(By.xpath("./tbody/tr")); //$NON-NLS-1$
        Iterator<WebElement> iterator = elements.iterator();
        
        while(iterator.hasNext()) {
            WebElement element = iterator.next();
            
            if(!element.isDisplayed())
                iterator.remove();
        }
        
        return elements;
    }
    
//    public List<String> getAvailableReggatasForTracking() {
//        
//    }
//    
//    public String getReggataForTracking() {
//        
//    }
//    
//    public void setReggataForTracking(String regatta) {
//        
//    }
    
    /**
     * <p>Sets the filter for the trackable races. After the filter is set you can obtain the new resulting list via
     *   {@link #getTrackableRaces}</p>
     * 
     * @param filter
     *   The filter to apply to the trackable races.
     */
    public void setFilterForTrackableRaces(String filter) {
        this.filterTrackableRacesField.clear();
        this.filterTrackableRacesField.sendKeys(filter);
    }
    
//    public void setTrackSettings(boolean trackWind, boolean correctWind, boolean simulateWithNow) {
//        setSelection(this.trackWindCheckBox, trackWind);
//        setSelection(this.correctWindCheckbox, correctWind);
//        setSelection(this.simulateWithNowCheckbox, simulateWithNow);
//    }
//    
//    public void startTrackingForRace() {
//        
//    }
//    
//    public void startTrackingForRaces() {
//        
//    }
//    
//    private void setSelection(WebElement checkbox, boolean selected) {
//        WebElement input = checkbox.findElement(By.tagName("input"));
//        if(input.isSelected() != selected)
//            input.click();
//    }
}
