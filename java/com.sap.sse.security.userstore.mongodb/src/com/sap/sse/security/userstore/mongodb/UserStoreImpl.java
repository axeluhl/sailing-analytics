package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.SocialSettingsKeys;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;


public class UserStoreImpl implements UserStore {
    private static final Logger logger = Logger.getLogger(UserStoreImpl.class.getName());
    
    private ConcurrentHashMap<String, User> users;
    private ConcurrentHashMap<String, Object> settings;
    private ConcurrentHashMap<String, Class<?>> settingTypes;

    public UserStoreImpl() {
        users = new ConcurrentHashMap<>();
        settings = new ConcurrentHashMap<>();
        settingTypes = new ConcurrentHashMap<String, Class<?>>();
        initSocialSettingsIfEmpty();
        addSetting("email_required", Boolean.class);
        for (User u : PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory().loadAllUsers()){
            users.put(u.getName(), u);
        }
    }
    
    private void initSocialSettingsIfEmpty() {
        for (SocialSettingsKeys ssk : SocialSettingsKeys.values()){
            if (settingTypes.get(ssk.name()) == null || settings.get(ssk.name()) == null){
                addSetting(ssk.name(), String.class);
                setSetting(ssk.name(), ssk.getValue());
            }
        }
    }

    @Override
    public String getName() {
        return "MongoDB user store";
    }

    @Override
    public User createUser(String name, String email, Account... accounts) {
        User user = new User(name, email, accounts);
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().storeUser(user);
        users.put(name, user);
        return user;
    }

    @Override
    public Collection<User> getUserCollection() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public User getUserByName(String name) {
        return users.get(name);
    }

    @Override
    public Set<String> getRolesFromUser(String name) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return users.get(name).getRoles();
    }

    @Override
    public void addRoleForUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).addRole(role);
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().storeUser(users.get(name));
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).removeRole(role);
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().storeUser(users.get(name));
    }

    @Override
    public void deleteUser(String name) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory().deleteUser(users.get(name));
        users.remove(name);
    }

    @Override
    public <T> T getSetting(String key, Class<T> clazz) {
        Class<?> settingClazz = settingTypes.get(key);
        if (settingClazz == null){
            return null;
        }
        if (!settingClazz.equals(clazz)){
            throw new IllegalArgumentException("Value for \"" + key + "\" is not of type \"" + clazz.getName() + "\"!");
        }
        return clazz.cast(settings.get(key));
    }

    @Override
    public void addSetting(String key, Class<?> type) {
        settingTypes.put(key, type);
    }

    @Override
    public void setSetting(String key, Object setting) {
        Class<?> clazz = settingTypes.get(key);
        if (clazz == null || !clazz.isInstance(setting)){
            return;
        }
        settings.put(key, setting);
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
