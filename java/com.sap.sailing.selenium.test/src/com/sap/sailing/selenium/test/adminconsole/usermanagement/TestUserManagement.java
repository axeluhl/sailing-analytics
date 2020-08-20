package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.ChangePasswordDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.CreateUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.EditUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserRoleDefinitionPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.WildcardPermissionPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestUserManagement extends AbstractSeleniumTest {
    private static final String TEST_USER_PASSWORD = "test1";
    private static final String TEST_USER_MAIL = "";
    private static final String TEST_USER_NAME = "testUser";
    private static final String TEST_ROLE = "spectator";
    private static final String TEST_GROUP = "";
    private static final String TEST_PERMISSION = "USER:READ";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    

    @Test
    public void testUserCreation() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        assertNull(userManagementPanel.findUser(TEST_USER_NAME));
        createUser(userManagementPanel);
        assertNotNull(userManagementPanel.findUser(TEST_USER_NAME));
    }

    private void createUser(final UserManagementPanelPO userManagementPanel) {
        final CreateUserDialogPO createUserdialog = userManagementPanel.getCreateUserDialog();
        assertNotNull(createUserdialog);
        createUserdialog.setValues(TEST_USER_NAME, TEST_USER_MAIL, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
        createUserdialog.clickOkButtonOrThrow();
    }
    
    @Test
    public void testRoleCreation() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        createUser(userManagementPanel);
        final UserRoleDefinitionPanelPO userRolesPO = userManagementPanel.getUserRoles();
        assertNull(userRolesPO.findRole(TEST_ROLE));
        createRole(userRolesPO);
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNotNull(userRolesPO.findRole(TEST_ROLE + ":"+ TEST_GROUP + ":" + TEST_USER_NAME));
    }


    private void createRole(final UserRoleDefinitionPanelPO userRolesPO) {
        userRolesPO.enterNewRoleValues(TEST_ROLE, null, TEST_USER_NAME);
        userRolesPO.clickAddButtonOrThrow();
    }
    
    @Test
    public void testPermissionCreation() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        createUser(userManagementPanel);
        final WildcardPermissionPanelPO wildcardPermissionPanelPO = userManagementPanel.getUserPermissions();
        assertNull(wildcardPermissionPanelPO.findPermission(TEST_PERMISSION));
        createPermission(wildcardPermissionPanelPO);
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNotNull(wildcardPermissionPanelPO.findPermission(TEST_PERMISSION));
    }


    private void createPermission(final WildcardPermissionPanelPO wildcardPermissionPanelPO) {
        wildcardPermissionPanelPO.enterNewPermissionValue(TEST_PERMISSION);
        wildcardPermissionPanelPO.clickAddButtonOrThrow();
    }
    
    @Test
    public void testChangePassword() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        final EditUserDialogPO editUserDialog = userManagementPanel.getEditUserDialog("admin");
        assertNotNull(editUserDialog);
        editUserDialog.clickChangePasswordButton();
        final ChangePasswordDialogPO changePasswordDialog = userManagementPanel.getChangePasswordDialog();
        changePasswordDialog.setNewPassword("supersecure");
        changePasswordDialog.clickOkButtonOrThrow();
    }
    
    @Test
    public void testRemoveUser() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        createUser(userManagementPanel);
        userManagementPanel.deleteUser(TEST_USER_NAME);
        getWebDriver().switchTo().alert().accept();
        assertNull(userManagementPanel.findUser(TEST_USER_NAME));
    }
    
    @Test
    public void testRemoveUserPermission() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        createUser(userManagementPanel);
        final WildcardPermissionPanelPO wildcardPermissionPanelPO = userManagementPanel.getUserPermissions();
        createPermission(wildcardPermissionPanelPO);
        userManagementPanel.selectUser(TEST_USER_NAME);
        wildcardPermissionPanelPO.deleteEntry(TEST_PERMISSION);
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNull(wildcardPermissionPanelPO.findPermission(TEST_PERMISSION));
    }
    
    @Test
    public void testRemoveRole() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        createUser(userManagementPanel);
        final UserRoleDefinitionPanelPO userRolesPO = userManagementPanel.getUserRoles();
        createRole(userRolesPO);
        userManagementPanel.selectUser(TEST_USER_NAME);
        userRolesPO.deleteEntry(TEST_ROLE);
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNull(userRolesPO.findRole(TEST_ROLE + ":"+ TEST_GROUP + ":" + TEST_USER_NAME));
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
