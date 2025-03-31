package com.sap.sailing.selenium.pages.adminconsole.racemanagementapp;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class DeviceConfigurationQRCodeDialogPO  extends DataEntryDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "QRIdentifierURL")
    private WebElement url;

    protected DeviceConfigurationQRCodeDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public String getUrl() {
        return url.getText();
    }

}
