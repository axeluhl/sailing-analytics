package com.sap.sse.security.test;

import static org.junit.Assert.assertNotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.util.Factory;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoginTest {
    
    @BeforeClass
    public static void setUp(){
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory();
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }

    @Test
    public void testGetUser() {
        assertNotNull("Subject should not be null: ",SecurityUtils.getSubject());
    }
}
