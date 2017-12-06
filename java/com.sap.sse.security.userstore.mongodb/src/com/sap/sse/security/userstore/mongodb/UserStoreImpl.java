package com.sap.sse.security.userstore.mongodb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;

import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.security.PreferenceConverter;
import com.sap.sse.security.PreferenceObjectListener;
import com.sap.sse.security.SocialSettingsKeys;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.DefaultRoles;
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

    private static final String ACCESS_TOKEN_KEY = "___access_token___";

    private String name = "MongoDB user store";

    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Set<User>> usersByEmail;
    private final ConcurrentHashMap<String, User> usersByAccessToken;
    private final ConcurrentHashMap<String, String> emailForUsername;
    private final ConcurrentHashMap<String, Object> settings;
    private final ConcurrentHashMap<String, Class<?>> settingTypes;
    
    /**
     * Keys are the usernames, values are the key/value pairs representing the user's preferences
     */
    private final ConcurrentHashMap<String, Map<String, String>> preferences;
    
    /**
     * Converter objects to map preference Strings to Objects.
     * The keys must match the keys of the preferences. 
     */
    private transient ConcurrentHashMap<String, PreferenceConverter<?>> preferenceConverters;
    
    /**
     * This is another view of the String preferences mapped by {@link #preferenceConverters} to Objects.
     * Keys are the usernames, values are the key/value pairs representing the user's preferences.
     */
    private transient ConcurrentHashMap<String, Map<String, Object>> preferenceObjects;
    
    /**
     * Keys are preferences keys as used by {@link #preferenceObjects}, values are the listeners to inform on changes of
     * the specific preference object for a {@link User}.
     */
    private transient Map<String, Set<PreferenceObjectListener<?>>> listeners;
    
    /**
     * To be used for locking when working with {@link #listeners}.
     */
    private transient NamedReentrantReadWriteLock listenersLock;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;

    public UserStoreImpl() {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    public UserStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory) {
        users = new ConcurrentHashMap<>();
        usersByEmail = new ConcurrentHashMap<>();
        emailForUsername = new ConcurrentHashMap<>();
        settings = new ConcurrentHashMap<>();
        settingTypes = new ConcurrentHashMap<>();
        usersByAccessToken = new ConcurrentHashMap<>();
        preferences = new ConcurrentHashMap<>();
        preferenceConverters = new ConcurrentHashMap<>();
        preferenceObjects = new ConcurrentHashMap<>();
        listeners = new HashMap<>();
        listenersLock = new NamedReentrantReadWriteLock(
                UserStoreImpl.class.getSimpleName() + " lock for listeners collection", false);
        this.mongoObjectFactory = mongoObjectFactory;
        if (domainObjectFactory != null) {
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
            for (Entry<String, Map<String, String>> e : preferences.entrySet()) {
                if (e.getValue() != null) {
                    final String accessToken = e.getValue().get(ACCESS_TOKEN_KEY);
                    if (accessToken != null) {
                        final User user = users.get(e.getKey());
                        if (user != null) {
                            usersByAccessToken.put(accessToken, user);
                        } else {
                            logger.warning("Couldn't find user \""+e.getKey()+"\" for which an access token was found in the preferences");
                        }
                    }
                }
            }
        }
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        preferenceConverters = new ConcurrentHashMap<>();
        preferenceObjects = new ConcurrentHashMap<>();
        listeners = new HashMap<>();
        listenersLock = new NamedReentrantReadWriteLock(
                UserStoreImpl.class.getSimpleName() + " lock for listeners collection", false);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Override
    public void clear() {
        clearAllPreferenceObjects();
        emailForUsername.clear();
        settings.clear();
        settingTypes.clear();
        users.clear();
        usersByEmail.clear();
        usersByAccessToken.clear();
    }

    /**
     * Preference objects can't be simply removed by clearing {@link #preferenceObjects} because listeners can have a
     * state depending on the current preference objects. So we need to notify all listeners about the removal of the
     * notification objects.
     */
    private void clearAllPreferenceObjects() {
        final Set<String> usersToProcess = new HashSet<>(preferences.keySet());
        for (String username : usersToProcess) {
            removeAllPreferencesForUser(username);
        }
    }

    @Override
    public void replaceContentsFrom(UserStore newUserStore) {
        clear();
        for (User user : newUserStore.getUsers()) {
            users.put(user.getName(), user);
            addToUsersByEmail(user);
            for (Entry<String, String> userPref : newUserStore.getAllPreferences(user.getName()).entrySet()) {
                setPreference(user.getName(), userPref.getKey(), userPref.getValue());
                if (userPref.getKey().equals(ACCESS_TOKEN_KEY)) {
                    usersByAccessToken.put(userPref.getValue(), user);
                }
            }
        }
        for (Entry<String, Object> setting : newUserStore.getAllSettings().entrySet()) {
            settings.put(setting.getKey(), setting.getValue());
        }
        for (Entry<String, Class<?>> settingType : newUserStore.getAllSettingTypes().entrySet()) {
            settingTypes.put(settingType.getKey(), settingType.getValue());
        }
    }

    @Override
    public boolean setAccessToken(String username, String accessToken) {
        final boolean result;
        final User user = getUserByName(username);
        if (user == null) {
            result = false;
        } else {
            result = true;
            final String oldAccessToken = getPreference(username, ACCESS_TOKEN_KEY);
            if (oldAccessToken != null) {
                usersByAccessToken.remove(oldAccessToken);
            }
            usersByAccessToken.put(accessToken, user);
            setPreference(username, ACCESS_TOKEN_KEY, accessToken);
        }
        return result;
    }

    @Override
    public String getAccessToken(String username) {
        // only the user or an administrator may request a user's access token
        final Object principal = SecurityUtils.getSubject().getPrincipal();
        if (SecurityUtils.getSubject().hasRole(DefaultRoles.ADMIN.getRolename()) ||
            (principal != null && principal.toString().equals(username))) {
            return getPreference(username, ACCESS_TOKEN_KEY);
        } else {
            throw new org.apache.shiro.authz.AuthorizationException("Only admin role or owner can retrieve access token");
        }
    }

    @Override
    public void removeAccessToken(String username) {
        // only the user or an administrator may request a user's access token
        if (SecurityUtils.getSubject().hasRole(DefaultRoles.ADMIN.getRolename()) ||
            SecurityUtils.getSubject().getPrincipal().toString().equals(username)) {
            User user = users.get(username);
            if (user != null) {
                final String accessToken = getPreference(username, ACCESS_TOKEN_KEY);
                if (accessToken != null) {
                    usersByAccessToken.remove(accessToken);
                }
                // the access token actually existed; now we need to update the preferences
                unsetPreference(username, ACCESS_TOKEN_KEY);
            }
        } else {
            throw new org.apache.shiro.authz.AuthorizationException("Only admin role or owner can retrieve access token");
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
        logger.info("Creating user: " + user + " with e-mail "+email);
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
    public boolean hasUsers() {
        return !users.isEmpty();
    }

    @Override
    public User getUserByName(String name) {
        final User result;
        if (name == null) {
            result = null;
        } else {
            result = users.get(name);
        }
        return result;
    }

    @Override
    public User getUserByAccessToken(String accessToken) {
        final User result;
        if (accessToken == null) {
            result = null;
        } else {
            result = usersByAccessToken.get(accessToken);
        }
        return result;
    }

    @Override
    public User getUserByEmail(String email) {
        final User result;
        if (email == null) {
            result = null;
        } else {
            Set<User> set = usersByEmail.get(email);
            if (set == null || set.isEmpty()) {
                result = null;
            } else {
                result = set.iterator().next();
            }
        }
        return result;
    }

    @Override
    public Iterable<String> getRolesFromUser(String username) throws UserManagementException {
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
    public Iterable<String> getPermissionsFromUser(String username) throws UserManagementException {
        if (users.get(username) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return users.get(username).getPermissions();
    }

    @Override
    public void addPermissionForUser(String name, String permission) throws UserManagementException {
        final User user = users.get(name);
        if (user == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        user.addPermission(permission);
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storeUser(user);
        }
    }

    @Override
    public void removePermissionFromUser(String name, String permission) throws UserManagementException {
        if (users.get(name) == null) {
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).removePermission(permission);
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
        removeAllPreferencesForUser(name);
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
        setPreferenceInternal(username, key, value);
        updatePreferenceObjectIfConverterIsAvailable(username, key);
    }

    private void setPreferenceInternal(String username, String key, String value) {
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
        if(value == null) {
            userMap.remove(key);
        } else {
            userMap.put(key, value);
        }
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
        unsetPreferenceObject(username, key);
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

    private void removeAllPreferencesForUser(String username) {
        // TODO should we keep the preferences anonymized (e.g. use a UUID as username) to enable better statistics?
        synchronized (preferences) {
            preferences.remove(username);
        }
        if (mongoObjectFactory != null) {
            mongoObjectFactory.storePreferences(username, Collections.<String, String>emptyMap());
        }
        removeAllPreferenceObjectsForUser(username);
    }

    private void removeAllPreferenceObjectsForUser(String username) {
        Map<String, Object> preferenceObjectsToRemove;
        synchronized (preferenceObjects) {
            preferenceObjectsToRemove = preferenceObjects.remove(username);
        }
        if(preferenceObjectsToRemove != null) {
            for(Map.Entry<String, Object> entry: preferenceObjectsToRemove.entrySet()) {
                notifyListenersOnPreferenceObjectChange(username, entry.getKey(), entry.getValue(), null);
            }
        }
    }

    @Override
    public Map<String, Object> getAllSettings() {
        return settings;
    }

    @Override
    public Map<String, Class<?>> getAllSettingTypes() {
        return settingTypes;
    }
    
    @Override
    public void registerPreferenceConverter(String preferenceKey, PreferenceConverter<?> converter) {
        PreferenceConverter<?> alreadyAssociatedConverter = preferenceConverters.putIfAbsent(preferenceKey, converter);

        if (alreadyAssociatedConverter == null) {
            final Set<String> usersToProcess = new HashSet<>(preferences.keySet());
            for (String user : usersToProcess) {
                updatePreferenceObjectWithConverter(user, preferenceKey, converter);
            }
        } else {
            logger.log(Level.SEVERE, "PreferenceConverter " + alreadyAssociatedConverter + " for key " + preferenceKey
                    + " is already registered. Converter " + converter + " will not be registered");
        }
    }
    
    @Override
    public void removePreferenceConverter(String preferenceKey) {
        PreferenceConverter<?> preferenceConverterToRemove = preferenceConverters.remove(preferenceKey);
        if (preferenceConverterToRemove != null) {
            final Set<String> usersToProcess = new HashSet<>(preferences.keySet());
            for (String username : usersToProcess) {
                unsetPreferenceObject(username, preferenceKey);
            }
        } else {
            logger.log(Level.WARNING, "PreferenceConverter for key " + preferenceKey
                    + " should be removed but wasn't registered");
        }
        
    }

    private void updatePreferenceObjectIfConverterIsAvailable(String username, String key) {
        PreferenceConverter<?> preferenceConverter = preferenceConverters.get(key);
        if (preferenceConverter != null) {
            updatePreferenceObjectWithConverter(username, key, preferenceConverter);
        }
    }

    private void updatePreferenceObjectWithConverter(String username, String key, PreferenceConverter<?> preferenceConverter) {
        final String preferenceString = getPreference(username, key);
        if (preferenceString != null) {
            try {
                final Object convertedObject = preferenceConverter.toPreferenceObject(preferenceString);
                setPreferenceObjectInternal(username, key, convertedObject);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error while converting preference for key " + key + " from String \""
                        + preferenceString + "\"", t);
            }
        }
    }

    private void setPreferenceObjectInternal(String username, String key, final Object convertedObject) {
        Map<String, Object> userMap = preferenceObjects.get(username);
        if (userMap == null) {
            synchronized (preferenceObjects) {
                // only synchronize when necessary
                userMap = preferenceObjects.get(username);
                if (userMap == null) {
                    userMap = new ConcurrentHashMap<>();
                    preferenceObjects.put(username, userMap);
                }
            }
        }
        // if the new preference object is simply null, we remove the entry instead of putting null
        Object oldPreference = convertedObject == null ? userMap.remove(key) : userMap.put(key, convertedObject);
        if (oldPreference != null || convertedObject != null) {
            // preference hasn't changed if it was null and is now null
            notifyListenersOnPreferenceObjectChange(username, key, oldPreference, convertedObject);
        }
    }

    private void unsetPreferenceObject(String username, String key) {
        Map<String, Object> userObjectMap = preferenceObjects.get(username);
        if (userObjectMap != null) {
            Object oldPreference = userObjectMap.remove(key);
            if(oldPreference != null) {
                notifyListenersOnPreferenceObjectChange(username, key, oldPreference, null);
            }
        }
    }

    @Override
    public <T> T getPreferenceObject(String username, String key) {
        final Object result;
        Map<String, Object> userMap = preferenceObjects.get(username);
        if (userMap != null) {
            result = userMap.get(key);
        } else {
            result = null;
        }
        @SuppressWarnings("unchecked")
        T resultT = (T) result;
        return resultT;
    }
    
    @Override
    public String setPreferenceObject(String username, String key, Object preferenceObject)
            throws IllegalArgumentException {
        @SuppressWarnings("unchecked")
        PreferenceConverter<Object> preferenceConverter = (PreferenceConverter<Object>) preferenceConverters.get(key);
        if (preferenceConverter == null) {
            throw new IllegalArgumentException("Setting preference for key "+key+" but there is no converter associated!");
        }
        String stringPreference = null;
        if (preferenceObject == null) {
            unsetPreference(username, key);
        } else {
            try {
                stringPreference = preferenceConverter.toPreferenceString(preferenceObject);
                setPreferenceInternal(username, key, stringPreference);
                setPreferenceObjectInternal(username, key, preferenceObject);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error while converting preference for key " + key + " from Object \""
                        + preferenceObject + "\"", t);
            }
        }
        return stringPreference;
    }

    private void notifyListenersOnPreferenceObjectChange(String username, String key, Object oldPreference,
            Object newPreference) {
        LockUtil.lockForRead(listenersLock);
        try {
            for (PreferenceObjectListener<? extends Object> listener : Util.get(listeners, key,
                    Collections.<PreferenceObjectListener<? extends Object>> emptySet())) {
                @SuppressWarnings("unchecked")
                PreferenceObjectListener<Object> listenerToFire = (PreferenceObjectListener<Object>) listener;
                listenerToFire.preferenceObjectChanged(username, key, oldPreference, newPreference);
            }
        } finally {
            LockUtil.unlockAfterRead(listenersLock);
        }
    }

    @Override
    public void addPreferenceObjectListener(String key, PreferenceObjectListener<? extends Object> listener,
            boolean fireForAlreadyExistingPreferences) {
        LockUtil.lockForWrite(listenersLock);
        try {
            Util.addToValueSet(listeners, key, listener);
            if (fireForAlreadyExistingPreferences) {
                final Set<String> usersToProcess = new HashSet<>(preferences.keySet());
                for (String username : usersToProcess) {
                    Map<String, Object> userMap = preferenceObjects.get(username);
                    if (userMap != null) {
                        Object preferenceObject = userMap.get(key);
                        if (preferenceObject != null) {
                            @SuppressWarnings("unchecked")
                            PreferenceObjectListener<Object> listenerToFire = (PreferenceObjectListener<Object>) listener;
                            listenerToFire.preferenceObjectChanged(username, key, null, preferenceObject);
                        }
                    }
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(listenersLock);
        }
    }

    @Override
    public void removePreferenceObjectListener(PreferenceObjectListener<?> listener) {
        LockUtil.lockForWrite(listenersLock);
        try {
            Util.removeFromAllValueSets(listeners, listener);
        } finally {
            LockUtil.unlockAfterWrite(listenersLock);
        }
    }
}
