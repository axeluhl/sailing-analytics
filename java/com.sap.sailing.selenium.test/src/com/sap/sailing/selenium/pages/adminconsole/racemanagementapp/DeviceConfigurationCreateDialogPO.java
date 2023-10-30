package com.sap.sailing.selenium.pages.adminconsole.racemanagementapp;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class DeviceConfigurationCreateDialogPO  extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "raceManagerDeviceName")
    private WebElement nameField;

    protected DeviceConfigurationCreateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void setDeviceName(String name) {
        nameField.sendKeys(name);
    }

}
