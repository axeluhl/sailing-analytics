package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;
import com.sap.sailing.selenium.pages.gwt.SuggestBoxPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class UserRoleDefinitionPanelPO extends PageArea {
    private static final String TABLE_ROLE_NAME_COLUMN = "Role Name";
    @FindBy(how = BySeleniumId.class, using = "addRoleButton")
    private WebElement addRoleButton;
    @FindBy(how = BySeleniumId.class, using = "suggestRole")
    private WebElement roleNameInput;
    @FindBy(how = BySeleniumId.class, using = "tenantInput")
    private WebElement tenantInput;
    @FindBy(how = BySeleniumId.class, using = "userInput")
    private WebElement userInput;
    @FindBy(how = BySeleniumId.class, using = "RoleWithSecurityDTOTable")
    private WebElement roleTable;
    
    public UserRoleDefinitionPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserTable() {
        return new GenericCellTablePO<>(this.driver, this.roleTable, DataEntryPO.class);
    }

    public DataEntryPO findRole(final String roleName) {
        final CellTablePO<DataEntryPO> table = getUserTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent(TABLE_ROLE_NAME_COLUMN);
            if (roleName.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public void enterNewRoleValues(String rolename, String groupname, String username) {
        SuggestBoxPO.create(driver, roleNameInput).appendText(rolename);
        TextBoxPO.create(driver, tenantInput).appendText(groupname);
        TextBoxPO.create(driver, userInput).appendText(username);
    }
    
    public void clickAddButtonOrThrow() {
        if (!addRoleButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addRoleButton.click();
        }
    }
}
