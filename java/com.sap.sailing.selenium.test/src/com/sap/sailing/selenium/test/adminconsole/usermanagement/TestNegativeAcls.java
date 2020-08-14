package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.advanced.LocalServerPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO.EventEntryPO;
import com.sap.sailing.selenium.pages.adminconsole.security.AclPopupPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupRoleDefinitionPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupUserPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.EditRolesAndPermissionsForUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserRoleDefinitionPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.WildcardPermissionPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * Test cases to ensure that a negative ACL prevents a permission to be given if the ACL affects the permission in
 * question. There are several cases where such a negative ACL may affect a meta permission check.
 */
public class TestNegativeAcls extends AbstractSeleniumTest {
    private static final String USER1_NAME = "user1";
    private static final String USER2_NAME = "user2";
    private static final String USER3_NAME = "user3";
    private static final String USER4_NAME = "user4";
    private static final String USER1_TENANT = USER1_NAME + "-tenant";
    private static final String USER2_TENANT = USER2_NAME + "-tenant";
    private static final String EVENT_NAME = "Demo event";
    private static final String USER_ROLE = "user";
    private static final String UPDATE_ACTION = "UPDATE";
    private static final String USER_GROUP_READ_PERMISSION = "USER_GROUP:READ:*";
    private static final String EVENT_ALL_PERMISSION_PREFIX = "EVENT:*:";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final LocalServerPO localServerPanel = adminConsole.goToLocalServerPanel();
        // deactivates the public server warning when opening the AdminConsole as simple user
        localServerPanel.setPublicServer(false);
        // ensures that our users can create objects on this server
        localServerPanel.setSelfServiceServer(true);
        final UserManagementPanelPO userManagementPanel = adminConsole.goToUserManagement();
        userManagementPanel.createUserWithEualUsernameAndPassword(USER1_NAME);
        userManagementPanel.createUserWithEualUsernameAndPassword(USER2_NAME);
        userManagementPanel.createUserWithEualUsernameAndPassword(USER3_NAME);
        userManagementPanel.createUserWithEualUsernameAndPassword(USER4_NAME);
        
        // user1 may only add other users' tenants to an ACL if this group is readable
        // adding permission USER_GROUP:READ:* just makes all groups readable to user 1
        userManagementPanel.grantPermissionToUser(USER1_NAME, USER_GROUP_READ_PERMISSION);
    }

    @Test
    public void testUserWithNegativeAclCantGiveARoleToAnotherUser() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        
        // user1 gives user2 and user3 the user role for objects owned by user1
        UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        userManagement.grantRoleToUserWithUserQualification(USER2_NAME, USER_ROLE, USER1_NAME);
        userManagement.grantRoleToUserWithUserQualification(USER3_NAME, USER_ROLE, USER1_NAME);
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to give the user role for objects owned by user1 to user3
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER4_NAME);
        UserRoleDefinitionPanelPO userRoles = editRolesAndPermissionsDialogForUser.getUserRoles();
        userRoles.enterNewRoleValues(USER_ROLE, "", USER1_NAME);
        // this is expected to fail because the negative ACL on one of user1's events
        // causes user 2 to not have all permissions implied by the user role
        userRoles.clickAddButtonAndExpectPermissionError();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER3_NAME, USER3_NAME);
        // user3 grants the user role for objects owned by user1 to user4
        // in this case, it works because user3 isn't affected by the negative ACL
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        userManagement.grantRoleToUserWithUserQualification(USER4_NAME, USER_ROLE, USER1_NAME);
        assertNotNull(userManagement.openEditRolesAndPermissionsDialogForUser(USER4_NAME).getUserRoles()
                .findRole(USER_ROLE + "::" + USER1_NAME));
    }
    
    @Test
    public void testUserWithNegativeAclCantGiveAPermissionToAnotherUser() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        String eventId = addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        String eventAllPermission = EVENT_ALL_PERMISSION_PREFIX + eventId;
        
        // user1 gives user2 and user3 the permission "EVENT:*:<event-id>"
        UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        userManagement.grantPermissionToUser(USER2_NAME, eventAllPermission);
        userManagement.grantPermissionToUser(USER3_NAME, eventAllPermission);
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to give the permission "EVENT:*:<event-id>" to user4
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER4_NAME);
        WildcardPermissionPanelPO userPermissions = editRolesAndPermissionsDialogForUser.getUserPermissions();
        userPermissions.enterNewPermissionValue(eventAllPermission);
        // this is expected to fail because the negative ACL on the event
        // causes user2 to not have all permissions implied by the wildcard permission
        userPermissions.clickAddButtonAndExpectPermissionError();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER3_NAME, USER3_NAME);
        // user3 grants permission "EVENT:*:<event-id>" to user4
        // in this case, it works because user3 isn't affected by the negative ACL
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        userManagement.grantPermissionToUser(USER4_NAME, eventAllPermission);
        assertNotNull(userManagement.openEditRolesAndPermissionsDialogForUser(USER4_NAME).getUserPermissions()
                .findPermission(eventAllPermission));
    }
    
    @Test
    public void testUserWithNegativeAclCantAddUserToGroup() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        
        // user1 adds the user role to his tenant and adds user2 and user3 as members
        UserGroupManagementPanelPO userGroupManagement = adminConsole.goToUserGroupDefinitions();
        userGroupManagement.findGroup(USER1_TENANT).select();
        userGroupManagement.getUserGroupRoles().addRole(USER_ROLE);
        userGroupManagement.getUserGroupUsers().addUser(USER2_NAME);
        userGroupManagement.getUserGroupUsers().addUser(USER3_NAME);
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to add user4 as member to user1's tenant
        userGroupManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserGroupDefinitions();
        UserGroupUserPanelPO userGroupUsers = userGroupManagement.getUserGroupUsers();
        userGroupManagement.findGroup(USER1_TENANT).select();
        userGroupUsers.enterNewUser(USER4_NAME);
        // this is expected to fail because the negative ACL on the event
        // causes user2 to not have all permissions of the user role for user1-tenant
        userGroupUsers.clickAddButtonAndExpectPermissionError();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER3_NAME, USER3_NAME);
        userGroupManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserGroupDefinitions();
        userGroupUsers = userGroupManagement.getUserGroupUsers();
        userGroupManagement.findGroup(USER1_TENANT).select();
        // user3 adds user4 to user1-tenant
        // in this case, it works because user3 isn't affected by the negative ACL
        userGroupUsers.addUser(USER4_NAME);
        assertNotNull(userGroupUsers.findUser(USER4_NAME));
    }
    
    @Test
    public void testUserWithNegativeAclCantAddRoleToGroup() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        
        UserGroupManagementPanelPO userGroupManagement = adminConsole.goToUserGroupDefinitions();
        userGroupManagement.findGroup(USER1_TENANT).select();
        userGroupManagement.getUserGroupUsers().addUser(USER4_NAME);
        
        // user1 adds the user role for his tenant to user2 and user3
        final UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        userManagement.grantRoleToUserWithGroupQualification(USER2_NAME, USER_ROLE, USER1_TENANT);
        userManagement.grantRoleToUserWithGroupQualification(USER3_NAME, USER_ROLE, USER1_TENANT);
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to add the user role to user1's tenant
        userGroupManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserGroupDefinitions();
        userGroupManagement.findGroup(USER1_TENANT).select();
        UserGroupRoleDefinitionPanelPO userGroupRoles = userGroupManagement.getUserGroupRoles();
        userGroupRoles.enterNewRoleName(USER_ROLE);
        // this is expected to fail because the negative ACL on the event
        // causes user2 to not have all permissions of the user role for user1-tenant
        userGroupRoles.clickAddButtonAndExpectPermissionError();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER3_NAME, USER3_NAME);
        userGroupManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserGroupDefinitions();
        userGroupManagement.findGroup(USER1_TENANT).select();
        userGroupRoles = userGroupManagement.getUserGroupRoles();
        // user3 adds user4 to user1-tenant
        // in this case, it works because user3 isn't affected by the negative ACL
        userGroupRoles.addRole(USER_ROLE);
        assertNotNull(userGroupRoles.findRole(USER_ROLE));
    }
    
    // TODO additional test cases required for:
    // adding a Permission to a Role that is granted to a user in a specific qualification
    // adding a Permission to a Role that is already added to a UserGroup
    // a user tries to leave a UserGroup which is affected by a negative ACL

    /**
     * @return the UUID of the newly created event.
     */
    private String addEventWithNegativeAclForGroup(AdminConsolePage adminConsole, String groupToUseForNegativeAcl) {
        final EventConfigurationPanelPO eventsPanel = adminConsole.goToEvents();
        eventsPanel.createEmptyEvent(EVENT_NAME, EVENT_NAME, EVENT_NAME,
                Date.from(ZonedDateTime.now().minus(Duration.ofDays(1l)).toInstant()),
                Date.from(ZonedDateTime.now().plus(Duration.ofDays(1l)).toInstant()), true);
        EventEntryPO eventEntry = eventsPanel.getEventEntry(EVENT_NAME);
        final String eventUUID = eventEntry.getUUID();
        
        // user1 adds a negative ACL for user2-tenant
        final AclPopupPO aclPopup = eventEntry.openAclPopup();
        aclPopup.addUserGroup(groupToUseForNegativeAcl);
        aclPopup.getDeniedActionsInput().addAction(UPDATE_ACTION);
        aclPopup.clickOkButtonOrThrow();
        
        return eventUUID;
    }
}
