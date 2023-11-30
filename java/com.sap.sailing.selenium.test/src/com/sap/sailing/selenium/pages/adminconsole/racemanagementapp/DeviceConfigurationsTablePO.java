package com.sap.sailing.selenium.pages.adminconsole.racemanagementapp;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class DeviceConfigurationsTablePO extends GenericCellTablePO<DeviceConfigurationsTablePO.DeviceConfigurationEntryPO> {

    public static class DeviceConfigurationEntryPO extends DataEntryPO {
        
        public DeviceConfigurationEntryPO(CellTablePO<?> table, WebElement element) {
            super(table, element);
        }
        
        @Override
        public Object getIdentifier() {
            return getColumnContent("Device");
        }
        
    }
    
    public DeviceConfigurationsTablePO(WebDriver driver, WebElement element) {
        super(driver, element, DeviceConfigurationEntryPO.class);
    }

    @Override
    protected DeviceConfigurationEntryPO createDataEntry(WebElement element) {
        return new DeviceConfigurationEntryPO(this, element);
    }

}
