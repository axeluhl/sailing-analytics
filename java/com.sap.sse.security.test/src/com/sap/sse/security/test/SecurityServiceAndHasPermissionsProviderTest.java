package com.sap.sse.security.test;

import java.util.concurrent.Callable;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserStoreManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityServiceAndHasPermissionsProviderTest {

    private static final String TEST_DEFAULT_TENANT = "TestDefaultTenant";
    private static final String ADMIN_USERNAME = "admin";
    private static final String REALM = "myRealm";
    private UserStoreImpl userStore;
    private AccessControlStoreImpl accessControlStore;
    private final WildcardPermission permission = new WildcardPermission("USER:READ:*");

    @Before
    public void setup() throws UserStoreManagementException {
        userStore = new UserStoreImpl(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), TEST_DEFAULT_TENANT);
        userStore.ensureDefaultRolesExist();
        userStore.loadAndMigrateUsers();
        accessControlStore = new AccessControlStoreImpl(userStore);
    }

    @After
    public void cleanup() {
        userStore.clear();
        accessControlStore.clear();
    }

    private SecurityService createSecurityServiceWithoutHasPermissionsProvider() {
        SecurityService securityService = new SecurityServiceImpl(null, userStore, accessControlStore, null);
        securityService.initialize();
        return securityService;
    }

    private SecurityService createSecurityServiceWithHasPermissionsProvider() {
        final SecurityService securityService = new SecurityServiceImpl(null, userStore, accessControlStore,
                SecuredSecurityTypes::getAllInstances);
        securityService.initialize();
        return securityService;
    }

    private boolean excecutePermissionCheckUnderAdminSubject(Callable<Boolean> callable) {
        PrincipalCollection principals = new SimplePrincipalCollection(ADMIN_USERNAME, REALM);
        Subject subject = new Subject.Builder().principals(principals).authenticated(true).buildSubject();
        return subject.execute(callable);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserMetaPermissionExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserAnyPermission(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserAnyPermissionExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserMetaPermission(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserMetaPermissionWithOwnershipLookupExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserMetaPermissionWithOwnershipLookup(null);
    }

    @Test
    public void testHasCurrentUserMetaPermission() {
        assert (excecutePermissionCheckUnderAdminSubject(
                () -> createSecurityServiceWithHasPermissionsProvider().hasCurrentUserAnyPermission(permission)));
    }

    @Test
    public void testHasCurrentUserAnyPermissionExpectExcept() {
        assert (excecutePermissionCheckUnderAdminSubject(() -> createSecurityServiceWithHasPermissionsProvider()
                .hasCurrentUserMetaPermission(permission, null)));
    }

    @Test
    public void testHasCurrentUserMetaPermissionWithOwnershipLookup() {
        assert (excecutePermissionCheckUnderAdminSubject(() -> createSecurityServiceWithHasPermissionsProvider()
                .hasCurrentUserMetaPermissionWithOwnershipLookup(permission)));
    }

}
