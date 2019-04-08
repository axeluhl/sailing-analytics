package com.sap.sse.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.sap.sse.mongodb.MongoDBConfiguration;
import com.sap.sse.mongodb.MongoDBService;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.userstore.mongodb.UserStoreImpl;
import com.sap.sse.security.userstore.mongodb.impl.CollectionNames;

/**
 * Tests that the MongoDB persistence is always in sync with the {@link UserStore}.
 */
public class UserStoreWithPersistenceTest {
    private final String username = "abc";
    private final String fullName = "Arno Nym";
    private final String company = "SAP SE";
    private final String email = "anonymous@sapsailing.com";
    private final String prefKey = "pk";
    private final String prefValue = "pv";

    private UserStoreImpl store;

    @Before
    public void setUp() throws UnknownHostException, MongoException {
        final MongoDBConfiguration dbConfiguration = MongoDBConfiguration.getDefaultTestConfiguration();
        final MongoDBService service = dbConfiguration.getService();
        MongoDatabase db = service.getDB();
        db.getCollection(CollectionNames.USERS.name()).drop();
        db.getCollection(CollectionNames.SETTINGS.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        db.getCollection(CollectionNames.PREFERENCES.name()).drop();
        newStore();
    }

    private void newStore() {
        store = new UserStoreImpl();
    }

    @Test
    public void testCreateUser() throws UserManagementException {
        store.createUser(username, email);
        assertNotNull(store.getUserByName(username));
        assertNotNull(store.getUserByEmail(email));

        newStore();
        assertNotNull(store.getUserByName(username));
        assertNotNull(store.getUserByEmail(email));
    }
    
    @Test
    public void testMasterdataIsSaved() throws UserManagementException {
        store.createUser(username, email);
        store.updateUser(new User(username, email, fullName, company, Locale.GERMAN, false, null, null, Collections.emptySet()));
        newStore();
        User savedUser = store.getUserByName(username);
        assertEquals(username, savedUser.getName());
        assertEquals(email, savedUser.getEmail());
        assertEquals(company, savedUser.getCompany());
        assertEquals(Locale.GERMAN, savedUser.getLocale());
    }

    /**
     * There was a {@link NullPointerException} thrown if calling getUserByEmail for an email address for which there
     * wasn't an associated user.
     */
    @Test
    public void testDeleteUser() throws UserManagementException {
        store.createUser(username, email);
        store.deleteUser(username);
        assertNull(store.getUserByName(username));
        assertNull(store.getUserByEmail(email));

        newStore();
        assertNull(store.getUserByName(username));
        assertNull(store.getUserByEmail(email));
    }

    @Test
    public void testSetPreferences() throws UserManagementException {
        store.createUser(username, email);
        store.setPreference(username, prefKey, prefValue);
        assertEquals(prefValue, store.getPreference(username, prefKey));
        newStore();
        assertEquals(prefValue, store.getPreference(username, prefKey));
    }

    @Test
    public void testUnsetPreferences() throws UserManagementException {
        store.createUser(username, email);
        store.setPreference(username, prefKey, prefValue);
        store.unsetPreference(username, prefKey);
        assertNull(store.getPreference(username, prefKey));
        newStore();
        assertNull(store.getPreference(username, prefKey));
    }

    /**
     * There was a bug that caused the preferences not to be removed when a user was deleted.
     */
    @Test
    public void testDeleteUserWithPreferences() throws UserManagementException {
        store.createUser(username, email);
        store.setPreference(username, prefKey, prefValue);
        store.deleteUser(username);
        assertNull(store.getPreference(username, prefKey));
        newStore();
        assertNull(store.getPreference(username, prefKey));
    }
}
