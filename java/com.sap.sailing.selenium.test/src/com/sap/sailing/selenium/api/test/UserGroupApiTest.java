package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.atomic.LongAdder;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.HttpException;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.RoleApi;
import com.sap.sailing.selenium.api.event.RoleApi.Role;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.UserGroupApi;
import com.sap.sailing.selenium.api.event.UserGroupApi.UserGroup;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class UserGroupApiTest extends AbstractSeleniumTest {

    private final UserGroupApi userGroupApi = new UserGroupApi();
    private final RoleApi roleApi = new RoleApi();
    private final SecurityApi securityApi = new SecurityApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), false);
        super.setUp();
    }

    @Test
    public void testCreateAndGetAndDeleteUserGroup() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        // create user group
        final String groupName = "test-group-01";
        final UserGroup userGroupCreated = userGroupApi.createUserGroup(adminCtx, groupName);

        assertEquals("Responded username of createUserGroup is different!", groupName, userGroupCreated.getGroupName());
        assertNotNull("GroupId is missing in reponse!", userGroupCreated.getGroupId());
        assertEquals("Wrong username count in response!", 1, Util.size(userGroupCreated.getUsers()));

        // check, if user group was created
        final UserGroup userGroupGet = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());

        assertEquals("Responded user group ids are not the same!", userGroupCreated.getGroupId(),
                userGroupGet.getGroupId());
        assertEquals("Responded user group names are not the same!", userGroupCreated.getGroupName(),
                userGroupGet.getGroupName());
        assertSameElements(userGroupCreated.getUsers(), userGroupGet.getUsers());
        assertSameElements(userGroupCreated.getRoles(), userGroupGet.getRoles());

        // check getByName
        final UserGroup userGroupGetByName = userGroupApi.getUserGroupByName(adminCtx, groupName);

        assertEquals("Responded user group ids are not the same!", userGroupCreated.getGroupId(),
                userGroupGetByName.getGroupId());

        // delete user group
        userGroupApi.deleteUserGroup(adminCtx, userGroupCreated.getGroupId());

        // check, if user group was deleted
        try {
            userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
            fail("Expected parsing error since user group should be null");
        } catch (RuntimeException e) {
            assertTrue("Unrelated exception.", e.getMessage().contains("Usergroup with this id does not exist"));
        }
    }

    @Test
    public void testMetaPermissionCheck() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        // add test users
        final SecurityApi securityApi = new SecurityApi();
        final String addingUsername = "adding user";
        final String addedUsername = "added user";

        securityApi.createUser(adminCtx, addingUsername, "test", "company", "password");
        securityApi.createUser(adminCtx, addedUsername, "test", "company", "password");

        final ApiContext userCtx = ApiContext.createApiContext(getContextRoot(), SECURITY_CONTEXT, addingUsername,
                "password");

        // create user group
        final UserGroup userGroupCreated = userGroupApi.createUserGroup(adminCtx, "test-group-01");
        assertNotNull("GroupId is missing in reponse!", userGroupCreated.getGroupId());

        // add new role to group with admin user
        final Role createdRole = roleApi.createRole(adminCtx, "My-Epic-Role");
        roleApi.updateRole(adminCtx, createdRole.getId(), Arrays.asList("*"), createdRole.getName());
        userGroupApi.addRoleToGroup(adminCtx, userGroupCreated.getGroupId(), createdRole.getId(), false);

        try {

            // add user to group with a user who does not have one of the roles implicated by the group
            userGroupApi.addUserToGroup(userCtx, userGroupCreated.getGroupId(), addedUsername);

            fail("Expected unauthorized exception.");
        } catch (RuntimeException e) {
            assertTrue("Expected unauthorized exception", e.getMessage().contains("failed (rc=401)"));
        }
    }

    @Test
    public void testGetReadableUserGroups() {
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final String user1Name = "user1";
        securityApi.createUser(adminSecurityCtx, user1Name, "test", "company", "password");
        final ApiContext user1Ctx = ApiContext.createApiContext(getContextRoot(), SERVER_CONTEXT, user1Name,
                "password");
        final ApiContext user1SecurityCtx = ApiContext.createApiContext(getContextRoot(), SECURITY_CONTEXT, user1Name,
                "password");
        assertEquals(1, Util.size(userGroupApi.getReadableGroupsOfUser(user1Ctx, user1Name)));

        // admin creates new group and adds user -> does not mean the user is allowed to read the group
        final LongAdder counter = new LongAdder();
        final UserGroup group1ToAdd = userGroupApi.createUserGroup(adminSecurityCtx, "group1ToAdd");
        userGroupApi.addUserToGroup(adminSecurityCtx, group1ToAdd.getGroupId(), user1Name);
        userGroupApi.getReadableGroupsOfUser(user1Ctx, user1Name).forEach(ug -> {
            if (group1ToAdd.getGroupId().equals(ug.getGroupId())) {
                counter.increment();
            }
        });
        assertEquals(0, counter.intValue());

        // user1 creates new group -> this group is readable by user1
        counter.reset();
        final UserGroup group2ToAdd = userGroupApi.createUserGroup(user1SecurityCtx, "group2ToAdd");
        userGroupApi.getReadableGroupsOfUser(user1Ctx, user1Name).forEach(ug -> {
            if (group2ToAdd.getGroupId().equals(ug.getGroupId())) {
                counter.increment();
            }
        });
        assertEquals(1, counter.intValue());

        // if passing null for the username, implying the current user, should return the same result
        counter.reset();
        userGroupApi.getReadableGroupsOfCurrentUser(user1Ctx).forEach(ug -> {
            if (group2ToAdd.getGroupId().equals(ug.getGroupId())) {
                counter.increment();
            }
        });
        assertEquals(1, counter.intValue());
    }

    @Test
    public void testChangeUsersInUserGroup() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        // create user group
        final String groupName = "test-group-01";
        final UserGroup userGroupCreated = userGroupApi.createUserGroup(adminCtx, groupName);
        assertNotNull("GroupId is missing in reponse!", userGroupCreated.getGroupId());

        // add test user
        final SecurityApi securityApi = new SecurityApi();
        final String username = "epicTestUser1337";
        try {
            securityApi.getUser(adminCtx, username);

        } catch (RuntimeException e) {
            securityApi.createUser(adminCtx, username, "test", "company", "password");
        }

        // add user to group
        userGroupApi.addUserToGroup(adminCtx, userGroupCreated.getGroupId(), username);

        // check, if user was properly added and group was not broken
        final UserGroup userGroupAfterUserAdd = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
        assertNotNull("GroupId is missing in reponse!", userGroupAfterUserAdd.getGroupId());
        assertEquals("Responded username of createUserGroup is different!", groupName,
                userGroupAfterUserAdd.getGroupName());
        assertSameElements(userGroupCreated.getRoles(), userGroupAfterUserAdd.getRoles());
        assertTrue("Added user is missing!", Util.contains(userGroupAfterUserAdd.getUsers(), username));

        // remove user from group
        userGroupApi.removeUserFromGroup(adminCtx, userGroupCreated.getGroupId(), username);

        // check, if user was properly removed and group is the same as before adding
        final UserGroup userGroupAfterUserRemove = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
        assertNotNull("GroupId is missing in reponse!", userGroupAfterUserRemove.getGroupId());
        assertEquals("Responded username of createUserGroup is different!", groupName,
                userGroupAfterUserRemove.getGroupName());
        assertSameElements(userGroupCreated.getRoles(), userGroupAfterUserRemove.getRoles());
        assertSameElements(userGroupCreated.getUsers(), userGroupAfterUserRemove.getUsers());
    }

    @Test
    public void setDefaultTenantForCurrentServerAndUserTest() {
        final String defaultTenantGroup = "NewGroup";
        final String eventName = "testevent";
        final String eventName2 = "testevent2";
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        final ApiContext ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        final AdminConsolePage adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);

        final EventApi eventApi = new EventApi();
        final Event eventCreatedWithDefaultTenant = eventApi.createEvent(ownerCtx, eventName, "GC 32",
                CompetitorRegistrationType.CLOSED, "somewhere");
        assertEquals("testevent", eventCreatedWithDefaultTenant.getName());

        final UserGroup newUserGroup = userGroupApi.createUserGroup(adminSecurityCtx, defaultTenantGroup);
        userGroupApi.setDefaultTenantForCurrentServerAndUser(ownerCtx, newUserGroup.getGroupId());

        final Event eventCreatedWithNewGroupTenant = eventApi.createEvent(ownerCtx, eventName2, "GC 32",
                CompetitorRegistrationType.CLOSED, "somewhere");
        assertEquals(eventName2, eventCreatedWithNewGroupTenant.getName());
        final String groupOfEvent = adminConsole.goToEvents().getEventEntry(eventName2).getColumnContent("Group");
        assertEquals(defaultTenantGroup, groupOfEvent);
    }

    @Test
    public void addUserToOwnGroupWithoutPermissionOnUserTest() {
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final String userToAdd = "usertoadd";
        securityApi.createUser(adminSecurityCtx, "groupowner", "groupowner", null, "daisy0815");
        securityApi.createUser(adminSecurityCtx, userToAdd, "", null, "daisy0815");

        final ApiContext groupownerSecurityCtx = createApiContext(getContextRoot(), SECURITY_CONTEXT, "groupowner",
                "daisy0815");
        // create group owned by "groupowner"
        final UserGroup privateUserGroup = userGroupApi.createUserGroup(groupownerSecurityCtx, "mygroup");

        // add user "usertoadd" to private user group
        final ApiContext groupownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "groupowner", "daisy0815");
        userGroupApi.addUserToUserGroupWithoutPermissionOnUser(groupownerCtx, userToAdd,
                privateUserGroup.getGroupId());
        final UserGroup privateUserGroupToCheck = userGroupApi.getUserGroup(adminSecurityCtx,
                privateUserGroup.getGroupId());
        boolean userExistsInGroup = false;
        for (final String user : privateUserGroupToCheck.getUsers()) {
            userExistsInGroup = userExistsInGroup || user.equals(userToAdd);
        }
        assertTrue("User does not exist in group", userExistsInGroup);

        // create group by admin and try to add user to it
        final UserGroup adminUserGroup = userGroupApi.createUserGroup(adminSecurityCtx, "admingroup");
        try {
            userGroupApi.addUserToUserGroupWithoutPermissionOnUser(groupownerCtx, "usertoadd",
                    adminUserGroup.getGroupId());
            fail();
        } catch (HttpException he) {
            assertEquals(401, he.getHttpStatusCode());
        }

        // try to add user again to privateUserGroup
        try {
            userGroupApi.addUserToUserGroupWithoutPermissionOnUser(groupownerCtx, "usertoadd",
                    privateUserGroup.getGroupId());
            fail();
        } catch (HttpException he) {
            assertEquals(400, he.getHttpStatusCode());
        }
    }

    @Test
    public void changeRolesAssociatedToUserGroup() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        // create user group
        final String groupName = "test-group-01";
        final UserGroup userGroupCreated = userGroupApi.createUserGroup(adminCtx, groupName);
        assertNotNull("GroupId is missing in reponse!", userGroupCreated.getGroupId());

        // create test role
        final RoleApi roleApi = new RoleApi();
        final Role role = roleApi.createRole(adminCtx, "test-role-01");
        assertNotNull("RoleId is missing in response!", role.getId());

        // add role to group
        userGroupApi.addRoleToGroup(adminCtx, userGroupCreated.getGroupId(), role.getId(), true);

        // check, if role was added properly and that the group was not broken in the process
        final UserGroup userGroupAfterRoleAdd = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
        assertNotNull("GroupId is missing in reponse!", userGroupAfterRoleAdd.getGroupId());
        assertEquals("Responded username of createUserGroup is different!", groupName,
                userGroupAfterRoleAdd.getGroupName());
        assertSameElements(userGroupCreated.getUsers(), userGroupAfterRoleAdd.getUsers());
        assertTrue("Added user is missing!", Util.contains(userGroupAfterRoleAdd.getRoles(), role));

        // remove the role from the group
        userGroupApi.removeRoleFromGroup(adminCtx, userGroupCreated.getGroupId(), role.getId());
        roleApi.deleteRole(adminCtx, role.getId());

        // check, if role was properly removed and group is the same as before adding
        final UserGroup userGroupAfterRoleRemove = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
        assertNotNull("GroupId is missing in reponse!", userGroupAfterRoleRemove.getGroupId());
        assertEquals("Responded username of createUserGroup is different!", groupName,
                userGroupAfterRoleRemove.getGroupName());
        assertSameElements(userGroupCreated.getRoles(), userGroupAfterRoleRemove.getRoles());
        assertSameElements(userGroupCreated.getUsers(), userGroupAfterRoleRemove.getUsers());
    }

    /** |A| = |B| ∧ ∀a∈ A: a∈ B --> A and B have the same elements, ignoring order. */
    private <T> void assertSameElements(Iterable<T> a, Iterable<T> b) {
        assertEquals("Element count changed!", Util.size(a), Util.size(b));
        for (T elem : b) {
            assertTrue("Element is missing!", Util.contains(a, elem));
        }
    }
}
