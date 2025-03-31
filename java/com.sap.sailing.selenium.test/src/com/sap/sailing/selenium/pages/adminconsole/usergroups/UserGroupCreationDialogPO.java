package com.sap.sailing.selenium.pages.adminconsole.usergroups;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class UserGroupCreationDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "GroupNameInput")
    private WebElement groupNameInput;

    protected UserGroupCreationDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        TextBoxPO.create(driver, groupNameInput).appendText(name);
    }
}
