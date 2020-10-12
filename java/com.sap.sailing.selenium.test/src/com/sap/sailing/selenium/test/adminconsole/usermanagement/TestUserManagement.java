package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static com.sap.sailing.selenium.pages.PageObject.DEFAULT_POLLING_INTERVAL;
import static com.sap.sailing.selenium.pages.PageObject.DEFAULT_WAIT_TIMEOUT_SECONDS;
import static com.sap.sailing.selenium.pages.PageObject.createFluentWait;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.ChangePasswordDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.CreateUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.EditUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserRoleDefinitionPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.WildcardPermissionPanelPO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
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

    private void createUser(final UserManagementPanelPO userManagementPanel) {
        final CreateUserDialogPO createUserdialog = userManagementPanel.getCreateUserDialog();
        assertNotNull(createUserdialog);
        createUserdialog.setValues(TEST_USER_NAME, TEST_USER_MAIL, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
        createUserdialog.clickOkButtonOrThrow();
        // wait until user is displayed
        FluentWait<UserManagementPanelPO> wait = createFluentWait(userManagementPanel, DEFAULT_WAIT_TIMEOUT_SECONDS,
                DEFAULT_POLLING_INTERVAL);
        wait.until(new Function<UserManagementPanelPO, Boolean>() {
            @Override
            public Boolean apply(UserManagementPanelPO userManagementPanel) {
                return userManagementPanel.findUser(TEST_USER_NAME) != null;
            }
        });
    }

    private void deleteUser(final UserManagementPanelPO userManagementPanel) {
        DataEntryPO testUserEntry = userManagementPanel.findUser(TEST_USER_NAME);
        // click delete button
        userManagementPanel.deleteUser(TEST_USER_NAME);
        // confirm deletion
        getWebDriver().switchTo().alert().accept();
        // wait until cell is removed from page
        FluentWait<DataEntryPO> wait = createFluentWait(testUserEntry, DEFAULT_WAIT_TIMEOUT_SECONDS,
                DEFAULT_POLLING_INTERVAL);
        wait.until(new Function<DataEntryPO, Object>() {
            @Override
            public Object apply(DataEntryPO testUserEntry) {
                try {
                    testUserEntry.getColumnContent(TEST_USER_NAME);
                } catch (StaleElementReferenceException e) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    @Test
    public void testUserCreation() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        assertNull(userManagementPanel.findUser(TEST_USER_NAME));
        createUser(userManagementPanel);
        assertNotNull(userManagementPanel.findUser(TEST_USER_NAME));
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
        // get cell of test user name before removing
        deleteUser(userManagementPanel);
        // double check and assert if test user is really removed from table
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
        userRolesPO.deleteEntry(TEST_ROLE + ":"+ TEST_GROUP + ":" + TEST_USER_NAME);
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNull(userRolesPO.findRole(TEST_ROLE + ":"+ TEST_GROUP + ":" + TEST_USER_NAME));
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
