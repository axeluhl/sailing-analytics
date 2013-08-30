package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class SeriesEditDialog extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "MedalSeriesCheckbox")
    private WebElement medalSeriesCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "StartsWithZeroScoreCheckbox")
    private WebElement startsWithZeroScoreCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "StartsWithNonDiscardableCarryForwardCheckbox")
    private WebElement startsWithNonDiscardableCarryForwardCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "DefinesResultDiscardingRulesCheckbox")
    private WebElement definesResultDiscardingRulesCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "NumberOfRacesDropDown")
    private WebElement numberOfRacesDropDown;
    
    @FindBy(how = BySeleniumId.class, using = "RaceNamePrefixTextField")
    private WebElement raceNamePrefixTextField;
    
    @FindBy(how = BySeleniumId.class, using = "AddRacesButton")
    private WebElement addRacesButton;
    
    @FindBy(how = BySeleniumId.class, using = "RacesPanel")
    private WebElement racesPanel;
    
    public SeriesEditDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public int getSetNumberOfRacesToAdd() {
        Select select = new Select(this.numberOfRacesDropDown);
        WebElement option = select.getFirstSelectedOption();
        
        return Integer.parseInt(option.getAttribute("value"));
    }
    
    public void setSetNumberOfRacesToAdd(int races) {
        Select select = new Select(this.numberOfRacesDropDown);
        select.selectByValue(Integer.toString(races));
    }
    
    public String getRaceNamePrefix() {
        return this.raceNamePrefixTextField.getText();
    }
    
    public void setRaceNamePrefix(String prefix) {
        this.raceNamePrefixTextField.clear();
        this.raceNamePrefixTextField.sendKeys(prefix);
    }
    
    public void addRaces() {
        this.addRacesButton.click();
    }
    
    public void addRaces(int races) {
        setSetNumberOfRacesToAdd(races);
        addRaces();
    }
    
    public void addRaces(int races, String prefix) {
        setSetNumberOfRacesToAdd(races);
        setRaceNamePrefix(prefix);
        addRaces();
    }
}
