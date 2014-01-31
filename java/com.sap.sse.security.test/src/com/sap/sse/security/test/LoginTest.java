package com.sap.sse.security.test;

import static org.junit.Assert.assertNotNull;

import org.apache.shiro.SecurityUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.sse.security.SecurityServiceImpl;

public class LoginTest {
    
    @BeforeClass
    public static void setUp(){
        new SecurityServiceImpl();
    }

    @Test
    public void testGetUser() {
        assertNotNull("Subject should not be null: ",SecurityUtils.getSubject());
    }
}
