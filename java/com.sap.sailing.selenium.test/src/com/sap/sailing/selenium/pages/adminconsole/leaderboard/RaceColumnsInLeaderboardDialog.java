package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class RaceColumnsInLeaderboardDialog extends DataEntryDialogPO {
    @FindBy(how = BySeleniumId.class, using = "NumberOfRacesListBox")
    private WebElement numberOfRacesListBox;
    
    @FindBy(how = BySeleniumId.class, using = "RaceNamePrefixTextBox")
    private WebElement raceNamePrefixTextField;

    @FindBy(how = BySeleniumId.class, using = "AddRacesButton")
    private WebElement addRacesButton;

    public RaceColumnsInLeaderboardDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public int getSetNumberOfRacesToAdd() {
        Select select = new Select(this.numberOfRacesListBox);
        WebElement option = select.getFirstSelectedOption();
        
        return Integer.parseInt(option.getAttribute("value"));
    }
    
    public void setSetNumberOfRacesToAdd(int races) {
        Select select = new Select(this.numberOfRacesListBox);
        select.selectByValue(Integer.toString(races));
    }
    
    public String getRaceNamePrefix() {
        return this.raceNamePrefixTextField.getText();
    }
    
    public void setRaceNamePrefix(String prefix) {
        this.raceNamePrefixTextField.clear();
        this.raceNamePrefixTextField.sendKeys(prefix);
    }
    
    public void pressAddRaces() {
        this.addRacesButton.click();
    }
    
    public void addRaces(int races) {
        addRaces(races, getRaceNamePrefix());
    }
    
    public void addRaces(int races, String prefix) {
        Select select = new Select(this.numberOfRacesListBox);
        select.selectByValue(Integer.toString(races));
        setRaceNamePrefix(prefix);
        this.addRacesButton.click();
        pressOk();
    }
}
