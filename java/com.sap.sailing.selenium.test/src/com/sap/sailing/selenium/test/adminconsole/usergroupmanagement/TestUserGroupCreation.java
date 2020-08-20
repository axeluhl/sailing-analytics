package com.sap.sailing.selenium.test.adminconsole.usergroupmanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupCreationDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupRoleDefinitionPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupUserPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestUserGroupCreation extends AbstractSeleniumTest {
    private static final String TEST_GROUP_NAME = "testGroup";
    private static final String TEST_ROLE = "spectator";
    private static final String TEST_USER_NAME = "<all>";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testUserGroupCreation() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        assertNull(userGroupManagementPanel.findGroup(TEST_GROUP_NAME));
        createGroup(userGroupManagementPanel);
        assertNotNull(userGroupManagementPanel.findGroup(TEST_GROUP_NAME));
    }

    private void createGroup(final UserGroupManagementPanelPO userGroupManagementPanel) {
        final UserGroupCreationDialogPO createUserdialog = userGroupManagementPanel.getCreateGroupDialog();
        assertNotNull(createUserdialog);
        createUserdialog.setName(TEST_GROUP_NAME);
        createUserdialog.clickOkButtonOrThrow();
    }
    
    @Test
    public void testRoleAddition() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        createGroup(userGroupManagementPanel);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        final UserGroupRoleDefinitionPanelPO userRolesPO = userGroupManagementPanel.getUserGroupRoles();
        assertNull(userRolesPO.findRole(TEST_ROLE));
        createRole(userRolesPO);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        assertNotNull(userRolesPO.findRole(TEST_ROLE));
    }

    private void createRole(final UserGroupRoleDefinitionPanelPO userRolesPO) {
        userRolesPO.enterNewRoleName(TEST_ROLE);
        userRolesPO.clickAddButtonOrThrow();
    }
    
    @Test
    public void testGroupUserAddition() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        createGroup(userGroupManagementPanel);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        final UserGroupUserPanelPO userGroupUserPanelPO = userGroupManagementPanel.getUserGroupUsers();
        assertNull(userGroupUserPanelPO.findUser(TEST_USER_NAME));
        addUserToGroup(userGroupUserPanelPO);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        assertNotNull(userGroupUserPanelPO.findUser(TEST_USER_NAME));
    }

    private void addUserToGroup(final UserGroupUserPanelPO userGroupUserPanelPO) {
        userGroupUserPanelPO.enterNewUser(TEST_USER_NAME);
        userGroupUserPanelPO.clickAddButtonOrThrow();
    }
    
    @Test
    public void testUserGroupDeletion() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        createGroup(userGroupManagementPanel);
        userGroupManagementPanel.deleteGroup(TEST_GROUP_NAME);
        getWebDriver().switchTo().alert().accept();
        assertNull(userGroupManagementPanel.findGroup(TEST_GROUP_NAME));
    }
    
    @Test
    public void testRoleRemoval() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        createGroup(userGroupManagementPanel);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        final UserGroupRoleDefinitionPanelPO userRolesPO = userGroupManagementPanel.getUserGroupRoles();
        createRole(userRolesPO);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        userRolesPO.removeRole(TEST_ROLE);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        assertNull(userRolesPO.findRole(TEST_ROLE));
    }
    
    @Test
    public void testUserRemoval() {
        final UserGroupManagementPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        createGroup(userGroupManagementPanel);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        final UserGroupUserPanelPO userGroupUserPanelPO = userGroupManagementPanel.getUserGroupUsers();
        addUserToGroup(userGroupUserPanelPO);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        userGroupUserPanelPO.removeUserFromGroup(TEST_USER_NAME);
        userGroupManagementPanel.selectGroup(TEST_GROUP_NAME);
        assertNull(userGroupUserPanelPO.findUser(TEST_USER_NAME));
    }

    private UserGroupManagementPanelPO goToUserGroupDefinitionsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserGroupDefinitions();
    }
}
