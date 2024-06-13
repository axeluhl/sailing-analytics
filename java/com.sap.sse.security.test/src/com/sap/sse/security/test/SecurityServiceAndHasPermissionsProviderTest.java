package com.sap.sse.security.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UserStoreManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.subscription.SSESubscriptionPlan;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.shared.subscription.Subscription;
import com.sap.sse.security.shared.subscription.chargebee.ChargebeeSubscription;

public class SecurityServiceAndHasPermissionsProviderTest {

    private static final String TEST_DEFAULT_TENANT = "TestDefaultTenant";
    private static final String ADMIN_USERNAME = "admin";
    private static final String REALM = "myRealm";
    private UserStoreImpl userStore;
    private AccessControlStoreImpl accessControlStore;
    private final WildcardPermission permission = new WildcardPermission("USER:READ:*");

    @Before
    public void setup() throws UserStoreManagementException {
        userStore = new UserStoreImpl(PersistenceFactory.INSTANCE.getDefaultMajorityDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMajorityMongoObjectFactory(), TEST_DEFAULT_TENANT);
        userStore.ensureDefaultRolesExist();
        userStore.loadAndMigrateUsers();
        accessControlStore = new AccessControlStoreImpl(PersistenceFactory.INSTANCE.getDefaultMajorityDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMajorityMongoObjectFactory(), userStore);
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

    @Test
    public void testSubscriptionCancellation() throws UserManagementException, UserGroupManagementException, MailException {
        final SecurityService securityService = createSecurityServiceWithHasPermissionsProvider();
        final String testUserName = "testUser";
        final User user = securityService.createSimpleUser(testUserName, /* email */ null, /* password */ "laeriuayelxuyn", /* fullName */ null, /* company */ null, /* locale */ null, /* validationBaseURL */ null, /* userOwner */ null);
        assertNotNull(user);
        // The subscription the user is supposed to have so far:
        // 
        // [subscriptionId=16BVIKT7dg6r8BrQI, planId=yearly_premium, customerId=diogocosta470, trialStart=Thu Jan 01
        // 00:00:00 UTC 1970, trialEnd=Thu Jan 01 00:00:00 UTC 1970, subscriptionStatus=active,
        // paymentStatus=no_success, invoiceId=286, invoiceStatus=payment_due, transactionType=payment,
        // transactionStatus=failure, subscriptionCreatedAt=Thu Jun 02 21:44:06 UTC 2022, reoccuringPaymentValue=4999,
        // currencyCode=USD, cancelledAt=Thu Jan 01 00:00:00 UTC 1970, subscriptionActivatedAt=Thu Jun 02 21:44:06 UTC
        // 2022, nextBillingAt=Mon Jun 02 21:44:06 UTC 2025, currentTermEnd=Mon Jun 02 21:44:06 UTC 2025,
        // subscriptionUpdatedAt=Sun Jun 02 21:44:15 UTC 2024, latestEventTime=Tue Jun 11 20:21:47 UTC 2024 (+909ms),
        // manualUpdatedAt=Thu Jan 01 00:00:00 UTC 1970, providerName=chargebee]
        final Subscription userSubscription = new ChargebeeSubscription(subscriptionId, planId, customerId, trialStart,
                trialEnd, subscriptionStatus, paymentStatus, transactionType, transactionStatus, invoiceId,
                invoiceStatus, reoccuringPaymentValue, currencyCode, subscriptionCreatedAt, subscriptionUpdatedAt,
                subscriptionActivatedAt, nextBillingAt, currentTermEnd, cancelledAt, latestEventTime, manualUpdatedAt);
        securityService.updateUserSubscription(testUserName, userSubscription);
    }
}
