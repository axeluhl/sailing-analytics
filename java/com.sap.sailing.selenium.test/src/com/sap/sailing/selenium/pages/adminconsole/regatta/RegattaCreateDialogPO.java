package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class RegattaCreateDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "RegattaNameTextField")
    private WebElement regattaNameTextField;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassTextField")
    private WebElement boatClassTextField;
    
//    @FindBy(how = BySeleniumId.class, using = "ScoringSchemeListBox")
//    private WebElement scoringSystemDropDown;
//    @FindBy(how = BySeleniumId.class, using = "EventListBox")
//    private WebElement eventDropDown;
//    @FindBy(how = BySeleniumId.class, using = "CourseAreaListBox")
//    private WebElement courseAreaDropDown;
    
    @FindBy(how = BySeleniumId.class, using = "AddSeriesButton")
    private WebElement addSeriesButton;
    
    public RegattaCreateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setRegattaName(String name) {
        this.regattaNameTextField.clear();
        this.regattaNameTextField.sendKeys(name);
    }
    
    public void setBoatClass(String boatClass) {
        this.boatClassTextField.clear();
        this.boatClassTextField.sendKeys(boatClass);
    }
    
    // TODO: Scoring System, Event and Course Area
    
    public SeriesCreateDialogPO addSeries() {
        this.addSeriesButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "SeriesCreateDialog");
        
        return new SeriesCreateDialogPO(this.driver, dialog);
    }
}
