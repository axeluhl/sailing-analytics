package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;
import com.sap.sailing.selenium.pages.common.DataEntryDialog;

public class RegattaLeaderboardCreateDialog extends DataEntryDialog {

    @FindBy(how = BySeleniumId.class, using = "LeaderboardNameField")
    private WebElement nameField;

    @FindBy(how = BySeleniumId.class, using = "RegattaDropDown")
    private WebElement regattaDropDown;
    
    public RegattaLeaderboardCreateDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void selectRegatta(RegattaDescriptor regatta) {
        Select dropDown = new Select(this.regattaDropDown);
        dropDown.selectByValue(regatta.toString());
    }
    
}
