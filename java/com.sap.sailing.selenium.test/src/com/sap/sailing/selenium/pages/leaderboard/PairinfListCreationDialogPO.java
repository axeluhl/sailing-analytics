package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class PairinfListCreationDialogPO extends PageArea {

    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement closeButton;
    
    @FindBy(how = BySeleniumId.class, using = "FlightCountLabel")
    private WebElement flightsLabel;
    
    @FindBy(how = BySeleniumId.class, using = "GroupCountLabel")
    private WebElement groupsLabel;
    
    @FindBy(how = BySeleniumId.class, using = "CompetitorCountLabel")
    private WebElement competitorsLabel;
    
    @FindBy(how = BySeleniumId.class, using = "FlightMultiplierCountLabel")
    private WebElement multiplierLabel;
    
    protected PairinfListCreationDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void pressClose(){
        closeButton.click();
    }
    
    public String getValueOfFlightsLabel(){
        return flightsLabel.getText();
    }
    public String getValueOfGroupsLabel(){
        return groupsLabel.getText();
    }
    public String getValueOfCompetitorsLabel(){
        return competitorsLabel.getText();
    }
    public String getValueOfMultiplerLabel(){
        return multiplierLabel.getText();
    }
}
