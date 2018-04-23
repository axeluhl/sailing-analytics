package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CheckBoxPO;

public class PairingListCreationSetupDialogPO extends PageArea {
    
    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorCountBox")
    private WebElement competitorsIntegerBox;
    
    @FindBy(how = BySeleniumId.class, using = "FlightMultiplierIntegerBox")
    private WebElement multiplierIntegerBox;
    
    @FindBy(how = BySeleniumId.class, using = "FlightMultiplierCheckBox")
    private WebElement multiplierCheckBox;
    
    @FindBy(how = BySeleniumId.class, using = "SelectedFlightsCheckbox: Quali")
    private WebElement flightsCheckbox;
    
    private PairingListCreationSetupDialogPO pairingListCreationSetupDialogPO;
    
    public PairingListCreationSetupDialogPO(WebDriver webDriver, WebElement webElement){
        super(webDriver,webElement);
        }
    public PairingListCreationSetupDialogPO getPairingListCreationDialogPO() {
        return this.pairingListCreationSetupDialogPO;
    }
    public void setCompetitorsCount(String count){
        competitorsIntegerBox.clear();
        competitorsIntegerBox.sendKeys(count);
    }
    public PairinfListCreationDialogPO pressOk(){
        okButton.click();
        return waitForPO(PairinfListCreationDialogPO::new, "PairingListCreationDialog", 60);
    }
    public boolean isOkButtonEnabled(){
        return okButton.isEnabled();
    }
    public boolean isFlightMultiplierBoxEnabled(){
        return multiplierIntegerBox.isEnabled();
    }
    public void setFlightMultiplier(String count){
        multiplierIntegerBox.clear();
        multiplierIntegerBox.sendKeys(count);
    }
    public void clickFlightMultiplierCheckBox() {
        CheckBoxPO.create(driver, multiplierCheckBox).setSelected(!this.getValueOfFlightMultiplierCheckBox());
    }
    public void clickFlightCheckBox() {
        CheckBoxPO.create(driver, flightsCheckbox).setSelected(!this.getValueOfFlightCheckBox());
    }
    public boolean getValueOfFlightCheckBox(){
        return CheckBoxPO.create(driver, flightsCheckbox).isSelected();
    }
    public boolean getValueOfFlightMultiplierCheckBox(){
        return CheckBoxPO.create(driver, multiplierCheckBox).isSelected();
    }
}
