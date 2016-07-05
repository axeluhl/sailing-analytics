package com.sap.sse.security.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

import junit.framework.Assert;

public class PreferenceObjectBasedNotificationSetTest {
    
    private static final String A = "a";
    private static final String B = "b";
    private static final String C = "c";

    private UserStoreImpl store;
    
    private String user1 = "me";
    private String user2 = "somebody_else";
    
    private JavaIoSerializablePreferenceConverter<HashSet<String>> prefConverter = new JavaIoSerializablePreferenceConverter<>();
    private String prefKey = "prefKey";
    private String otherPrefKey = "otherPrefKey";
    private HashSet<String> values1 = values(A, B);
    private HashSet<String> values2 = values(B, C);

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        DB db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        store = new UserStoreImpl();
    }
    
    @Test
    public void noPreferenceAvailableTest() {
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        Assert.assertTrue(Util.isEmpty(notificationSet.getUsersnamesToNotifyFor(prefKey)));
    }

    @Test
    public void preferenceAllreadySetTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
    }
    
    @Test
    public void preferenceSetAfterNotificationSetCreationTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.setPreferenceObject(user1, prefKey, values1);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
    }
    
    @Test
    public void preferenceSetForTwoUsersTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user2, prefKey, values2);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1, user2)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values(user2)));
    }
    
    @Test
    public void preferenceUpdateTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.setPreferenceObject(user1, prefKey, values2);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values(user1)));
    }
    
    @Test
    public void testForObjectThatIsntKnownTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor("x"), values()));
    }
    
    @Test
    public void otherPrefDoesntInfluenceCalculationTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.registerPreferenceConverter(otherPrefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user1, otherPrefKey, values2);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
    }
    
    private static HashSet<String> values(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
    
    private static class PreferenceObjectBasedNotificationSetImpl extends PreferenceObjectBasedNotificationSet<HashSet<String>, String> {

        public PreferenceObjectBasedNotificationSetImpl(String key, UserStore store) {
            super(key, store);
        }

        @Override
        protected Collection<String> calculateObjectsToNotify(HashSet<String> preference) {
            return preference;
        }
        
    }
}
