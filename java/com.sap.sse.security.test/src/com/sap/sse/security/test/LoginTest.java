package com.sap.sse.security.test;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.apache.shiro.SecurityUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sse.security.SecurityServiceImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.UsernamePasswordRealm;
import com.sap.sse.security.impl.Activator;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class LoginTest {
    @BeforeClass
    public static void setUp() {
        UserStore store = new UserStoreImpl();
        UsernamePasswordRealm.setTestUserStore(store);
        Activator.setTestUserStore(store);
        new SecurityServiceImpl(store, /* mailProperties */ new Properties());
    }

    @Test
    public void testGetUser() {
        assertNotNull("Subject should not be null: ", SecurityUtils.getSubject());
    }
}
