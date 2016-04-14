package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.sap.sse.security.UserStore;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;

public class UserStoreTest {
    @Test
    public void testClear() {
        final UserStore userStore = new UserStoreImpl();
        final String username = "abc";
        final String accessToken = "ak";
        userStore.setAccessToken(username, accessToken);
        final String prefKey = "pk";
        final String prefValue = "pv";
        userStore.setPreference(username, prefKey, prefValue);
        assertEquals(prefValue, userStore.getPreference(username, prefKey));
        assertEquals(username, userStore.getUserByAccessToken(accessToken).getName());
        assertEquals(accessToken, userStore.getAccessToken(username));
        
        userStore.clear();
        assertNull(userStore.getPreference(username, prefKey));
        assertNull(userStore.getUserByAccessToken(accessToken));
        assertNull(userStore.getAccessToken(username));
    }
}
