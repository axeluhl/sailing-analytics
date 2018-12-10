package com.sap.sse.security.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.MongoException;
import com.sap.sse.common.Util;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.PreferenceObjectBasedNotificationSet;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

import org.junit.Assert;

public class PreferenceObjectBasedNotificationSetTest {
    
    private static final String A = "a";
    private static final String B = "b";
    private static final String C = "c";

    private UserStoreImpl store;
    
    private static final String user1 = "me";
    private static final String user2 = "somebody_else";
    private static final String mail = "anonymous@sapsailing.com";
    
    private static final JavaIoSerializablePreferenceConverter<HashSet<String>> prefConverter = new JavaIoSerializablePreferenceConverter<>();
    private static final String prefKey = "prefKey";
    private static final String otherPrefKey = "otherPrefKey";
    private static final HashSet<String> values1 = values(A, B);
    private static final HashSet<String> values2 = values(B, C);
    private static final HashSet<String> allValues = values(A, B, C);

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        DB db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
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
    public void preferenceUpdateDoesntInfluenceOtherUserTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user2, prefKey, allValues);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.setPreferenceObject(user1, prefKey, values2);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values(user2)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user1, user2)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values(user1, user2)));
    }
    
    @Test
    public void preferenceUnsetDoesntInfluenceOtherUserTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user2, prefKey, values2);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.unsetPreference(user1, prefKey);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values(user2)));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values(user2)));
    }
    
    @Test
    public void preferenceUnsetTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.setPreferenceObject(user1, prefKey, values1);
        store.unsetPreference(user1, prefKey);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
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
    
    @Test
    public void noUserMappingTest() {
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        
        UserConsumerMock mock = new UserConsumerMock();
        notificationSet.forUsersWithVerifiedEmailMappedTo(A, mock);
        Assert.assertEquals(0, mock.calls.size());
    }
    
    @Test
    public void userMappingTest() throws UserManagementException {
        createUserWithVerifiedEmail(user1, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        
        UserConsumerMock mock = new UserConsumerMock();
        
        notificationSet.forUsersWithVerifiedEmailMappedTo(A, mock);
        Assert.assertEquals(users(store.getUserByName(user1)), mock.calls);
    }
    
    @Test
    public void userWithNonVerifiedEmailIsSkippedTest() throws UserManagementException {
        store.createUser(user1, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        
        UserConsumerMock mock = new UserConsumerMock();
        
        notificationSet.forUsersWithVerifiedEmailMappedTo(A, mock);
        Assert.assertTrue(mock.calls.isEmpty());
    }
    
    @Test
    public void userMappingWithTwoUsersTest() throws UserManagementException {
        createUserWithVerifiedEmail(user1, mail);
        createUserWithVerifiedEmail(user2, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user2, prefKey, values2);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        
        UserConsumerMock mock = new UserConsumerMock();
        notificationSet.forUsersWithVerifiedEmailMappedTo(A, mock);
        Assert.assertEquals(users(store.getUserByName(user1)), mock.calls);
        
        mock = new UserConsumerMock();
        notificationSet.forUsersWithVerifiedEmailMappedTo(B, mock);
        Assert.assertEquals(users(store.getUserByName(user1), store.getUserByName(user2)), mock.calls);
    }
    
    @Test
    public void userMappingWithOneExistingAndOneUnknownUserTest() throws UserManagementException {
        createUserWithVerifiedEmail(user1, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        store.setPreferenceObject(user2, prefKey, values2);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        
        UserConsumerMock mock = new UserConsumerMock();
        notificationSet.forUsersWithVerifiedEmailMappedTo(B, mock);
        Assert.assertEquals(users(store.getUserByName(user1)), mock.calls);
    }
    
    /**
     * There was a bug that caused the preferences not to be removed when a user was deleted.
     */
    @Test
    public void deleteUserWithMappingTest() throws UserManagementException {
        store.createUser(user1, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.deleteUser(user1);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
    }
    
    @Test
    public void removePreferenceConverterTest() throws UserManagementException {
        store.createUser(user1, mail);
        store.registerPreferenceConverter(prefKey, prefConverter);
        store.setPreferenceObject(user1, prefKey, values1);
        PreferenceObjectBasedNotificationSetImpl notificationSet = new PreferenceObjectBasedNotificationSetImpl(prefKey, store);
        store.removePreferenceConverter(prefKey);
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(A), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(B), values()));
        Assert.assertTrue(Util.equals(notificationSet.getUsersnamesToNotifyFor(C), values()));
    }
    
    private static HashSet<String> values(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
    
    private static HashSet<User> users(User... values) {
        return new HashSet<>(Arrays.asList(values));
    }
    
    private void createUserWithVerifiedEmail(String username, String email) throws UserManagementException {
        store.createUser(username, email);
        store.updateUser(new User(username, email, null, null, null, true, null, null, Collections.emptySet()));
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
    
    private static class UserConsumerMock implements Consumer<User> {
        final HashSet<User> calls = new HashSet<>();

        @Override
        public void accept(User user) {
            if(user == null) {
                throw new IllegalArgumentException("User is null");
            }
            calls.add(user);
        }
    }
}
