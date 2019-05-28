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

        final String groupName = "test-group-01";
        final UserGroup userGroupCreated = userGroupApi.createUserGroup(adminCtx, groupName);

        assertEquals("Responded username of createUserGroup is different!", groupName, userGroupCreated.getGroupName());
        assertNotNull("GroupId is missing in reponse!", userGroupCreated.getGroupId());
        assertEquals("Wrong username count in response!", 1, Util.size(userGroupCreated.getUsers()));

        final UserGroup userGroupGet = userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());

        assertEquals("Responded user group ids are not the same!", userGroupCreated.getGroupId(),
                userGroupGet.getGroupId());
        assertEquals("Responded user group names are not the same!", userGroupCreated.getGroupName(),
                userGroupGet.getGroupName());
        assertSameElements(userGroupCreated.getUsers(), userGroupGet.getUsers());

        final UserGroup userGroupGetByName = userGroupApi.getUserGroupByName(adminCtx, groupName);

        assertEquals("Responded user group ids are not the same!", userGroupCreated.getGroupId(),
                userGroupGetByName.getGroupId());

        userGroupApi.deleteUserGroup(adminCtx, userGroupCreated.getGroupId());

        try {
            userGroupApi.getUserGroup(adminCtx, userGroupCreated.getGroupId());
            fail("Expected parsing error since user group should be null");
        } catch (RuntimeException e) {
            assertTrue("Unrelated exception.", e.getMessage().contains("Usergroup with this id does not exist"));
        }
    }

    /** |A| = |B| ∧ ∀a∈ A: a∈ B --> A and B have the same elements, ignoring order. */
    private <T> void assertSameElements(Iterable<T> a, Iterable<T> b) {
        assertEquals("Element count changed!", Util.size(a), Util.size(b));
        for (T elem : b) {
            assertTrue("Element is missing!", Util.contains(a, elem));
        }
    }
}
