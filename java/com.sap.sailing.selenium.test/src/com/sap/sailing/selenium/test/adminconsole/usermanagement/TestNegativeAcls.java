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
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestNegativeAcls extends AbstractSeleniumTest {
    private static final String USER1_NAME = "user1";
    private static final String USER2_NAME = "user2";
    private static final String USER3_NAME = "user3";
    private static final String USER2_TENANT = USER2_NAME + "-tenant";
    private static final String EVENT_NAME = "Demo event";
    private static final String USER_ROLE = "user";
    private static final String UPDATE_ACTION = "UPDATE";
    private static final String USER_GROUP_READ_PERMISSION = "USER_GROUP:READ:*";

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
        
        // user1 may only add other users' tenant to an ACL if this group is readable
        // adding permission USER_GROUP:READ:* just makes all groups readable to user 1
        userManagementPanel.selectUser(USER1_NAME);
        userManagementPanel.getUserPermissionsPO().addPermission(USER_GROUP_READ_PERMISSION);
        // FIXME hack, please implement proper wait
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
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        final EventConfigurationPanelPO eventsPanel = adminConsole.goToEvents();
        eventsPanel.createEmptyEvent(EVENT_NAME, EVENT_NAME, EVENT_NAME,
                Date.from(ZonedDateTime.now().minus(Duration.ofDays(1l)).toInstant()),
                Date.from(ZonedDateTime.now().plus(Duration.ofDays(1l)).toInstant()), true);
        EventEntryPO eventEntry = eventsPanel.getEventEntry(EVENT_NAME);
        final AclPopupPO aclPopup = eventEntry.openAclPopup();
        aclPopup.addUserGroup(USER2_TENANT);
        aclPopup.getDeniedActionsInput().addAction(UPDATE_ACTION);
        aclPopup.clickOkButtonOrThrow();
        UserManagementPanelPO userManagement = adminConsole.goToUserManagement();
        EditRolesAndPermissionsForUserDialogPO openEditRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER2_NAME);
        UserRoleDefinitionPanelPO userRoles = openEditRolesAndPermissionsDialogForUser.getUserRolesPO();
        userRoles.enterNewRoleValues(USER_ROLE, "", USER1_NAME);
        userRoles.clickAddButtonOrThrow();
        openEditRolesAndPermissionsDialogForUser.clickOkButtonOrThrow();
        
        clearSession(getWebDriver());
        setUpAuthenticatedSession(getWebDriver(), USER2_NAME, USER2_NAME);
        userManagement = AdminConsolePage.goToPage(getWebDriver(), getContextRoot()).goToUserManagement();
        openEditRolesAndPermissionsDialogForUser = userManagement.openEditRolesAndPermissionsDialogForUser(USER3_NAME);
        userRoles = openEditRolesAndPermissionsDialogForUser.getUserRolesPO();
        userRoles.enterNewRoleValues(USER_ROLE, "", USER1_NAME);
        userRoles.clickAddButtonAndExpectPermissionError();
    }
}
