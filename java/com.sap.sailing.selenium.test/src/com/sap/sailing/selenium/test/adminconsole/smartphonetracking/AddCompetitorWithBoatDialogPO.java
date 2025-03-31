package com.sap.sailing.selenium.test.adminconsole.smartphonetracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class AddCompetitorWithBoatDialogPO extends DataEntryDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "NameTextBox")
    private WebElement nameTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "SailIdTextBox")
    private WebElement sailIdTextBox;
    
    public AddCompetitorWithBoatDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void addCompetitorWithBoat() {
        nameTextBox.sendKeys("Competitor Test");
        sailIdTextBox.sendKeys("1234");
        clickOkButtonOrThrow();
    }
    
    
}
