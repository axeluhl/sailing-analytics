package com.sap.sse.security.test;

import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserStoreManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.subscription.SSESubscriptionPlan;
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
        userStore = new UserStoreImpl(PersistenceFactory.INSTANCE.getDomainObjectFactory(ReadConcern.MAJORITY, WriteConcern.MAJORITY),
                PersistenceFactory.INSTANCE.getMongoObjectFactory(ReadConcern.MAJORITY, WriteConcern.MAJORITY), TEST_DEFAULT_TENANT);
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
        SecurityService securityService = new SecurityServiceImpl(/* mailServiceTracker */ null, userStore,
                accessControlStore, /* HasPermissionsProvider */ null, SSESubscriptionPlan::getAllInstances);
        securityService.initialize();
        return securityService;
    }

    private SecurityService createSecurityServiceWithHasPermissionsProvider() {
        final SecurityService securityService = new SecurityServiceImpl(/* mailServiceTracker */ null, userStore,
                accessControlStore, SecuredSecurityTypes::getAllInstances, SSESubscriptionPlan::getAllInstances);
        securityService.initialize();
        return securityService;
    }

    private boolean excecutePermissionCheckUnderAdminSubject(SecurityService securityService, Function<SecurityService, Boolean> callable) {
        PrincipalCollection principals = new SimplePrincipalCollection(ADMIN_USERNAME, REALM);
        Subject subject = new Subject.Builder().principals(principals).authenticated(true).buildSubject();
        return subject.execute(()->callable.apply(securityService));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserAnyPermissionExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserAnyPermission(new WildcardPermission("LEADERBOARD:READ:Humba"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserMetaPermissionExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserMetaPermission(new WildcardPermission("LEADERBOARD:READ:Humba"), /* ownership */ null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasCurrentUserMetaPermissionWithOwnershipLookupExpectException() {
        createSecurityServiceWithoutHasPermissionsProvider().hasCurrentUserMetaPermissionWithOwnershipLookup(new WildcardPermission("LEADERBOARD:READ:Humba"));
    }
    
    @Test
    public void testHasCurrentUserAnyPermission() {
        assertTrue(excecutePermissionCheckUnderAdminSubject(createSecurityServiceWithHasPermissionsProvider(),
                securityService->securityService.hasCurrentUserAnyPermission(permission)));
    }

    @Test
    public void testHasCurrentUserMetaPermission() {
        assertTrue(excecutePermissionCheckUnderAdminSubject(createSecurityServiceWithHasPermissionsProvider(),
                securityService->securityService.hasCurrentUserMetaPermission(permission, /* ownership */ null)));
    }

    @Test
    public void testHasCurrentUserMetaPermissionWithOwnershipLookup() {
        assertTrue(excecutePermissionCheckUnderAdminSubject(createSecurityServiceWithHasPermissionsProvider(),
                securityService->securityService.hasCurrentUserMetaPermissionWithOwnershipLookup(permission)));
    }

}
