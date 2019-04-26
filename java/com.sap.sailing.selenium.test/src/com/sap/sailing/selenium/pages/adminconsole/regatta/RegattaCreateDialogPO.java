package com.sap.sailing.selenium.pages.adminconsole.regatta;

import java.util.Date;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.DateAndTimeInputPO;
import com.sap.sailing.selenium.pages.gwt.ListBoxPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class RegattaCreateDialogPO extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassTextBox")
    private WebElement boatClassTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "StartDateTimeBox")
    private WebElement startDateTimeBox;
    
    @FindBy(how = BySeleniumId.class, using = "EndDateTimeBox")
    private WebElement endDateTimeBox;
    
//    @FindBy(how = BySeleniumId.class, using = "ScoringSchemeListBox")
//    private WebElement scoringSystemDropDown;
    @FindBy(how = BySeleniumId.class, using = "EventListBox")
    private WebElement eventDropDown;
    @FindBy(how = BySeleniumId.class, using = "CourseAreaListBox")
    private WebElement courseAreaDropDown;
    
    @FindBy(how = BySeleniumId.class, using = "AddSeriesButton")
    private WebElement addSeriesButton;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorRegistrationTypeListBox")
    private WebElement competitorRegistrationTypeListBox;
    
    public RegattaCreateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setRegattaName(String name) {
        TextBoxPO.create(driver, nameTextBox).setText(name);
    }
    
    public void setBoatClass(String boatClass) {
        this.boatClassTextBox.clear();
        this.boatClassTextBox.sendKeys(boatClass);
    }
    
    public void setCompetitorRegistrationType(CompetitorRegistrationType competitorRegistrationType) {
        if (competitorRegistrationTypeListBox.isEnabled()) {
            new Select(competitorRegistrationTypeListBox).selectByIndex(competitorRegistrationType.ordinal());
        }
    }
    
    public void setEventAndCourseArea(String event, String courseArea) {
        ListBoxPO.create(driver, eventDropDown).selectOptionByLabel(event);
        ListBoxPO.create(driver, courseAreaDropDown).selectOptionByLabel(courseArea);
    }
    
    public void setValues(String name, String boatClass, Date startDate, Date endDate) {
        this.setRegattaName(name);
        this.setBoatClass(boatClass);
        DateAndTimeInputPO.create(driver, startDateTimeBox).setValue(startDate, false);
        DateAndTimeInputPO.create(driver, endDateTimeBox).setValue(endDate, false);
    }
    
    // TODO: Scoring System, Event and Course Area
    
    public SeriesCreateDialogPO addSeries() {
        this.addSeriesButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "SeriesCreateDialog");
        
        return new SeriesCreateDialogPO(this.driver, dialog);
    }
}
