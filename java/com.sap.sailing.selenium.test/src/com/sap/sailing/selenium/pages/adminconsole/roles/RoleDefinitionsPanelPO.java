package com.sap.sailing.selenium.pages.adminconsole.roles;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class RoleDefinitionsPanelPO extends PageArea {
    private static final String CREATE_ROLE_DIALOG = "RoleDefinitionCreationDialog";
    
    @FindBy(how = BySeleniumId.class, using = "RoleTable")
    private WebElement roleTable;
    @FindBy(how = BySeleniumId.class, using = "CreateRoleButton")
    private WebElement createRoleButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRoleButton")
    private WebElement removeRoleButton;
    
    public RoleDefinitionsPanelPO(WebDriver driver, WebElement element) {
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
    public RoleDefinitionCreationDialogPO getCreateRoleDialog() {
        createRoleButton.click();
        final WebElement dialog = findElementBySeleniumId(this.driver, CREATE_ROLE_DIALOG);
        return new RoleDefinitionCreationDialogPO(this.driver, dialog);
    }
}
