package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.PasswordTextBoxPO;

public class ChangePasswordDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "pw")
    private WebElement textBoxPw;

    @FindBy(how = BySeleniumId.class, using = "pwrepeat")
    private WebElement textBoxPwRepeat;

    @FindBy(how = BySeleniumId.class, using = "OkButton")
    private WebElement okButton;

    protected ChangePasswordDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setNewPassword(String newPassword) {
        PasswordTextBoxPO.create(driver, textBoxPw).appendText(newPassword);
        PasswordTextBoxPO.create(driver, textBoxPwRepeat).appendText(newPassword);
    }

    public void clickOkButtonOrThrow() {
        if (!okButton.isEnabled()) {
            throw new ElementNotSelectableException("OK Button was disabled");
        } else {
            okButton.click();
        }
    }

}
