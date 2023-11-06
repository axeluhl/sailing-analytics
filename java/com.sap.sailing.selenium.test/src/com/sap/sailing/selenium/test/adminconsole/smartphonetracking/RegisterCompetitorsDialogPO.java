package com.sap.sailing.selenium.test.adminconsole.smartphonetracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class RegisterCompetitorsDialogPO extends DataEntryDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "registerCompetitorsDialog")
    private WebElement dialog;
    
    @FindBy(how = BySeleniumId.class, using = "addCompetitorWithBoatButton")
    private WebElement addCompetitorWithBoatButton;
    
    public RegisterCompetitorsDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
        
    }
    
    public AddCompetitorWithBoatDialogPO openAddCompetitorWithBoatDialog() {
        this.addCompetitorWithBoatButton.click();
        return getPO(AddCompetitorWithBoatDialogPO::new, "CompetitorWithBoatEditDialog");
    }
    
    
}
