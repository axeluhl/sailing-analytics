package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.RoleApi;
import com.sap.sailing.selenium.api.event.RoleApi.Role;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.UserGroupApi;
import com.sap.sailing.selenium.api.event.UserGroupApi.UserGroup;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class UserGroupApiTest extends AbstractSeleniumTest {

    private final UserGroupApi userGroupApi = new UserGroupApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
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
