package com.sap.sailing.selenium.test.adminconsole.usermanagement;

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
        
        // user1 may only add other users' tenants to an ACL if this group is readable
        // adding permission USER_GROUP:READ:* just makes all groups readable to user 1
        userManagementPanel.selectUser(USER1_NAME);
        userManagementPanel.getUserPermissions().addPermission(USER_GROUP_READ_PERMISSION);
        
        // FIXME hack to wait until the permission is saved -> please implement proper wait
        // at the moment, user management panel loses selection when saving a permission
        // this in fact prevents to wait for the permission to appear in the permission CellTable
        // after the UI is changed to keep selection, we could easily implement this using a FluentWait
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUserWithNegativeAclCantGiveARoleToAnotherUser() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        
        // user1 gives user2 the user role for objects owned by user1
        UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER2_NAME);
        UserRoleDefinitionPanelPO userRoles = editRolesAndPermissionsDialogForUser.getUserRoles();
        userRoles.enterNewRoleValues(USER_ROLE, "", USER1_NAME);
        userRoles.clickAddButtonOrThrow();
        editRolesAndPermissionsDialogForUser.clickOkButtonOrThrow();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to give the user role for objects owned by user1 to user3
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER3_NAME);
        userRoles = editRolesAndPermissionsDialogForUser.getUserRoles();
        userRoles.enterNewRoleValues(USER_ROLE, "", USER1_NAME);
        // this is expected to fail because the negative ACL on one of user1's events
        // causes user 2 to not have all permissions implied by the user role
        userRoles.clickAddButtonAndExpectPermissionError();
    }
    
    @Test
    public void testUserWithNegativeAclCantGiveAPermissionToAnotherUser() {
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER1_NAME, USER1_NAME);
        // user1 creates an event
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        String eventId = addEventWithNegativeAclForGroup(adminConsole, USER2_TENANT);
        String eventAllPermission = EVENT_ALL_PERMISSION_PREFIX + eventId;
        
        // user1 gives user2 the permission "EVENT:*:<event-id>"
        UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        EditRolesAndPermissionsForUserDialogPO editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER2_NAME);
        WildcardPermissionPanelPO userPermissions = editRolesAndPermissionsDialogForUser.getUserPermissions();
        userPermissions.addPermission(eventAllPermission);
        editRolesAndPermissionsDialogForUser.clickOkButtonOrThrow();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        // user2 tries to give the permission "EVENT:*:<event-id>" to user3
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        editRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER3_NAME);
        userPermissions = editRolesAndPermissionsDialogForUser.getUserPermissions();
        userPermissions.enterNewPermissionValue(eventAllPermission);
        // this is expected to fail because the negative ACL on the event
        // causes user2 to not have all permissions implied by the wildcard permission
        userPermissions.clickAddButtonAndExpectPermissionError();
    }
    
    // TODO additional test cases required for:
    // adding a User to a UserGroup that has a Role associated
    // adding a Role to a UserGroup
    // adding a Permission to a Role that is granted to a user in a specific qualification
    // adding a Permission to a Role that is already added to a UserGroup

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
