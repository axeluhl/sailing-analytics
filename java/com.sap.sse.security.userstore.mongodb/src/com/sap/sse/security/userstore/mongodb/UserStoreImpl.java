package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UserStore;

public class UserStoreImpl implements UserStore {
    private static final Logger logger = Logger.getLogger(UserStoreImpl.class.getName());

    private String name = "MongoDB user store";

    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Object> settings;
    private final ConcurrentHashMap<String, Class<?>> settingTypes;
    private final DomainObjectFactory domainObjectFactory;
    private final MongoObjectFactory mongoObjectFactory;

    public UserStoreImpl() {
        users = new ConcurrentHashMap<>();
        settings = new ConcurrentHashMap<>();
        settingTypes = new ConcurrentHashMap<String, Class<?>>();
        domainObjectFactory = PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory();
        mongoObjectFactory = PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory();
        for (Entry<String, Class<?>> e : domainObjectFactory.loadSettingTypes().entrySet()) {
            settingTypes.put(e.getKey(), e.getValue());
        }
        for (Entry<String, Object> e : domainObjectFactory.loadSettings().entrySet()) {
            settings.put(e.getKey(), e.getValue());
        }
        boolean changed = false;
        changed = changed || initSocialSettingsIfEmpty();
        if (changed) {
            mongoObjectFactory.storeSettingTypes(settingTypes);
            mongoObjectFactory.storeSettings(settings);
        }
        for (User u : domainObjectFactory.loadAllUsers()) {
            users.put(u.getName(), u);
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
        mongoObjectFactory.storeUser(user);
        users.put(name, user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<User> getUserCollection() {
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
        mongoObjectFactory.storeUser(user);
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).removeRole(role);
        mongoObjectFactory.storeUser(users.get(name));
    }

    @Override
    public void deleteUser(String name) throws UserManagementException {
        if (users.get(name) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        logger.info("Deleting user: " + users.get(name).toString());
        mongoObjectFactory.deleteUser(users.get(name));
        users.remove(name);
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
        mongoObjectFactory.storeSettingTypes(settingTypes);
    }

    @Override
    public void setSetting(String key, Object setting) {
        Class<?> clazz = settingTypes.get(key);
        if (clazz == null || !clazz.isInstance(setting)) {
            return;
        }
        settings.put(key, setting);
        mongoObjectFactory.storeSettings(settings);
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
