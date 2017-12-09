package com.sap.sse.security.userstore.mongodb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.OwnershipImpl;
import com.sap.sse.security.Social;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.TenantImpl;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.userstore.mongodb.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB db;

    public DomainObjectFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore) {
        ArrayList<AccessControlList> result = new ArrayList<>();
        DBCollection aclCollection = db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name());
        try {
            for (DBObject o : aclCollection.find()) {
                result.add(loadAccessControlList(o, userStore));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load ACLs.");
            logger.log(Level.SEVERE, "loadAllAccessControlLists", e);
        }
        return result;
    }
    
    private AccessControlList loadAccessControlList(DBObject aclDBObject, UserStore userStore) {
        final String id = (String) aclDBObject.get(FieldNames.AccessControlList.OBJECT_ID.name());
        final String displayName = (String) aclDBObject.get(FieldNames.AccessControlList.OBJECT_DISPLAY_NAME.name());
        Iterable<?> dbPermissionMap = ((BasicDBList) aclDBObject.get(FieldNames.AccessControlList.PERMISSION_MAP.name()));
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        for (Object dbPermissionMapEntryO : dbPermissionMap) {
            DBObject dbPermissionMapEntry = (DBObject) dbPermissionMapEntryO;
            final UUID userGroupKey = (UUID) dbPermissionMapEntry.get(FieldNames.AccessControlList.PERMISSION_MAP_USER_GROUP_ID.name());
            final UserGroup userGroup = userStore.getUserGroup(userGroupKey);
            Set<String> actions = new HashSet<>();
            for (Object o : (BasicDBList) dbPermissionMapEntry.get(FieldNames.AccessControlList.PERMISSION_MAP_ACTIONS.name())) {
                actions.add(o.toString());
            }
            permissionMap.put(userGroup, actions);
        }
        AccessControlList result = new AccessControlListImpl(id, displayName, permissionMap);
        return result;
    }
    
    @Override
    public Iterable<Ownership> loadAllOwnerships(UserStore userStore) {
        ArrayList<Ownership> result = new ArrayList<>();
        DBCollection ownershipCollection = db.getCollection(CollectionNames.OWNERSHIPS.name());
        try {
            for (DBObject o : ownershipCollection.find()) {
                result.add(loadOwnership(o, userStore));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load ownerships.");
            logger.log(Level.SEVERE, "loadAllOwnerships", e);
        }
        return result;
    }
    
    private Ownership loadOwnership(DBObject ownershipDBObject, UserStore userStore) {
        final String idOfOwnedObject = (String) ownershipDBObject.get(FieldNames.Ownership.OBJECT_ID.name());
        final String displayNameOfOwnedObject = (String) ownershipDBObject.get(FieldNames.Ownership.OBJECT_DISPLAY_NAME.name());
        final String userOwnerName = (String) ownershipDBObject.get(FieldNames.Ownership.OWNER_USERNAME.name());
        final UUID tenantOwnerId = (UUID) ownershipDBObject.get(FieldNames.Ownership.TENANT_OWNER_ID.name());
        final SecurityUser userOwner = userStore.getUserByName(userOwnerName);
        final Tenant tenantOwner = userStore.getTenant(tenantOwnerId);
        return new OwnershipImpl(idOfOwnedObject, userOwner, tenantOwner, displayNameOfOwnedObject);
    }
    
    @Override
    public Iterable<Role> loadAllRoles() {
        ArrayList<Role> result = new ArrayList<>();
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        try {
            for (DBObject o : roleCollection.find()) {
                result.add(loadRole(o));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load roles.");
            logger.log(Level.SEVERE, "loadAllRoles", e);
        }
        return result;
    }
    
    private Role loadRole(DBObject roleDBObject) {
        final String id = (String) roleDBObject.get(FieldNames.Role.ID.name());
        final String displayName = (String) roleDBObject.get(FieldNames.Role.NAME.name());
        final Set<WildcardPermission> permissions = new HashSet<>();
        for (Object o : (BasicDBList) roleDBObject.get(FieldNames.Role.PERMISSIONS.name())) {
            permissions.add(new WildcardPermission(o.toString(), true));
        }
        Role result = new RoleImpl(UUID.fromString(id), displayName, permissions);
        return result;
    }
    
    private Set<UUID> loadAllTenantIds() {
        Set<UUID> result = new HashSet<>();
        DBCollection tenantCollection = db.getCollection(CollectionNames.TENANTS.name());
        for (DBObject o : tenantCollection.find()) {
            result.add((UUID) o.get(FieldNames.Tenant.ID.name()));
        }
        return result;
    }
    
    @Override
    public Pair<Iterable<UserGroup>, Iterable<Tenant>> loadAllUserGroupsAndTenants(Map<String, UserImpl> usersByName) {
        Set<UserGroup> userGroups = new HashSet<>();
        Set<Tenant> tenants = new HashSet<>();
        final Set<UUID> tenantIds = loadAllTenantIds();
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        try {
            for (DBObject o : userGroupCollection.find()) {
                final UserGroup userGroup = loadUserGroup(o, usersByName);
                if (tenantIds.contains(userGroup.getId())) {
                    tenants.add(new TenantImpl(userGroup));
                } else {
                    userGroups.add(userGroup);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load user groups.");
            logger.log(Level.SEVERE, "loadAllUserGroups", e);
        }
        return new Pair<Iterable<UserGroup>, Iterable<Tenant>>(userGroups, tenants);
    }
    
    private UserGroup loadUserGroup(DBObject groupDBObject, Map<String, UserImpl> usersByName) {
        final UUID id = (UUID) groupDBObject.get(FieldNames.UserGroup.ID.name());
        final String name = (String) groupDBObject.get(FieldNames.UserGroup.NAME.name());
        Set<SecurityUser> users = new HashSet<>();
        BasicDBList usersO = (BasicDBList) groupDBObject.get(FieldNames.UserGroup.USERNAMES.name());
        if (usersO != null) {
            for (Object o : usersO) {
                users.add(usersByName.get((String) o));
            }
        }
        UserGroup result = new UserGroupImpl(id, name, users);
        return result;
    }

    /**
     * @return the user objects returned have dummy objects for their {@link SecurityUser#getDefaultTenant() default
     *         tenant} attribute which need to be replaced by the caller once the {@link Tenant} objects have been
     *         loaded from the DB. The only field that is set correctly in those dummy {@link Tenant} objects
     *         is their {@link Tenant#getId() ID} field.
     */
    @Override
    public Iterable<UserImpl> loadAllUsers(Map<UUID, Role> rolesById) {
        ArrayList<UserImpl> result = new ArrayList<>();
        DBCollection userCollection = db.getCollection(CollectionNames.USERS.name());
        try {
            for (DBObject o : userCollection.find()) {
                result.add(loadUser(o, rolesById));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load users.");
            logger.log(Level.SEVERE, "loadAllUsers", e);
        }
        return result;
    }
    
    /**
     * @return the user objects returned have dummy objects for their {@link SecurityUser#getDefaultTenant() default
     *         tenant} attribute which need to be replaced by the caller once the {@link Tenant} objects have been
     *         loaded from the DB. The only field that is set correctly in those dummy {@link Tenant} objects
     *         is their {@link Tenant#getId() ID} field.
     */
    private UserImpl loadUser(DBObject userDBObject, Map<UUID, Role> rolesById) {
        final String name = (String) userDBObject.get(FieldNames.User.NAME.name());
        final String email = (String) userDBObject.get(FieldNames.User.EMAIL.name());
        final String fullName = (String) userDBObject.get(FieldNames.User.FULLNAME.name());
        final String company = (String) userDBObject.get(FieldNames.User.COMPANY.name());
        final String localeRaw = (String)  userDBObject.get(FieldNames.User.LOCALE.name());
        final Locale locale = localeRaw != null ? Locale.forLanguageTag(localeRaw) : null; 
        Boolean emailValidated = (Boolean) userDBObject.get(FieldNames.User.EMAIL_VALIDATED.name());
        String passwordResetSecret = (String) userDBObject.get(FieldNames.User.PASSWORD_RESET_SECRET.name());
        String validationSecret = (String) userDBObject.get(FieldNames.User.VALIDATION_SECRET.name());
        Set<UUID> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        BasicDBList rolesO = (BasicDBList) userDBObject.get(FieldNames.User.ROLE_IDS.name());
        if (rolesO != null) {
            for (Object o : rolesO) {
                roles.add((UUID) o);
            }
        }
        BasicDBList permissionsO = (BasicDBList) userDBObject.get(FieldNames.User.PERMISSIONS.name());
        if (permissionsO != null) {
            for (Object o : permissionsO) {
                permissions.add((String) o);
            }
        }
        final UUID defaultTenantId = (UUID) userDBObject.get(FieldNames.User.DEFAULT_TENANT_ID.name());
        final Tenant defaultTenantOnlyWithId = defaultTenantId == null ? null : new TenantImpl(defaultTenantId, null);
        DBObject accountsMap = (DBObject) userDBObject.get(FieldNames.User.ACCOUNTS.name());
        Map<AccountType, Account> accounts = createAccountMapFromdDBObject(accountsMap);
        UserImpl result = new UserImpl(name, email, fullName, company, locale,
                emailValidated == null ? false : emailValidated, passwordResetSecret, validationSecret, defaultTenantOnlyWithId,
                accounts.values());
        for (UUID roleId : roles) {
            result.addRole(rolesById.get(roleId));
        }
        for (String permission : permissions) {
            result.addPermission(new WildcardPermission(permission));
        }
        return result;
    }

    private Map<AccountType, Account> createAccountMapFromdDBObject(DBObject accountsMap) {
        Map<AccountType, Account> accounts = new HashMap<>();
        Map<?, ?> accountsM = (Map<?, ?>) accountsMap.toMap();
        for (Entry<?, ?> e : accountsM.entrySet()){
            AccountType type = AccountType.valueOf((String) e.getKey());
            Account account = createAccountFromDBObject((DBObject) e.getValue(), type);
            accounts.put(type, account);
        }
        return accounts;
    }

    private Account createAccountFromDBObject(DBObject dbAccount, final AccountType type) {
        switch (type) {
        case USERNAME_PASSWORD:
            String name = (String) dbAccount.get(FieldNames.UsernamePassword.NAME.name());
            String saltedPassword = (String) dbAccount.get(FieldNames.UsernamePassword.SALTED_PW.name());
            byte[] salt = (byte[]) dbAccount.get(FieldNames.UsernamePassword.SALT.name());
            return new UsernamePasswordAccount(name, saltedPassword, salt);
            //TODO [D056866] add other Account-types
        case SOCIAL_USER:
            SocialUserAccount socialUserAccount = new SocialUserAccount();
            for (Social s : Social.values()){
                socialUserAccount.setProperty(s.name(), (String) dbAccount.get(s.name()));
            }
            return socialUserAccount;
        default:
            return null;
        }
    }

    @Override
    public Map<String, Object> loadSettings() {
        Map<String, Object> result = new HashMap<>();
        DBCollection settingsCollection = db.getCollection(CollectionNames.SETTINGS.name());

        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.VALUES.name());
            DBObject settingDBObject = (DBObject) settingsCollection.findOne(query);
            if (settingDBObject != null) {
                result = loadSettingMap(settingDBObject);
            }
            else {
                logger.info("No stored settings found!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load settings.");
            logger.log(Level.SEVERE, "loadSettings", e);
        }

        return result;
    }

    @Override
    public Map<String, Map<String, String>> loadPreferences() {
        Map<String, Map<String, String>> result = new HashMap<>();
        DBCollection settingsCollection = db.getCollection(CollectionNames.PREFERENCES.name());
        try {
            for (Object o : settingsCollection.find()) {
                DBObject usernameAndPreferencesMap = (DBObject) o;
                Map<String, String> userMap = loadPreferencesMap((BasicDBList) usernameAndPreferencesMap.get(FieldNames.Preferences.KEYS_AND_VALUES.name()));
                String username = (String) usernameAndPreferencesMap.get(FieldNames.Preferences.USERNAME.name());
                result.put(username, userMap);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load settings.");
            logger.log(Level.SEVERE, "loadSettings", e);
        }
        return result;
    }

    private Map<String, String> loadPreferencesMap(BasicDBList preferencesDBObject) {
        Map<String, String> result = new HashMap<>();
        for (Object o : preferencesDBObject) {
            DBObject keyValue = (DBObject) o;
            String key = (String) keyValue.get(FieldNames.Preferences.KEY.name());
            String value = (String) keyValue.get(FieldNames.Preferences.VALUE.name());
            result.put(key, value);
        }
        return result;
    }

    private Map<String, Object> loadSettingMap(DBObject settingDBObject) {
        Map<String, Object> result = new HashMap<>();
        Map<?, ?> map = ((DBObject) settingDBObject.get(FieldNames.Settings.MAP.name())).toMap();
        for (Entry<?, ?> e : map.entrySet()){
            String key = (String) e.getKey();
            Object value = e.getValue();
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Map<String, Class<?>> loadSettingTypes() {
        Map<String, Class<?>> result = new HashMap<String, Class<?>>();
        DBCollection settingsCollection = db.getCollection(CollectionNames.SETTINGS.name());

        try {
            BasicDBObject query = new BasicDBObject();
            query.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.TYPES.name());
            DBObject settingTypesDBObject = settingsCollection.findOne(query);
            if (settingTypesDBObject != null) {
                result = loadSettingTypesMap(settingTypesDBObject);
            }
            else {
                logger.info("No stored setting types found!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load setting types.");
            logger.log(Level.SEVERE, "loadSettingTypes", e);
        }

        return result;
    }

    private Map<String, Class<?>> loadSettingTypesMap(DBObject settingTypesDBObject) {
        Map<String, Class<?>> result = new HashMap<>();
        Map<?, ?> map = ((DBObject) settingTypesDBObject.get(FieldNames.Settings.MAP.name())).toMap();
        for (Entry<?, ?> e : map.entrySet()){
            String key = (String) e.getKey();
            Class<?> value = null;
            try {
                value = Class.forName((String) e.getValue());
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            result.put(key, value);
        }
        return result;
    }
}
