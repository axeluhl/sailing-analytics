package com.sap.sse.security.jaxrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.sap.sse.security.User;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.PersistenceFactory;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityResourceTest {
    private SecurityResource servlet;
    private SecurityServiceImpl service;
    private Subject authenticatedAdmin;

    @Before
    public void setUp() throws UserManagementException, MailException {
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().getDatabase().dropDatabase();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            final UserStoreImpl store = new UserStoreImpl();
            Activator.setTestUserStore(store);
            UsernamePasswordRealm.setTestUserStore(store);
            service = new SecurityServiceImpl(/* mailServiceTracker */ null,
                    store, /* setAsActivatorSecurityService */ true);
            SecurityUtils.setSecurityManager(service.getSecurityManager());
            authenticatedAdmin = SecurityUtils.getSubject();
            authenticatedAdmin.login(new UsernamePasswordToken("admin", "admin"));
            Session session = authenticatedAdmin.getSession();
            assertNotNull(session);
            servlet = new SecurityResource() {
                @Override
                public SecurityService getService() {
                    return service;
                }
            };
            store.addPermissionForUser("admin", "can do");
            store.addPermissionForUser("admin", "event:view:*");
            store.addPermissionForUser("admin", "event:edit:123");
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }
    
    private String getOrCreateAccessToken() throws ParseException {
        String responseJsonString = (String) servlet.respondWithAccessTokenForUser("admin").getEntity();
        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseJsonString);
        assertEquals("admin", responseJson.get("username"));
        String accessToken = (String) responseJson.get("access_token");
        assertNotNull(accessToken);
        return accessToken;
    }

    private String createAccessToken() throws ParseException {
        assertEquals(Response.Status.OK.getStatusCode(), servlet.respondToRemoveAccessTokenForUser("admin").getStatus());
        String responseJsonString = (String) servlet.respondWithAccessTokenForUser("admin").getEntity();
        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseJsonString);
        assertEquals("admin", responseJson.get("username"));
        String accessToken = (String) responseJson.get("access_token");
        assertNotNull(accessToken);
        return accessToken;
    }

    private void removeAccessToken() {
        assertEquals(Response.Status.OK.getStatusCode(), servlet.respondToRemoveAccessTokenForUser("admin").getStatus());
    }

    @Test
    public void createAccessTokenAndAuthenticate() throws ParseException {
        String accessToken = getOrCreateAccessToken();
        User user = service.getUserByAccessToken(accessToken);
        assertNotNull(user);
        assertEquals("admin", user.getName());
        final Subject subject = SecurityUtils.getSubject();
        subject.login(new BearerAuthenticationToken(accessToken));
        assertTrue(subject.isAuthenticated());
        assertEquals("admin", subject.getPrincipal());
        assertTrue(subject.isPermitted("can do"));
        assertFalse(subject.isPermitted("can't do"));
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
