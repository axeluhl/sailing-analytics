package com.sap.sailing.selenium.pages.adminconsole.usergroups;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class UserGroupManagementPanelPO extends PageArea {
    private static final String CREATE_ROLE_DIALOG = "CreateUserGroupDialog";
    
    @FindBy(how = BySeleniumId.class, using = "UserGroupWithSecurityDTOTable")
    private WebElement groupTable;
    
    @FindBy(how = BySeleniumId.class, using = "CreateGroupButton")
    private WebElement createGroupButton;
    
    @FindBy(how = BySeleniumId.class, using = "DeleteUserGroupButton")
    private WebElement deleteUserGroupButton;
    
    public UserGroupManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserGroupTable() {
        return new GenericCellTablePO<>(this.driver, this.groupTable, DataEntryPO.class);
    }

    public DataEntryPO findGroup(final String username) {
        final CellTablePO<DataEntryPO> table = getUserGroupTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent("Group Name");
            if (username.equals(name)) {
                return entry;
            }
        }
        return null;
    }
    public UserGroupCreationDialogPO getCreateGroupDialog() {
        createGroupButton.click();
        final WebElement dialog = findElementBySeleniumId(this.driver, CREATE_ROLE_DIALOG);
        return new UserGroupCreationDialogPO(this.driver, dialog);
    }
    
    public void selectGroup(String name) {
        final CellTablePO<DataEntryPO> table = getUserGroupTable();
        final DataEntryPO findUser = findGroup(name);
        if(findUser != null) {
            table.selectEntry(findUser);
        }
    }
    
    public UserGroupRoleDefinitionPanelPO getUserGroupRoles() {
        return waitForChildPO(UserGroupRoleDefinitionPanelPO::new, "GroupRoleDefinitionPanel");
    }
    
    public UserGroupUserPanelPO getUserGroupUsers() {
        return waitForChildPO(UserGroupUserPanelPO::new, "UserGroupDetailPanel");
    }
    
    public void deleteGroup(String name) {
        selectGroup(name);
        deleteSelectedGroup();
    }
    
    public void deleteSelectedGroup() {
        deleteUserGroupButton.click();
    }
}
