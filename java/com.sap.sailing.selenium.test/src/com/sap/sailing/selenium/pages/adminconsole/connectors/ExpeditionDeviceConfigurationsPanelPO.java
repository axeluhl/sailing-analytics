package com.sap.sailing.selenium.pages.adminconsole.connectors;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class ExpeditionDeviceConfigurationsPanelPO extends PageArea {
    public static class ExpeditionDeviceConfigurationEntryPO extends DataEntryPO {

        public ExpeditionDeviceConfigurationEntryPO(CellTablePO<?> table, WebElement element) {
            super(table, element);
        }
        
        protected ExpeditionDeviceConfigurationEntryPO() {
            super();
        }
        
        @Override
        public String getIdentifier() {
            return getName();
        }
        
        public String getName() {
            return getColumnContent("Name");
        }
        
        public String getId() {
            return getColumnContent("ID");
        }
        
        public String getBoatId() {
            return getColumnContent("Boat ID (e.g. 0, 1, or 2)");
        }
    }
    
    @FindBy(how = BySeleniumId.class, using = "ExpeditionDeviceConfigurationsTable")
    private WebElement expeditionDeviceConfigurationTable;
    
    @FindBy(how = BySeleniumId.class, using = "addExpeditionDeviceConfiguration")
    private WebElement addExpeditionDeviceConfigurationButton;
    
    public ExpeditionDeviceConfigurationsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public ExpeditionDeviceConfigurationCreateDialogPO startCreatingExpeditionDeviceConfiguration() {
        this.addExpeditionDeviceConfigurationButton.click();
        // Wait, since we trigger an AJAX-request to get the available events
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "AddExpeditionDeviceConfigurationDialog");
        return new ExpeditionDeviceConfigurationCreateDialogPO(this.driver, dialog);
    }
    
    public void createExpeditionDeviceConfiguration(String name, String id) {
        ExpeditionDeviceConfigurationCreateDialogPO dialog = startCreatingExpeditionDeviceConfiguration();
        dialog.setName(name);
        dialog.setExpeditionBoatId(id);
        dialog.pressOk();
    }
    
    public List<String> getAvailableExpeditionDeviceConfigurations() {
        List<String> configurations = new ArrayList<>();
        CellTablePO<ExpeditionDeviceConfigurationEntryPO> table = getExpeditionDeviceConfigurationsTable();
        List<ExpeditionDeviceConfigurationEntryPO> entries = table.getEntries();
        for (ExpeditionDeviceConfigurationEntryPO entry : entries) {
            configurations.add(entry.getIdentifier());
        }
        return configurations;
    }
    
    public List<ExpeditionDeviceConfigurationEntryPO> getSelectedEntries() {
        return getExpeditionDeviceConfigurationsTable().getSelectedEntries();
    }
    
    public CellTablePO<ExpeditionDeviceConfigurationEntryPO> getExpeditionDeviceConfigurationsTable() {
        return new GenericCellTablePO<>(this.driver, this.expeditionDeviceConfigurationTable, ExpeditionDeviceConfigurationEntryPO.class);
    }
    
}
