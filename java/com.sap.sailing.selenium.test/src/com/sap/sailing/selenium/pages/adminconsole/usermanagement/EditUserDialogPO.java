package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class EditUserDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "ChangePasswordButton")
    private WebElement changePasswordButton;

    protected EditUserDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void clickChangePasswordButton() {
        changePasswordButton.click();
    }

}
