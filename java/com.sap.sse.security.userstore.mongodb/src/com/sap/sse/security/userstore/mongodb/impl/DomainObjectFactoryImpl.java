package com.sap.sse.security.userstore.mongodb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sse.common.Util;
import com.sap.sse.security.Social;
import com.sap.sse.security.UserGroupProvider;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.userstore.mongodb.DomainObjectFactory;
import com.sap.sse.security.userstore.mongodb.impl.FieldNames.Tenant;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB db;

    public DomainObjectFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public Iterable<AccessControlListAnnotation> loadAllAccessControlLists(UserStore userStore) {
        ArrayList<AccessControlListAnnotation> result = new ArrayList<>();
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
    
    private AccessControlListAnnotation loadAccessControlList(DBObject aclDBObject, UserStore userStore) {
        final QualifiedObjectIdentifier id = new QualifiedObjectIdentifierImpl((String) aclDBObject.get(FieldNames.AccessControlList.OBJECT_ID.name()));
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
        AccessControlListAnnotation result = new AccessControlListAnnotation(new AccessControlList(permissionMap), id,
                displayName);
        return result;
    }
    
    @Override
    public Iterable<OwnershipAnnotation> loadAllOwnerships(UserStore userStore) {
        ArrayList<OwnershipAnnotation> result = new ArrayList<>();
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
    
    private OwnershipAnnotation loadOwnership(DBObject ownershipDBObject, UserStore userStore) {
        final QualifiedObjectIdentifier idOfOwnedObject = new QualifiedObjectIdentifierImpl((String) ownershipDBObject.get(FieldNames.Ownership.OBJECT_ID.name()));
        final String displayNameOfOwnedObject = (String) ownershipDBObject.get(FieldNames.Ownership.OBJECT_DISPLAY_NAME.name());
        final String userOwnerName = (String) ownershipDBObject.get(FieldNames.Ownership.OWNER_USERNAME.name());
        final UUID tenantOwnerId = (UUID) ownershipDBObject.get(FieldNames.Ownership.TENANT_OWNER_ID.name());
        final User userOwner = userStore.getUserByName(userOwnerName);
        final UserGroup tenantOwner = userStore.getUserGroup(tenantOwnerId);
        return new OwnershipAnnotation(new Ownership(userOwner, tenantOwner), idOfOwnedObject, displayNameOfOwnedObject);
    }
    
    @Override
    public Iterable<RoleDefinition> loadAllRoleDefinitions() {
        ArrayList<RoleDefinition> result = new ArrayList<>();
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        try {
            for (DBObject o : roleCollection.find()) {
                result.add(loadRoleDefinition(o));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load role definitions.");
            logger.log(Level.SEVERE, "loadAllRoleDefinitions", e);
        }
        return result;
    }
    
    private RoleDefinition loadRoleDefinition(DBObject roleDefinitionDBObject) {
        final String id = (String) roleDefinitionDBObject.get(FieldNames.Role.ID.name());
        final String displayName = (String) roleDefinitionDBObject.get(FieldNames.Role.NAME.name());
        final Set<WildcardPermission> permissions = new HashSet<>();
        for (Object o : (BasicDBList) roleDefinitionDBObject.get(FieldNames.Role.PERMISSIONS.name())) {
            permissions.add(new WildcardPermission(o.toString()));
        }
        return new RoleDefinitionImpl(UUID.fromString(id), displayName, permissions);
    }
    
    @Override
    public Iterable<UserGroup> loadAllUserGroupsAndTenantsWithProxyUsers() {
        Set<UserGroup> userGroups = new HashSet<>();
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        try {
            for (DBObject o : userGroupCollection.find()) {
                final UserGroup userGroup = loadUserGroupWithProxyUsers(o);
                userGroups.add(userGroup);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load user groups.");
            logger.log(Level.SEVERE, "loadAllUserGroups", e);
        }
        return userGroups;
    }
    
    private UserGroup loadUserGroupWithProxyUsers(DBObject groupDBObject) {
        final UUID id = (UUID) groupDBObject.get(FieldNames.UserGroup.ID.name());
        final String name = (String) groupDBObject.get(FieldNames.UserGroup.NAME.name());
        Set<User> users = new HashSet<>();
        BasicDBList usersO = (BasicDBList) groupDBObject.get(FieldNames.UserGroup.USERNAMES.name());
        if (usersO != null) {
            for (Object o : usersO) {
                users.add(new UserProxy((String) o));
            }
        }
        UserGroup result = new UserGroup(users, id, name);
        return result;
    }

    /**
     * @param defaultTenantForRoleMigration
     *            when a string-based role is found on the user object it will be mapped to a {@link Role} object
     *            pointing to an equal-named {@link RoleDefinition} from the {@code roleDefinitionsById} map, with a
     *            {@link Role#getQualifiedForTenant() tenant qualification} as defined by this parameter; if this
     *            parameter is {@code null}, role migration will throw an exception.
     * @param userGroups
     *            the user groups to resolve tenant IDs against for users' default tenants as well as role tenant qualifiers
     * @return the user objects returned have a fully resolved default tenant as well as fully-resolved role tenant/user
     *         qualifiers; the {@link Tenant} objects passed in the {@code tenants} map may still have an empty user
     *         group that is filled later.
     */
    @Override
    public Iterable<User> loadAllUsers(
            Map<UUID, RoleDefinition> roleDefinitionsById, UserGroup defaultTenantForRoleMigration,
            Map<UUID, UserGroup> userGroups, UserGroupProvider userGroupProvider) throws UserManagementException {
        Map<String, User> result = new HashMap<>();
        DBCollection userCollection = db.getCollection(CollectionNames.USERS.name());
        try {
            for (DBObject o : userCollection.find()) {
                User userWithProxyRoleUserQualifier = loadUserWithProxyRoleUserQualifiers(o, roleDefinitionsById,
                        defaultTenantForRoleMigration, userGroups, userGroupProvider);
                result.put(userWithProxyRoleUserQualifier.getName(), userWithProxyRoleUserQualifier);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load users.");
            logger.log(Level.SEVERE, "loadAllUsers", e);
        }
        resolveRoleUserQualifiers(result);
        return result.values();
    }
    
    private void resolveRoleUserQualifiers(Map<String, User> users) throws UserManagementException {
        for (final User user : users.values()) {
            final Set<Role> userRoles = new HashSet<>();
            Util.addAll(user.getRoles(), userRoles); // avoid concurrent modification exception
            for (final Role roleWithUserQualifierProxy : userRoles) {
                final User userQualifierProxy = roleWithUserQualifierProxy.getQualifiedForUser();
                if (userQualifierProxy != null) {
                    final User resolvedUserQualifier = users.get(userQualifierProxy.getName());
                    if (resolvedUserQualifier == null) {
                        throw new UserManagementException("Unable to resolve user named "+userQualifierProxy.getName()+
                                " which serves as a role qualifier for role "+roleWithUserQualifierProxy.getName()+
                                " for user "+user.getName());
                    }
                    user.removeRole(roleWithUserQualifierProxy);
                    user.addRole(new Role(roleWithUserQualifierProxy.getRoleDefinition(),
                            roleWithUserQualifierProxy.getQualifiedForTenant(), resolvedUserQualifier));
                }
            }
        }
    }

    /**
     * @param defaultTenantForRoleMigration
     *            when a string-based role is found on the user object it will be mapped to a {@link Role} object
     *            pointing to an equal-named {@link RoleDefinition} from the {@code roleDefinitionsById} map, with a
     *            {@link Role#getQualifiedForTenant() tenant qualification} as defined by this parameter; if this
     *            parameter is {@code null}, role migration will throw an exception.
     * @param tenants
     *            the tenants to resolve tenant IDs against for users' default tenants as well as role tenant qualifiers
     * @return the user objects returned have dummy objects for their {@link UserImpl#getRoles() roles'}
     *         {@link Role#getQualifiedForUser() user qualifier} where only the username is set properly to identify the
     *         user in the calling method where ultimately all users will be known.
     */
    private UserImpl loadUserWithProxyRoleUserQualifiers(DBObject userDBObject,
            Map<UUID, RoleDefinition> roleDefinitionsById, UserGroup defaultTenantForRoleMigration, Map<UUID, UserGroup> tenants, UserGroupProvider userGroupProvider) {
        final String name = (String) userDBObject.get(FieldNames.User.NAME.name());
        final String email = (String) userDBObject.get(FieldNames.User.EMAIL.name());
        final String fullName = (String) userDBObject.get(FieldNames.User.FULLNAME.name());
        final String company = (String) userDBObject.get(FieldNames.User.COMPANY.name());
        final String localeRaw = (String)  userDBObject.get(FieldNames.User.LOCALE.name());
        final Locale locale = localeRaw != null ? Locale.forLanguageTag(localeRaw) : null; 
        Boolean emailValidated = (Boolean) userDBObject.get(FieldNames.User.EMAIL_VALIDATED.name());
        String passwordResetSecret = (String) userDBObject.get(FieldNames.User.PASSWORD_RESET_SECRET.name());
        String validationSecret = (String) userDBObject.get(FieldNames.User.VALIDATION_SECRET.name());
        Set<Role> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        BasicDBList rolesO = (BasicDBList) userDBObject.get(FieldNames.User.ROLE_IDS.name());
        boolean rolesMigrated = false; // if a role needs migration, user needs an update in the DB
        if (rolesO != null) {
            for (Object o : rolesO) {
                final Role role = loadRoleWithProxyUserQualifier((DBObject) o, roleDefinitionsById, tenants);
                if (role != null) {
                    roles.add(role);
                } else {
                    logger.warning("Role with ID "+o+" that used to be assigned to user "+name+" not found");
                }
            }
        } else {
            // migration of old name-based, non-entity roles:
            // try to find an equal-named role in the set of role definitions and create a role
            // that is qualified by the default tenant; for this a default tenant must exist because
            // otherwise a user would obtain global rights by means of migration which must not happen.
            logger.info("Migrating roles of user "+name);
            BasicDBList roleNames = (BasicDBList) userDBObject.get("ROLES");
            if (roleNames != null) {
                logger.info("Found old roles "+roleNames+" for user "+name);
                if (defaultTenantForRoleMigration == null) {
                    throw new IllegalStateException(
                            "For role migration a valid default tenant is required. Set system property "
                                    + UserStore.DEFAULT_TENANT_NAME_PROPERTY_NAME+" or provide a server name");
                }
                for (Object o : roleNames) {
                    boolean found = false;
                    for (final RoleDefinition roleDefinition : roleDefinitionsById.values()) {
                        if (roleDefinition.getName().equals(o.toString())) {
                            logger.info("Found role "+roleDefinition+" for old role "+o.toString()+" for user "+name);
                            roles.add(new Role(roleDefinition, defaultTenantForRoleMigration,
                                    /* user qualification */ null));
                            rolesMigrated = true;
                            break;
                        }
                    }
                    if (!found) {
                        logger.warning("Role "+o.toString()+" for user "+name+" not found during migration. User will no longer be in this role.");
                    }
                }
            }
        }
        BasicDBList permissionsO = (BasicDBList) userDBObject.get(FieldNames.User.PERMISSIONS.name());
        if (permissionsO != null) {
            for (Object o : permissionsO) {
                permissions.add((String) o);
            }
        }

        final Map<String, UserGroup> defaultTenant = new ConcurrentHashMap<>();
        final BasicDBList defaultTenantIds = (BasicDBList) userDBObject.get(FieldNames.User.DEFAULT_TENANT_IDS.name());
        if (defaultTenantIds != null) {
            for (Object singleDefaultTenant : defaultTenantIds) {
                BasicDBObject singleDefaultTenantObj = (BasicDBObject) singleDefaultTenant;
                String serverName = singleDefaultTenantObj.getString(FieldNames.User.DEFAULT_TENANT_SERVER.name());
                UUID groupId = (UUID) singleDefaultTenantObj.get(FieldNames.User.DEFAULT_TENANT_GROUP.name());
                UserGroup tenantOfGroup = tenants.get(groupId);
                if (tenantOfGroup == null) {
                    logger.warning("Couldn't find tenant for user " + name + ". The tenant was identified by ID "
                            + groupId + " but no tenant with that ID was found");
                } else {
                    defaultTenant.put(serverName, tenantOfGroup);
                }
            }
        }
        DBObject accountsMap = (DBObject) userDBObject.get(FieldNames.User.ACCOUNTS.name());
        Map<AccountType, Account> accounts = createAccountMapFromdDBObject(accountsMap);
        UserImpl result = new UserImpl(name, email, fullName, company, locale,
                emailValidated == null ? false : emailValidated, passwordResetSecret, validationSecret, defaultTenant,
                accounts.values(), userGroupProvider);
        for (final Role role : roles) {
            result.addRole(role);
        }
        for (String permission : permissions) {
            result.addPermission(new WildcardPermission(permission));
        }
        if (rolesMigrated) {
            // update the user object after roles have been migrated;
            // the default tenant is only a dummy object but should be sufficient
            // for the DB update because, as for the read process, the write process
            // is also only interested in the object's ID
            new MongoObjectFactoryImpl(db).storeUser(result);
        }
        return result;
    }

    private Role loadRoleWithProxyUserQualifier(DBObject rolesO, Map<UUID, RoleDefinition> roleDefinitionsById, Map<UUID, UserGroup> userGroups) {
        final RoleDefinition roleDefinition = roleDefinitionsById.get(rolesO.get(FieldNames.Role.ID.name()));
        final UUID qualifyingTenantId = (UUID) rolesO.get(FieldNames.Role.QUALIFYING_TENANT_ID.name());
        final UserGroup qualifyingTenant = qualifyingTenantId == null ? null : userGroups.get(qualifyingTenantId);
        final User proxyQualifyingUser = rolesO.get(FieldNames.Role.QUALIFYING_USERNAME.name()) == null ? null
                : new UserProxy((String) rolesO.get(FieldNames.Role.QUALIFYING_USERNAME.name()));
        return new Role(roleDefinition, qualifyingTenant, proxyQualifyingUser);
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
