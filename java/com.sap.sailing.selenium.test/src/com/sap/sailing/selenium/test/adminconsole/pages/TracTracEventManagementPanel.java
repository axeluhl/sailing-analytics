package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

//import org.openqa.selenium.support.FindBy;
//import org.openqa.selenium.support.How;

import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.core.BySeleniumId;

import com.sap.sailing.selenium.test.PageObject;

public class TracTracEventManagementPanel extends PageObject {
    //@FindBy(how = How.XPATH, using = ".//*[@selenium-id='JSONURL']")
    @FindBy(how = BySeleniumId.class, using = "JSONURL")
    private WebElement jsonURLField;
    
    //@FindBy(how = How.XPATH, using = ".//*[@selenium-id='ListRaces']")
    @FindBy(how = BySeleniumId.class, using = "ListRaces")
    private WebElement listRacesButton;
    
    //@FindBy(how = How.XPATH, using = ".//*[@selenium-id='FilterRaces']")
    @FindBy(how = BySeleniumId.class, using = "FilterRaces")
    private WebElement filterTrackableRacesField;
    
//    private WebElement trackWindCheckBox;
//    private WebElement correctWindCheckbox;
//    private WebElement simulateWithNowCheckbox;
    
    public TracTracEventManagementPanel(WebDriver driver, SearchContext context) {
        super(driver, context);
    }
    
    public void listRaces(String url) {
        this.jsonURLField.clear();
        this.jsonURLField.sendKeys(url);
        this.listRacesButton.click();
        
        waitForAjaxRequests();
    }
    
    public List<WebElement> getTrackableRaces() {
        WebElement availableRacesTabel = findElementBySeleniumId(this.context, "RacesTable");
        List<WebElement> elements = availableRacesTabel.findElements(By.xpath("./tbody/tr"));
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
