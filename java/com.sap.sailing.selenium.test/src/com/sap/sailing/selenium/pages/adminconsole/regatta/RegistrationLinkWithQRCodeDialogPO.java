package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class RegistrationLinkWithQRCodeDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "SecretTextBox")
    private WebElement secretTextBox;

    @FindBy(how = BySeleniumId.class, using = "GenerateSecretButton")
    private WebElement generateSecretButton;

    @FindBy(how = BySeleniumId.class, using = "RegistrationLinkUrl")
    private WebElement registrationLinkUrl;
    
    protected RegistrationLinkWithQRCodeDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public String getRegistrationLinkUrl() {
        return TextBoxPO.create(driver, registrationLinkUrl).getValue();
    }

}
