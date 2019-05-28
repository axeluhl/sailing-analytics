package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.RoleApi;
import com.sap.sailing.selenium.api.event.RoleApi.Role;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class RoleApiTest extends AbstractSeleniumTest {

    private final RoleApi roleApi = new RoleApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testCreateAndGetRole() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        final String testRoleName = "test role 123";
        final Role roleCreated = roleApi.createRole(adminCtx, testRoleName);

        assertEquals("Responded role name of createRole is different!", testRoleName, roleCreated.getName());
        assertNotNull("UUID cannot be null!", roleCreated.getId());
        assertTrue("Permissions should be empty!", Util.isEmpty(roleCreated.getPermissions()));

        final Role roleGet = roleApi.getRole(adminCtx, roleCreated.getId());

        assertEquals("Responded role name of getRole is different!", roleCreated.getName(), roleGet.getName());
        assertEquals("UUID must be the same!", roleCreated.getId(), roleGet.getId());
        assertEquals("Permissions changed!", roleCreated.getPermissions(), roleGet.getPermissions());

        roleApi.deleteRole(adminCtx, roleCreated.getId());
    }

    @Test
    public void testDeleteRole() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        final String testRoleName = "test role 123";
        final Role roleCreated = roleApi.createRole(adminCtx, testRoleName);

        assertNotNull("UUID cannot be null!", roleCreated.getId());

        final Role roleGet = roleApi.getRole(adminCtx, roleCreated.getId());

        assertEquals("UUID must be the same!", roleCreated.getId(), roleGet.getId());

        roleApi.deleteRole(adminCtx, roleCreated.getId());

        try {
            roleApi.getRole(adminCtx, roleCreated.getId());
            fail("Expected exception since role should not be parsable!");
        } catch (RuntimeException e) {
            assertTrue("Unrelated error", e.getMessage().contains("No role with id"));
        }
    }

    @Test
    public void testUpdateRole() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);

        final String testRoleName = "test role 123";
        final Role roleCreated = roleApi.createRole(adminCtx, testRoleName);

        assertNotNull("UUID cannot be null!", roleCreated.getId());

        final Role roleGet = roleApi.getRole(adminCtx, roleCreated.getId());

        assertEquals("UUID must be the same!", roleCreated.getId(), roleGet.getId());

        final Collection<String> permissions = new ArrayList<>();
        permissions.add("COMPETITOR,EVENT:READ");
        permissions.add("BOAT:READ");

        final String updatedRoleName = roleCreated.getName() + "_changed";
        final String roleUpdate = roleApi.updateRole(adminCtx, roleCreated.getId(), permissions, updatedRoleName);

        assertNull("Empty string expected.", roleUpdate);

        final Role roleGetAfterUpdate = roleApi.getRole(adminCtx, roleCreated.getId());

        assertEquals("Responded role name of getRole is different!", updatedRoleName, roleGetAfterUpdate.getName());
        assertEquals("UUID must be the same!", roleCreated.getId(), roleGetAfterUpdate.getId());

        // |A| = |B| ∧ ∀a∈ A: a∈ B --> A and B have the same elements, ignoring order
        assertEquals("Permissions changed!", permissions.size(), Util.size(roleGetAfterUpdate.getPermissions()));
        for (String permission : roleGetAfterUpdate.getPermissions()) {
            assertTrue("Permissions missing!", permission.contains(permission));
        }

        roleApi.deleteRole(adminCtx, roleCreated.getId());
    }
}
