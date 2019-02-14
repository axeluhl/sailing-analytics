package com.sap.sse.security.jaxrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.BearerAuthenticationToken;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.userstore.mongodb.AccessControlStoreImpl;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityResourceTest {
    private static final String USERNAME = "user-123";
    private static final String PASSWORD = "pass-234";
    private SecurityResource servlet;
    private SecurityServiceImpl service;
    private Subject authenticatedAdmin;
    private UserStore store;
    private AccessControlStore accessControlStore;

    @Before
    public void setUp() throws UserManagementException, MailException, UserGroupManagementException {
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase().drop();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            store = new UserStoreImpl("TestDefaultTenant");
            store.ensureDefaultRolesExist();
            store.ensureDefaultTenantExists();
            accessControlStore = new AccessControlStoreImpl(store);
            Activator.setTestStores(store, accessControlStore);
            service = new SecurityServiceImpl(/* mailServiceTracker */ null,
                    store, accessControlStore, /* hasPermissionsProvider */null);
            service.initialize();
            Activator.setSecurityService(service);
            SecurityUtils.setSecurityManager(service.getSecurityManager());
            service.createSimpleUser(USERNAME, "a@b.c", PASSWORD, "The User", "SAP SE",
                    /* validation URL */ Locale.ENGLISH, null, null);
            authenticatedAdmin = SecurityUtils.getSubject();
            authenticatedAdmin.login(new UsernamePasswordToken(USERNAME, PASSWORD));
            Session session = authenticatedAdmin.getSession();
            assertNotNull(session);
            servlet = new SecurityResource() {
                @Override
                public SecurityService getService() {
                    return service;
                }
            };
            store.addPermissionForUser(USERNAME, new WildcardPermission("can do")); // equivalent to "can do:*:*"
            store.addPermissionForUser(USERNAME, new WildcardPermission("event:view:*"));
            store.addPermissionForUser(USERNAME, new WildcardPermission("event:edit:123"));
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }
    
    private String getOrCreateAccessToken() throws ParseException {
        String responseJsonString = (String) servlet.respondWithAccessTokenForUser(USERNAME).getEntity();
        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseJsonString);
        assertEquals(USERNAME, responseJson.get("username"));
        String accessToken = (String) responseJson.get("access_token");
        assertNotNull(accessToken);
        return accessToken;
    }

    private String createAccessToken() throws ParseException {
        assertEquals(Response.Status.OK.getStatusCode(), servlet.respondToRemoveAccessTokenForUser(USERNAME).getStatus());
        String responseJsonString = (String) servlet.respondWithAccessTokenForUser(USERNAME).getEntity();
        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseJsonString);
        assertEquals(USERNAME, responseJson.get("username"));
        String accessToken = (String) responseJson.get("access_token");
        assertNotNull(accessToken);
        return accessToken;
    }

    private void removeAccessToken() {
        assertEquals(Response.Status.OK.getStatusCode(), servlet.respondToRemoveAccessTokenForUser(USERNAME).getStatus());
    }

    @Test
    public void createAccessTokenAndAuthenticate() throws ParseException, UserManagementException {
        String accessToken = getOrCreateAccessToken();
        User user = service.getUserByAccessToken(accessToken);
        assertNotNull(user);
        assertEquals(USERNAME, user.getName());
        final Subject subject = SecurityUtils.getSubject();
        subject.login(new BearerAuthenticationToken(accessToken));
        assertTrue(subject.isAuthenticated());
        assertEquals(USERNAME, subject.getPrincipal());
        assertTrue(subject.isPermitted("can do"));
        assertFalse(subject.isPermitted("can't do"));
        service.addPermissionForUser(USERNAME, new WildcardPermission("can't do"));
        assertTrue(subject.isPermitted("can't do"));

        assertTrue(subject.isPermitted("event:view:999"));
        assertTrue(subject.isPermitted("event:edit:123"));
        assertFalse(subject.isPermitted("event:edit:234"));
        subject.logout();
        assertFalse(subject.isAuthenticated());
    }

    @Test
    public void ensureOldBearerTokenIsInvalidatedByObtainingNewOne() throws ParseException {
        String accessToken = getOrCreateAccessToken();
        createAccessToken();
        User user = service.getUserByAccessToken(accessToken);
        assertNull(user); // the old access token is expected to have been obsoleted by obtaining a new one
    }

    @Test
    public void ensureOldBearerTokenIsInvalidatedByRequestingItsRemoval() throws ParseException {
        String accessToken = getOrCreateAccessToken();
        removeAccessToken();
        User user = service.getUserByAccessToken(accessToken);
        assertNull(user); // the old access token is expected to have been obsoleted by obtaining a new one
    }
}
