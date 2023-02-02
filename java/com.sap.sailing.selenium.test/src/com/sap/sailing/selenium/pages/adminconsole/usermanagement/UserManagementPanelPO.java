package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.ActionsHelper;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class UserManagementPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "UsersTable")
    private WebElement userTable;
    @FindBy(how = BySeleniumId.class, using = "CreateUserButton")
    private WebElement createUserButton;
    
    @FindBy(how = BySeleniumId.class, using = "DeleteUserButton")
    private WebElement deleteUserButton;
    
    @FindBy(how = BySeleniumId.class, using = "UserNameTextbox")
    private WebElement userNameTextbox;
    
    @FindBy(how = BySeleniumId.class, using = "EditRolesAndPermissionsForUserButton")
    private WebElement editRolesAndPermissionsForUserButton;
    
    

    public UserManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserTable() {
        return new GenericCellTablePO<>(this.driver, this.userTable, DataEntryPO.class);
    }

    public DataEntryPO findUser(final String username) {
        final CellTablePO<DataEntryPO> table = getUserTable();
        List<DataEntryPO> dataEntries = new ArrayList<DataEntryPO>();
        waitUntil(() -> {
            List<DataEntryPO> entries;
            try {
                entries = table.getEntries();
                dataEntries.addAll(entries);
            } catch (StaleElementReferenceException e) {
                entries = null;
            }
            return entries != null;
        });
        for (DataEntryPO entry : table.getEntries()) {
            try {
                if (username.equals(entry.getColumnContent("User name"))) {
                    return entry;
                }
            } catch (StaleElementReferenceException e) {
                // entry is not existing any more but must not break iteration
            }
        }
        return null;
    }

    public EditUserDialogPO getEditUserDialog(final String username) {
        DataEntryPO entry = findUser(username);
        try {
            if (entry != null) {
                final WebElement action = ActionsHelper.findUpdateAction(entry.getWebElement());
                action.click();
                final WebElement dialog = findElementBySeleniumId(this.driver, "UserEditDialog");
                return new EditUserDialogPO(this.driver, dialog);
            }
        } catch (StaleElementReferenceException e) {
            // entry is not existing any more
        }
        return null;
    }
    
    public CreateUserDialogPO getCreateUserDialog() {
        createUserButton.click();
        final WebElement dialog = findElementBySeleniumId(this.driver, "CreateUserDialog");
        return new CreateUserDialogPO(this.driver, dialog);
    }
    
    public void createUserWithEqualUsernameAndPassword(String usernameAndPassword) {
        final CreateUserDialogPO createUserDialog = getCreateUserDialog();
        createUserDialog.setValues(usernameAndPassword, "", usernameAndPassword, usernameAndPassword);
        createUserDialog.clickOkButtonOrThrow();
    }

    public ChangePasswordDialogPO getChangePasswordDialog() {
        final WebElement dialog = findElementBySeleniumId(this.driver, "ChangePasswordDialog");
        return new ChangePasswordDialogPO(this.driver, dialog);
    }
    
    public UserRoleDefinitionPanelPO getUserRoles() {
        return waitForChildPO(UserRoleDefinitionPanelPO::new, "UserRoleDefinitionPanel");
    }
    
    public void selectUser(String name) {
        try {
            final DataEntryPO userTableEntry = findUser(name);
            if (userTableEntry != null) {
                userTableEntry.select();
            }
        } catch (StaleElementReferenceException e) {
            throw new ElementNotSelectableException("Cannot select user any more. Entry has already been removed from DOM.", e);
        }
    }
    
    public void deleteUser(String name) {
        selectUser(name);
        deleteSelectedUser();
        waitUntilAlertIsPresent();
        driver.switchTo().alert().accept();
        // wait until cell is removed from page
        waitUntil(() -> findUser(name) == null);
    }

    public void deleteSelectedUser() {
        deleteUserButton.click();
    }

    public void waitUntilUserFound(String userName) {
        waitUntil(() -> findUser(userName) != null);
    }

    public WildcardPermissionPanelPO getUserPermissions() {
        return waitForChildPO(WildcardPermissionPanelPO::new, "WildcardPermissionPanel");
    }
    
    public EditRolesAndPermissionsForUserDialogPO openEditRolesAndPermissionsDialogForUser(String username) {
        userNameTextbox.clear();
        userNameTextbox.sendKeys(username);
        editRolesAndPermissionsForUserButton.click();
        return waitForPO(EditRolesAndPermissionsForUserDialogPO::new, "EditUserRolesAndPermissionsDialog");
    }
    
    public void grantRoleToUserWithUserQualification(String userToGrantRole, String roleName, String userQualification) {
        grantRoleToUser(userToGrantRole, roleName, "", userQualification);
    }
    
    public void grantRoleToUserWithGroupQualification(String userToGrantRole, String roleName, String groupQualification) {
        grantRoleToUser(userToGrantRole, roleName, groupQualification, "");
    }
    
    private void grantRoleToUser(String userToGrantRole, String roleName, String groupQualification, String userQualification) {
        final EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = openEditRolesAndPermissionsDialogForUser(userToGrantRole);
        UserRoleDefinitionPanelPO userRoles = editRolesAndPermissionsDialogForUser.getUserRoles();
        userRoles.addRole(roleName, groupQualification, userQualification);
        editRolesAndPermissionsDialogForUser.clickOkButtonOrThrow();
    }
    
    public void grantPermissionToUser(String userToGrantPermission, String permission) {
        final EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = openEditRolesAndPermissionsDialogForUser(userToGrantPermission);
        WildcardPermissionPanelPO userPermissions = editRolesAndPermissionsDialogForUser.getUserPermissions();
        userPermissions.addPermission(permission);
        editRolesAndPermissionsDialogForUser.clickOkButtonOrThrow();
    }
}
