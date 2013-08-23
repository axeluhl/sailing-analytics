package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.common.DataEntryDialog;

public class RegattaCreationDialog extends DataEntryDialog {
    @FindBy(how = BySeleniumId.class, using = "RegattaNameTextField")
    private WebElement regattaNameTextField;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassTextField")
    private WebElement boatClassTextField;
    
//    @FindBy(how = BySeleniumId.class, using = "ScoringSystemDropDown")
//    private WebElement scoringSystemDropDown;
//    @FindBy(how = BySeleniumId.class, using = "EventDropDown")
//    private WebElement eventDropDown;
//    @FindBy(how = BySeleniumId.class, using = "CourseAreaDropDown")
//    private WebElement courseAreaDropDown;
//    @FindBy(how = BySeleniumId.class, using = "AddSeriesButton")
//    private WebElement addSeriesButton;
    
    public RegattaCreationDialog(WebDriver driver, WebElement element) {
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
}
