package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.PasswordTextBoxPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class CreateUserDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "username")
    private WebElement usernameInput;
    @FindBy(how = BySeleniumId.class, using = "eMail")
    private WebElement mailInput;
    @FindBy(how = BySeleniumId.class, using = "pw")
    private WebElement passwordInput;
    @FindBy(how = BySeleniumId.class, using = "pwrepeat")
    private WebElement repeatPasswordInput;

    protected CreateUserDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setValues(String name, String mail, String password, String repeatedPassword) {
        TextBoxPO.create(driver, usernameInput).appendText(name);
        TextBoxPO.create(driver, mailInput).appendText(mail);
        PasswordTextBoxPO.create(driver, passwordInput).appendText(password);
        PasswordTextBoxPO.create(driver, repeatPasswordInput).appendText(repeatedPassword);
    }
}
