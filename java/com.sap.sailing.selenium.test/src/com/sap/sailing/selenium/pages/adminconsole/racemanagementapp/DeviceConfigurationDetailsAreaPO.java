package com.sap.sailing.selenium.pages.adminconsole.racemanagementapp;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class DeviceConfigurationDetailsAreaPO extends PageArea {

    public static final String DEVICE_CONFIGURATION_QR_CODE_DIALOG_ID = "DeviceConfigurationQRIdentifierDialog";

    @FindBy(how = BySeleniumId.class, using = "DeviceConfigurationDetailsQrCodeButton")
    private WebElement deviceConfigurationDetailsQrCodeButton;

    public DeviceConfigurationDetailsAreaPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public DeviceConfigurationQRCodeDialogPO openQRCodeDialog() {
        deviceConfigurationDetailsQrCodeButton.click();
        return getPO(DeviceConfigurationQRCodeDialogPO::new, DEVICE_CONFIGURATION_QR_CODE_DIALOG_ID);
    }

}
