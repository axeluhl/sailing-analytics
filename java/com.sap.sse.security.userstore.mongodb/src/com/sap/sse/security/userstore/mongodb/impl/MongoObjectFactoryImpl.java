package com.sap.sse.security.userstore.mongodb.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.sap.sse.security.Social;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {

    private final DB db;

    public MongoObjectFactoryImpl(DB db) {
        this.db = db;
    }

    @Override
    public DB getDatabase() {
        return db;
    }
    
    @Override
    public void storeAccessControlList(AccessControlListAnnotation acl) {
        DBCollection aclCollection = db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name());
        aclCollection.createIndex(new BasicDBObject(FieldNames.AccessControlList.OBJECT_ID.name(), 1));
        DBObject dbACL = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.AccessControlList.OBJECT_ID.name(), acl.getIdOfAnnotatedObject().toString());
        dbACL.put(FieldNames.AccessControlList.OBJECT_ID.name(), acl.getIdOfAnnotatedObject().toString());
        dbACL.put(FieldNames.AccessControlList.OBJECT_DISPLAY_NAME.name(), acl.getDisplayNameOfAnnotatedObject());
        BasicDBList permissionMap = new BasicDBList();
        for (Entry<UserGroup, Set<String>> entry : acl.getAnnotation().getActionsByUserGroup().entrySet()) {
            DBObject permissionMapEntry = new BasicDBObject();
            permissionMapEntry.put(FieldNames.AccessControlList.PERMISSION_MAP_USER_GROUP_ID.name(), entry.getKey().getId());
            final BasicDBList dbActions = new BasicDBList();
            dbActions.addAll(entry.getValue());
            permissionMapEntry.put(FieldNames.AccessControlList.PERMISSION_MAP_ACTIONS.name(), dbActions);
            permissionMap.add(permissionMapEntry);
        }
        dbACL.put(FieldNames.AccessControlList.PERMISSION_MAP.name(), permissionMap);
        aclCollection.update(query, dbACL, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, AccessControlList acl) {
        DBCollection aclCollection = db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name());
        DBObject dbACL = new BasicDBObject();
        dbACL.put(FieldNames.AccessControlList.OBJECT_ID.name(), idOfAccessControlledObject.toString());
        aclCollection.remove(dbACL);
    }
    
    @Override
    public void storeOwnership(OwnershipAnnotation owner) {
        DBCollection ownershipCollection = db.getCollection(CollectionNames.OWNERSHIPS.name());
        ownershipCollection.createIndex(new BasicDBObject(FieldNames.Ownership.OBJECT_ID.name(), 1));
        DBObject dbOwnership = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Ownership.OBJECT_ID.name(), owner.getIdOfAnnotatedObject().toString());
        dbOwnership.put(FieldNames.Ownership.OBJECT_ID.name(), owner.getIdOfAnnotatedObject().toString());
        dbOwnership.put(FieldNames.Ownership.OWNER_USERNAME.name(), owner.getAnnotation().getUserOwner()==null?null:owner.getAnnotation().getUserOwner().getName());
        dbOwnership.put(FieldNames.Ownership.TENANT_OWNER_ID.name(), owner.getAnnotation().getTenantOwner()==null?null:owner.getAnnotation().getTenantOwner().getId());
        dbOwnership.put(FieldNames.Ownership.OBJECT_DISPLAY_NAME.name(), owner.getDisplayNameOfAnnotatedObject());
        ownershipCollection.update(query, dbOwnership, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void deleteOwnership(QualifiedObjectIdentifier ownedObjectId, Ownership ownership) {
        DBCollection ownershipCollection = db.getCollection(CollectionNames.OWNERSHIPS.name());
        DBObject dbOwnership = new BasicDBObject();
        dbOwnership.put(FieldNames.Ownership.OBJECT_ID.name(), ownedObjectId.toString());
        ownershipCollection.remove(dbOwnership);
    }

    @Override
    public void storeRoleDefinition(RoleDefinition role) {
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        roleCollection.createIndex(new BasicDBObject(FieldNames.Role.ID.name(), 1));
        DBObject dbRole = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Role.ID.name(), role.getId().toString());
        dbRole.put(FieldNames.Role.ID.name(), role.getId().toString());
        dbRole.put(FieldNames.Role.NAME.name(), role.getName());
        HashSet<String> stringPermissions = new HashSet<>();
        for (WildcardPermission permission : role.getPermissions()) {
            stringPermissions.add(permission.toString());
        }
        dbRole.put(FieldNames.Role.PERMISSIONS.name(), stringPermissions);
        roleCollection.update(query, dbRole, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void deleteRoleDefinition(RoleDefinition role) {
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        DBObject dbRole = new BasicDBObject();
        dbRole.put(FieldNames.Role.ID.name(), role.getId().toString());
        roleCollection.remove(dbRole);
    }
    
    private DBObject storeRole(Role role) {
        final DBObject result = new BasicDBObject();
        result.put(FieldNames.Role.ID.name(), role.getRoleDefinition().getId());
        result.put(FieldNames.Role.NAME.name(), role.getRoleDefinition().getName()); // for human readability only
        result.put(FieldNames.Role.QUALIFYING_TENANT_ID.name(), role.getQualifiedForTenant()==null?null:role.getQualifiedForTenant().getId());
        result.put(FieldNames.Role.QUALIFYING_TENANT_NAME.name(), role.getQualifiedForTenant()==null?null:role.getQualifiedForTenant().getName());
        result.put(FieldNames.Role.QUALIFYING_USERNAME.name(), role.getQualifiedForUser()==null?null:role.getQualifiedForUser().getName());
        return result;
    }
    
    @Override
    public void storeUserGroup(UserGroup group) {
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        userGroupCollection.createIndex(new BasicDBObject(FieldNames.UserGroup.ID.name(), 1));
        DBObject dbUserGroup = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.UserGroup.ID.name(), group.getId());
        dbUserGroup.put(FieldNames.UserGroup.ID.name(), group.getId());
        dbUserGroup.put(FieldNames.UserGroup.NAME.name(), group.getName());
        BasicDBList dbUsernames = new BasicDBList();
        for (SecurityUser user : group.getUsers()) {
            dbUsernames.add(user.getName());
        }
        dbUserGroup.put(FieldNames.UserGroup.USERNAMES.name(), dbUsernames);
        userGroupCollection.update(query, dbUserGroup, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteUserGroup(UserGroup userGroup) {
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        DBObject dbUserGroup = new BasicDBObject();
        dbUserGroup.put(FieldNames.UserGroup.ID.name(), userGroup.getId());
        userGroupCollection.remove(dbUserGroup);
    }

    @Override
    public void storeUser(User user) {
        DBCollection usersCollection = db.getCollection(CollectionNames.USERS.name());
        usersCollection.createIndex(new BasicDBObject(FieldNames.User.NAME.name(), 1));
        DBObject dbUser = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.User.NAME.name(), user.getName());
        dbUser.put(FieldNames.User.NAME.name(), user.getName());
        dbUser.put(FieldNames.User.EMAIL.name(), user.getEmail());
        dbUser.put(FieldNames.User.FULLNAME.name(), user.getFullName());
        dbUser.put(FieldNames.User.COMPANY.name(), user.getCompany());
        dbUser.put(FieldNames.User.LOCALE.name(), user.getLocale() != null ? user.getLocale().toLanguageTag() : null);
        dbUser.put(FieldNames.User.EMAIL_VALIDATED.name(), user.isEmailValidated());
        dbUser.put(FieldNames.User.PASSWORD_RESET_SECRET.name(), user.getPasswordResetSecret());
        dbUser.put(FieldNames.User.VALIDATION_SECRET.name(), user.getValidationSecret());
        dbUser.put(FieldNames.User.ACCOUNTS.name(), createAccountMapObject(user.getAllAccounts()));
        BasicDBList dbRoles = new BasicDBList();
        for (Role role : user.getRoles()) {
            dbRoles.add(storeRole(role));
        }
        dbUser.put(FieldNames.User.ROLE_IDS.name(), dbRoles);
        BasicDBList dbPermissions = new BasicDBList();
        for (WildcardPermission permission : user.getPermissions()) {
            dbPermissions.add(permission.toString());
        }
        dbUser.put(FieldNames.User.PERMISSIONS.name(), dbPermissions);

        List<Object> defaultTennants = new BasicDBList();
        for(Entry<String, UserGroup> entries:user.getDefaultTenantMap().entrySet()) {
            BasicDBObject tenant = new BasicDBObject();
            tenant.put(FieldNames.User.DEFAULT_TENANT_SERVER.name(), entries.getKey());
            tenant.put(FieldNames.User.DEFAULT_TENANT_GROUP.name(), entries.getValue().getId());
            defaultTennants.add(tenant);
        }
        dbUser.put(FieldNames.User.DEFAULT_TENANT_IDS.name(), defaultTennants);
        usersCollection.update(query, dbUser, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteUser(SecurityUser user) {
        DBCollection usersCollection = db.getCollection(CollectionNames.USERS.name());
        DBObject dbUser = new BasicDBObject();
        dbUser.put(FieldNames.User.NAME.name(), user.getName());
        usersCollection.remove(dbUser);
    }

    private DBObject createAccountMapObject(Map<AccountType, Account> accounts) {
        DBObject dbAccounts = new BasicDBObject();
        for (Entry<AccountType, Account> e : accounts.entrySet()) {
            dbAccounts.put(e.getKey().name(), createAccountObject(e.getValue()));
        }
        return dbAccounts;
    }

    private DBObject createAccountObject(Account a) {
        DBObject dbAccount = new BasicDBObject();
        if (a instanceof UsernamePasswordAccount) {
            UsernamePasswordAccount upa = (UsernamePasswordAccount) a;
            dbAccount.put(FieldNames.UsernamePassword.NAME.name(), upa.getName());
            dbAccount.put(FieldNames.UsernamePassword.SALTED_PW.name(), upa.getSaltedPassword());
            dbAccount.put(FieldNames.UsernamePassword.SALT.name(), upa.getSalt());
        }
        if (a instanceof SocialUserAccount) {
            SocialUserAccount account = (SocialUserAccount) a;
            for (Social s : Social.values()) {
                dbAccount.put(s.name(), account.getProperty(s.name()));
            }
        }
        return dbAccount;
    }

    @Override
    public void storeSettings(Map<String, Object> settings) {
        DBCollection settingCollection = db.getCollection(CollectionNames.SETTINGS.name());
        settingCollection.createIndex(new BasicDBObject(FieldNames.Settings.NAME.name(), 1));
        DBObject dbSettings = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Settings.NAME.name(), FieldNames.Settings.VALUES.name());
        dbSettings.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.VALUES.name());
        dbSettings.put(FieldNames.Settings.MAP.name(), createSettingsMapObject(settings));

        settingCollection.update(query, dbSettings, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void storePreferences(String username, Map<String, String> userMap) {
        DBCollection settingCollection = db.getCollection(CollectionNames.PREFERENCES.name());
        settingCollection.createIndex(new BasicDBObject(FieldNames.Preferences.USERNAME.name(), 1));
        BasicDBList dbSettings = new BasicDBList();
        for (Entry<String, String> e : userMap.entrySet()) {
            DBObject entry = new BasicDBObject();
            entry.put(FieldNames.Preferences.KEY.name(), e.getKey());
            entry.put(FieldNames.Preferences.VALUE.name(), e.getValue());
            dbSettings.add(entry);
        }
        DBObject query = new BasicDBObject(FieldNames.Preferences.USERNAME.name(), username);
        DBObject update = new BasicDBObject(FieldNames.Preferences.KEYS_AND_VALUES.name(), dbSettings);
        update.put(FieldNames.Preferences.USERNAME.name(), username);
        settingCollection.update(query, update, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void storeSettingTypes(Map<String, Class<?>> settingTypes) {
        DBCollection settingCollection = db.getCollection(CollectionNames.SETTINGS.name());
        settingCollection.createIndex(new BasicDBObject(FieldNames.Settings.NAME.name(), 1));
        DBObject dbSettingTypes = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Settings.NAME.name(), FieldNames.Settings.TYPES.name());
        dbSettingTypes.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.TYPES.name());
        dbSettingTypes.put(FieldNames.Settings.MAP.name(), createSettingTypesMapObject(settingTypes));

        settingCollection.update(query, dbSettingTypes, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    private DBObject createSettingsMapObject(Map<String, Object> settings) {
        DBObject dbSettings = new BasicDBObject();
        for (Entry<String, Object> e : settings.entrySet()) {
            dbSettings.put(e.getKey(), e.getValue());
        }
        return dbSettings;
    }

    private DBObject createSettingTypesMapObject(Map<String, Class<?>> settingTypes) {
        DBObject dbSettingTypes = new BasicDBObject();
        for (Entry<String, Class<?>> e : settingTypes.entrySet()) {
            dbSettingTypes.put(e.getKey(), e.getValue().getName());
        }
        return dbSettingTypes;
    }
}
