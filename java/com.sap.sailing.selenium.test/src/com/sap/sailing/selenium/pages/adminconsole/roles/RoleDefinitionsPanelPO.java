package com.sap.sailing.selenium.pages.adminconsole.roles;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By.ByName;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.security.DataEntryWithSecurityActionsPO;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class RoleDefinitionsPanelPO extends PageArea {
    
    public static class RoleEntryPO extends DataEntryWithSecurityActionsPO {

        @FindBy(how = ByName.class, using = "UPDATE")
        private WebElement updateButton;

        public RoleEntryPO(CellTablePO<?> table, WebElement element) {
            super(table, element);
        }
        
        public String getEventURL() {
            return getWebElement().findElement(By.xpath(".//td/div/a")).getAttribute("href");
        }
        
        public RoleDefinitionCreationAndUpdateDialogPO openUpdateDialog() {
            updateButton.click();
            return waitForPO(RoleDefinitionCreationAndUpdateDialogPO::new, "RoleDefinitionEditDialog");
        }
    }
    
    private static final String CREATE_ROLE_DIALOG = "RoleDefinitionCreationDialog";
    
    @FindBy(how = BySeleniumId.class, using = "RolesCellTable")
    private WebElement roleTable;
    
    @FindBy(how = BySeleniumId.class, using = "CreateRoleButton")
    private WebElement createRoleButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRoleButton")
    private WebElement removeRoleButton;
    
    public RoleDefinitionsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<RoleEntryPO> getRoleTable() {
        return new GenericCellTablePO<>(this.driver, this.roleTable, RoleEntryPO.class);
    }

    public RoleEntryPO findRole(final String roleName) {
        final CellTablePO<RoleEntryPO> table = getRoleTable();
        for (RoleEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent("Name");
            if (roleName.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public RoleDefinitionCreationAndUpdateDialogPO getCreateRoleDialog() {
        createRoleButton.click();
        return waitForPO(RoleDefinitionCreationAndUpdateDialogPO::new, CREATE_ROLE_DIALOG);
    }
}
