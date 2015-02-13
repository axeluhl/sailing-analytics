package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.security.SecurityService;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.impl.SecurityServiceImpl;
import com.sap.sse.security.jaxrs.api.SecurityResource;
import com.sap.sse.security.shared.MailException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class SecurityResourceTest {
    private SecurityResource servlet;
    private SecurityServiceImpl service;

    @Before
    public void setUp() throws UserManagementException, MailException {
        final UserStoreImpl store = new UserStoreImpl();
        Activator.setTestUserStore(store);
        UsernamePasswordRealm.setTestUserStore(store);
        service = new SecurityServiceImpl(store, /* mailProperties */ new Properties(), /* setAsActivatorSecurityService */ true);
        SecurityUtils.setSecurityManager(service.getSecurityManager());
        Session session = SecurityUtils.getSubject().getSession();
        assertNotNull(session);
        servlet = new SecurityResource() {
            @Override
            public SecurityService getService() {
                return service;
            }
        };
    }
    
    @Test
    public void createAccessTokenAndAuthenticate() throws ParseException {
        String responseJsonString = (String) servlet.accessToken("admin", "admin").getEntity();
        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseJsonString);
        assertEquals("admin", responseJson.get("username"));
        String accessToken = (String) responseJson.get("access_token");
        assertNotNull(accessToken);
    }
}
