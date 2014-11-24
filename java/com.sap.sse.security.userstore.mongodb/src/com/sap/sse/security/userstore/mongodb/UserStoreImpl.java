package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.UserManagementException;

/**
 * An implementation of the {@link UserStore} interface, intended to store its state durably in a MongoDB instance.
 * A de-serialized copy, however, will have its {@link #mongoObjectFactory} field set to <code>null</code> and will
 * therefore not perform any changes to the database. This is also the reason why all access to the
 * {@link #mongoObjectFactory} field needs to be <code>null</code>-safe.<p>
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class UserStoreImpl implements UserStore {
    private static final long serialVersionUID = -3860868283827473187L;

    private static final Logger logger = Logger.getLogger(UserStoreImpl.class.getName());

    private String name = "MongoDB user store";

    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Set<User>> usersByEmail;
    private final ConcurrentHashMap<String, String> emailForUsername;
    private final ConcurrentHashMap<String, Object> settings;
    private final ConcurrentHashMap<String, Class<?>> settingTypes;
    private final ConcurrentHashMap<String, Map<String, String>> preferences;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;

    public UserStoreImpl() {
        users = new ConcurrentHashMap<>();
        usersByEmail = new ConcurrentHashMap<>();
        emailForUsername = new ConcurrentHashMap<>();
        settings = new ConcurrentHashMap<>();
        settingTypes = new ConcurrentHashMap<String, Class<?>>();
        preferences = new ConcurrentHashMap<>();
        final DomainObjectFactory domainObjectFactory = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        for (Entry<String, Class<?>> e : domainObjectFactory.loadSettingTypes().entrySet()) {
            settingTypes.put(e.getKey(), e.getValue());
        }
        for (Entry<String, Object> e : domainObjectFactory.loadSettings().entrySet()) {
            settings.put(e.getKey(), e.getValue());
        }
        for (Entry<String, Map<String, String>> e : domainObjectFactory.loadPreferences().entrySet()) {
            preferences.put(e.getKey(), e.getValue());
        }
        boolean changed = false;
        changed = changed || initSocialSettingsIfEmpty();
        if (changed) {
            mongoObjectFactory.storeSettingTypes(settingTypes);
            mongoObjectFactory.storeSettings(settings);
        }
        for (User u : domainObjectFactory.loadAllUsers()) {
            users.put(u.getName(), u);
            addToUsersByEmail(u);
        }
    }

    @Override
    public void clear() {
        preferences.clear();
        emailForUsername.clear();
        settings.clear();
        settingTypes.clear();
        users.clear();
        usersByEmail.clear();
    }

    @Override
    public void replaceContentsFrom(UserStore newUserStore) {
        clear();
        for (User user : newUserStore.getUsers()) {
            users.put(user.getName(), user);
            addToUsersByEmail(user);
            for (Entry<String, String> userPref : newUserStore.getAllPreferences(user.getName()).entrySet()) {
                setPreference(user.getName(), userPref.getKey(), userPref.getValue());
            }
        }
        for (Entry<String, Object> setting : newUserStore.getAllSettings().entrySet()) {
            settings.put(setting.getKey(), setting.getValue());
        }
        for (Entry<String, Class<?>> settingType : newUserStore.getAllSettingTypes().entrySet()) {
            settingTypes.put(settingType.getKey(), settingType.getValue());
        }
    }

    private void addToUsersByEmail(User u) {
        if (u.getEmail() != null && !u.getEmail().isEmpty()) {
            Set<User> set = usersByEmail.get(u.getEmail());
            if (set == null) {
                set = new HashSet<>();
                usersByEmail.put(u.getEmail(), set);
            }
            set.add(u);
            emailForUsername.put(u.getName(), u.getEmail());
        }
    }

    private void removeFromUsersByEmail(User u) {
        if (u != null) {
            final String email = emailForUsername.remove(u.getName());
            if (email != null) {
                Set<User> set = usersByEmail.get(email); // this also works if the user's e-mail has changed meanwhile
                if (set != null) {
                    set.remove(u);
                }
            }
        }
    }
    
    private boolean initSocialSettingsIfEmpty() {
        boolean changed = false;
        for (SocialSettingsKeys ssk : SocialSettingsKeys.values()) {
            if (settingTypes.get(ssk.name()) == null || settings.get(ssk.name()) == null) {
                addSetting(ssk.name(), String.class);
                setSetting(ssk.name(), ssk.getValue());
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public User createUser(String name, String email, Account... accounts) throws UserManagementException {
        if (getUserByName(name) != null) {
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        User user = new User(name, email, accounts);
        logger.info("Creating user: " + user);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeUser(user);
        }
        users.put(name, user);
        addToUsersByEmail(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        logger.info("Updating user "+user+" in DB");
        users.put(user.getName(), user);
        removeFromUsersByEmail(user);
        addToUsersByEmail(user);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeUser(user);
        }
    }

    @Override
    public Iterable<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public User getUserByName(String name) {
        if (name == null) {
            return null;
        }
        return users.get(name);
    }

    @Override
    public User getUserByEmail(String email) {
        final User result;
        if (email == null) {
            result = null;
        } else {
            Set<User> set = usersByEmail.get(email);
            if (set.isEmpty()) {
                result = null;
            } else {
                result = set.iterator().next();
            }
        }
        return result;
    }

    @Override
    public Set<String> getRolesFromUser(String username) throws UserManagementException {
        if (users.get(username) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return users.get(username).getRoles();
    }

    @Override
    public void addRoleForUser(String name, String role) throws UserManagementException {
        final User user = users.get(name);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        user.addRole(role);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeUser(user);
        }
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).removeRole(role);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeUser(users.get(name));
        }
    }

    @Override
    public void deleteUser(String name) throws UserManagementException {
        if (users.get(name) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        logger.info("Deleting user: " + users.get(name).toString());
        if (mongoObjectFactory != null) {
            mongoObjectFactory.deleteUser(users.get(name));
        }
        removeFromUsersByEmail(users.remove(name));
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {
        Class<?> settingClazz = settingTypes.get(key);
        if (settingClazz == null) {
            return null;
        }
        if (!settingClazz.equals(clazz)) {
            throw new IllegalArgumentException("Value for \"" + key + "\" is not of type \"" + clazz.getName() + "\"!");
        }
        return clazz.cast(settings.get(key));
    }

    @Override
    public void addSetting(String key, Class<?> type) {
        settingTypes.put(key, type);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeSettingTypes(settingTypes);
        }
    }

    @Override
    public boolean setSetting(String key, Object setting) {
        final boolean result;
        Class<?> clazz = settingTypes.get(key);
        if (clazz == null || !clazz.isInstance(setting)) {
            result = false;
        } else {
            settings.put(key, setting);
            if (mongoObjectFactory != null) {
                mongoObjectFactory.storeSettings(settings);
            }
            result = true;
        }
        return result;
    }

    @Override
    public void setPreference(String username, String key, String value) {
        Map<String, String> userMap = preferences.get(username);
        if (userMap == null) {
            synchronized (preferences) {
                // only synchronize when necessary
                userMap = preferences.get(username);
                if (userMap == null) {
                    userMap = new ConcurrentHashMap<>();
                    preferences.put(username, userMap);
                }
            }
        }
        userMap.put(key, value);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storePreferences(username, userMap);
        }
    }

    @Override
    public void unsetPreference(String username, String key) {
        Map<String, String> userMap = preferences.get(username);
        if (userMap != null) {
            userMap.remove(key);
            if (mongoObjectFactory != null) {
                mongoObjectFactory.storePreferences(username, userMap);
            }
        }
    }

    @Override
    public String getPreference(String username, String key) {
        final String result;
        Map<String, String> userMap = preferences.get(username);
        if (userMap != null) {
            result = userMap.get(key);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Map<String, String> getAllPreferences(String username) {
        final Map<String, String> userPrefs = preferences.get(username);
        final Map<String, String> result;
        if (userPrefs == null) {
            result = Collections.emptyMap();
        } else {
            result = Collections.unmodifiableMap(userPrefs);
        }
        return result;
    }

    @Override
    public Map<String, Object> getAllSettings() {
        return settings;
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {
        return settingTypes;
    }
}
