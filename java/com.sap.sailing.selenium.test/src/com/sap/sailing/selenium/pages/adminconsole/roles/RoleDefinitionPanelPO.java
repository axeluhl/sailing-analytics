package com.sap.sailing.selenium.pages.adminconsole.roles;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class RoleDefinitionPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "RoleTable")
    private WebElement roleTable;
    @FindBy(how = BySeleniumId.class, using = "CreateRoleButton")
    private WebElement createRoleButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRoleButton")
    private WebElement removeRoleButton;
    
    public RoleDefinitionPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getRoleTable() {
        return new GenericCellTablePO<>(this.driver, this.roleTable, DataEntryPO.class);
    }

    public DataEntryPO findRole(final String username) {
        final CellTablePO<DataEntryPO> table = getRoleTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent("Name");
            if (username.equals(name)) {
                return entry;
            }
        }
        return null;
    }
    
}
