package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.SecurityApi.AccessToken;
import com.sap.sailing.selenium.api.event.SecurityApi.Hello;
import com.sap.sailing.selenium.api.event.SecurityApi.User;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.server.security.EventManagerRole;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.util.RemoteServerUtil;
import com.sap.sse.security.util.SecuredServer;
import com.sap.sse.security.util.SecuredServer.RoleDescriptor;
import com.sap.sse.security.util.impl.SecuredServerImpl;

public class SecurityApiTest extends AbstractSeleniumTest {
    private static final String USERNAME = "max";
    private static final String USERNAME_FULL = "Max Mustermann";

    private final SecurityApi securityApi = new SecurityApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testCreateAndGetUser() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final AccessToken createUserResponse = securityApi.createUser(adminCtx, USERNAME, USERNAME_FULL, null, "start123");
        assertEquals("Responded username of createUser is different!", USERNAME, createUserResponse.getUsername());
        assertNotNull("Token is missing in reponse!", createUserResponse.getAccessToken());
        User getUserResponse = securityApi.getUser(adminCtx, USERNAME);
        assertEquals("Responded username of getUser is different!", USERNAME, getUserResponse.getUsername());
    }

    @Test
    public void testSayHello() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final Hello hello = securityApi.sayHello(adminCtx);
        assertEquals("Responded principal of hello is different!", "admin", hello.getPrincipal());
        assertEquals("Responded authenticated of hello is different!", true, hello.isAuthenticated());
    }

    @Test
    public void testSecuredServerGetUsername() throws ClientProtocolException, IOException, ParseException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        assertEquals(ApiContext.ADMIN_USERNAME, securedServer.getUsername());
    }

    private SecuredServerImpl createSecuredServer(final ApiContext adminCtx) throws MalformedURLException {
        return new SecuredServerImpl(new URL(adminCtx.getContextRoot()),
                RemoteServerUtil.resolveBearerTokenForRemoteServer(adminCtx.getContextRoot(), ApiContext.ADMIN_USERNAME,
                        ApiContext.ADMIN_PASSWORD));
    }

    @Test
    public void testSecuredServerGetUserGroupId() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        assertNotNull(securedServer.getUserGroupIdByName("admin-tenant"));
        assertNull(securedServer.getUserGroupIdByName("this-group-does-not-exist"));
    }

    @Test
    public void testGetOwnership() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        final UUID adminTenantGroupId = securedServer.getUserGroupIdByName("admin-tenant");
        final Pair<UUID, String> userAndGroupOwner = securedServer.getGroupAndUserOwner(SecuredSecurityTypes.USER_GROUP, new TypeRelativeObjectIdentifier(adminTenantGroupId.toString()));
        assertEquals(adminTenantGroupId, userAndGroupOwner.getA());
    }

    @Test
    public void testSetAndGetAcl() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        final UUID adminTenantGroupId = securedServer.getUserGroupIdByName("admin-tenant");
        final Map<UUID, Set<String>> actionsByGroups = new HashMap<>();
        actionsByGroups.put(null, Collections.singleton("READ"));
        actionsByGroups.put(adminTenantGroupId, Collections.singleton("!DELETE"));
        securedServer.setAccessControlLists(SecuredSecurityTypes.USER_GROUP, new TypeRelativeObjectIdentifier(adminTenantGroupId.toString()), actionsByGroups);
        final Map<UUID, Set<String>> groupIdAndActions = securedServer.getAccessControlLists(SecuredSecurityTypes.USER_GROUP, new TypeRelativeObjectIdentifier(adminTenantGroupId.toString()));
        assertEquals(actionsByGroups, groupIdAndActions);
    }

    @Test
    public void testCreateUserGroup() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        final UUID humbaGroupId = securedServer.createUserGroupAndAddCurrentUser("Humba");
        assertNotNull(humbaGroupId);
        final UUID humbaGroupIdAgain = securedServer.createUserGroupAndAddCurrentUser("Humba");
        assertNull(humbaGroupIdAgain);
    }

    @Test
    public void testAddUserToAndRemoveUserFromGroup() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        final UUID humbaGroupId = securedServer.createUserGroupAndAddCurrentUser("Humba");
        {
            final Iterable<String> usernamesInGroup = securedServer.getNamesOfUsersInGroup(humbaGroupId);
            assertTrue(Util.contains(usernamesInGroup, ApiContext.ADMIN_USERNAME));
        }
        securedServer.removeCurrentUserFromGroup(humbaGroupId);
        {
            final Iterable<String> usernamesInGroup = securedServer.getNamesOfUsersInGroup(humbaGroupId);
            assertFalse(Util.contains(usernamesInGroup, ApiContext.ADMIN_USERNAME));
        }
        securedServer.addCurrentUserToGroup(humbaGroupId);
        {
            final Iterable<String> usernamesInGroup = securedServer.getNamesOfUsersInGroup(humbaGroupId);
            assertTrue(Util.contains(usernamesInGroup, ApiContext.ADMIN_USERNAME));
        }
    }

    @Test
    public void testAddRoleToUser() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        securityApi.createUser(adminCtx, USERNAME, USERNAME_FULL, null, "start123");
        securedServer.addRoleToUser(EventManagerRole.getInstance().getId(), USERNAME, /* qualified for group */ null, /* qualifiedForUserWithName */ USERNAME, /* transitive */ true);
        final Iterable<RoleDescriptor> roles = securedServer.getRoles(USERNAME);
        assertTrue(Util.stream(roles)
                .anyMatch(r -> r.getQualifiedForGroupWithId() == null
                        && r.getQualifiedForUserWithName().equals(USERNAME) && r.isTransitive()
                        && r.getRoleDefinitionId().equals(EventManagerRole.getInstance().getId())));
    }

    @Test
    public void testGetPermissions() throws ClientProtocolException, IOException, ParseException, IllegalAccessException {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        final SecuredServer securedServer = createSecuredServer(adminCtx);
        final UUID adminTenantGroupId = securedServer.getUserGroupIdByName("admin-tenant");
        final WildcardPermission groupReadPermission = SecuredSecurityTypes.USER_GROUP.getPermissionForTypeRelativeIdentifier(DefaultActions.READ, new TypeRelativeObjectIdentifier(adminTenantGroupId.toString()));
        final WildcardPermission groupCreatePermission = SecuredSecurityTypes.USER_GROUP.getPermissionForTypeRelativeIdentifier(DefaultActions.CREATE, new TypeRelativeObjectIdentifier(adminTenantGroupId.toString()));
        final Iterable<Pair<WildcardPermission, Boolean>> permissions = securedServer.hasPermissions(Arrays.asList(groupReadPermission, groupCreatePermission));
        boolean read = false;
        boolean create = false;
        for (final Pair<WildcardPermission, Boolean> permissionAndGranted : permissions) {
            if (permissionAndGranted.getA().equals(groupReadPermission) && permissionAndGranted.getB()) {
                read = true;
            }
            if (permissionAndGranted.getA().equals(groupCreatePermission) && permissionAndGranted.getB()) {
                create = true;
            }
        }
        assertTrue(read);
        assertTrue(create);
    }
}