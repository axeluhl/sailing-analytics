package com.sap.sailing.selenium.test.adminconsole.smartphonetracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class MapDevicesDialogPO extends DataEntryDialogPO {
    
    @FindBy(how = BySeleniumId.class, using = "addMappingButton")
    private WebElement addMappingButton;
    
    public MapDevicesDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public AddDeviceMappingsDialogPO addMapping() {
        addMappingButton.click();
        return this.waitForPO(AddDeviceMappingsDialogPO::new, "RegattaLogAddDeviceMappingDialog");
    }
    
}
