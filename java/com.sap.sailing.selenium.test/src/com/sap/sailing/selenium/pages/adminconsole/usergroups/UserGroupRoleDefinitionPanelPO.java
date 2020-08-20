package com.sap.sailing.selenium.pages.adminconsole.usergroups;

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

public class UserGroupRoleDefinitionPanelPO extends PageArea {
    private static final String TABLE_ROLE_NAME_COLUMN = "Role Name";
    @FindBy(how = BySeleniumId.class, using = "AddGroupUserButton")
    private WebElement addRoleButton;
    @FindBy(how = BySeleniumId.class, using = "RoleSuggestion")
    private WebElement roleNameInput;
    @FindBy(how = BySeleniumId.class, using = "GroupRoleDefinitionDTOTable")
    private WebElement roleTable;
    
    public UserGroupRoleDefinitionPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getRoleTable() {
        return new GenericCellTablePO<>(this.driver, this.roleTable, DataEntryPO.class);
    }

    public DataEntryPO findRole(final String roleName) {
        final CellTablePO<DataEntryPO> table = getRoleTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent(TABLE_ROLE_NAME_COLUMN);
            if (roleName.equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public void addRole(String rolename) {
        enterNewRoleName(rolename);
        clickAddButtonOrThrow();
        waitUntil(() -> findRole(rolename) != null);
    }
    
    public void enterNewRoleName(String rolename) {
        SuggestBoxPO.create(driver, roleNameInput).appendText(rolename);
    }
    
    public void clickAddButtonOrThrow() {
        if (!addRoleButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addRoleButton.click();
        }
    }
    
    public void clickAddButtonAndExpectPermissionError() {
        if (!addRoleButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addRoleButton.click();
        }
        waitForAlertContainingMessageAndAccept("could not be added to group");
    }

    public void removeRole(String name) {
        // TODO: Identify and add DebugId to Delete Action Buttons.
        WebElement findElementBySeleniumId = findElementBySeleniumId("DeleteRoleButton-" + name);
        findElementBySeleniumId.click();
    }
}
