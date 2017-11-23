package com.sap.sse.security.userstore.mongodb.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.sap.sse.security.Social;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.SocialUserAccount;
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
    public void storeAccessControlList(AccessControlList acl) {
        DBCollection aclCollection = db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name());
        aclCollection.createIndex(new BasicDBObject(FieldNames.AccessControlList.ID.name(), 1));
        DBObject dbACL = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.AccessControlList.ID.name(), acl.getId().toString());
        dbACL.put(FieldNames.AccessControlList.ID.name(), acl.getId().toString());
        dbACL.put(FieldNames.AccessControlList.DISPLAY_NAME.name(), acl.getDisplayName());
        Map<String, Set<String>> permissionMap = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : acl.getPermissionMap().entrySet()) {
            permissionMap.put(entry.getKey().toString(), entry.getValue());
        }
        dbACL.put(FieldNames.AccessControlList.PERMISSION_MAP.name(), permissionMap);
        aclCollection.update(query, dbACL, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteAccessControlList(AccessControlList acl) {
        DBCollection aclCollection = db.getCollection(CollectionNames.ACCESS_CONTROL_LISTS.name());
        DBObject dbACL = new BasicDBObject();
        dbACL.put(FieldNames.AccessControlList.ID.name(), acl.getId().toString());
        aclCollection.remove(dbACL);
    }
    
    @Override
    public void storeOwnership(Owner owner) {
        DBCollection ownershipCollection = db.getCollection(CollectionNames.OWNERSHIPS.name());
        ownershipCollection.createIndex(new BasicDBObject(FieldNames.Ownership.ID.name(), 1));
        DBObject dbOwnership = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Ownership.ID.name(), owner.getId().toString());
        dbOwnership.put(FieldNames.Ownership.ID.name(), owner.getId().toString());
        dbOwnership.put(FieldNames.Ownership.OWNER.name(), owner.getOwner());
        dbOwnership.put(FieldNames.Ownership.TENANT_OWNER.name(), owner.getTenantOwner().toString());
        dbOwnership.put(FieldNames.Ownership.DISPLAY_NAME.name(), owner.getDisplayName());
        ownershipCollection.update(query, dbOwnership, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void deleteOwnership(Owner owner) {
        DBCollection ownershipCollection = db.getCollection(CollectionNames.OWNERSHIPS.name());
        DBObject dbOwnership = new BasicDBObject();
        dbOwnership.put(FieldNames.Ownership.ID.name(), owner.getId().toString());
        ownershipCollection.remove(dbOwnership);
    }

    @Override
    public void storeRole(Role role) {
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        roleCollection.createIndex(new BasicDBObject(FieldNames.Role.ID.name(), 1));
        DBObject dbRole = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Role.ID.name(), role.getId().toString());
        dbRole.put(FieldNames.Role.ID.name(), role.getId().toString());
        dbRole.put(FieldNames.Role.DISPLAY_NAME.name(), role.getDisplayName());
        HashSet<String> stringPermissions = new HashSet<>();
        for (WildcardPermission permission : role.getPermissions()) {
            stringPermissions.add(permission.toString());
        }
        dbRole.put(FieldNames.Role.PERMISSIONS.name(), role.getPermissions());
        roleCollection.update(query, dbRole, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void deleteRole(Role role) {
        DBCollection roleCollection = db.getCollection(CollectionNames.ROLES.name());
        DBObject dbRole = new BasicDBObject();
        dbRole.put(FieldNames.Role.ID.name(), role.getId().toString());
        roleCollection.remove(dbRole);
    }
    
    @Override
    public void storeTenant(UUID id) {
        DBCollection tenantCollection = db.getCollection(CollectionNames.TENANTS.name());
        tenantCollection.createIndex(new BasicDBObject(FieldNames.Tenant.ID.name(), 1));
        DBObject dbTenant = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.Tenant.ID.name(), id.toString());
        dbTenant.put(FieldNames.Tenant.ID.name(), id.toString());
        tenantCollection.update(query, dbTenant, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }

    @Override
    public void deleteTenant(UUID id) {
        DBCollection tenantCollection = db.getCollection(CollectionNames.TENANTS.name());
        DBObject dbTenant = new BasicDBObject();
        dbTenant.put(FieldNames.Tenant.ID.name(), id.toString());
        tenantCollection.remove(dbTenant);
    }
    
    @Override
    public void storeUserGroup(UserGroup group) {
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        userGroupCollection.createIndex(new BasicDBObject(FieldNames.UserGroup.ID.name(), 1));
        DBObject dbUserGroup = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.UserGroup.ID.name(), group.getId().toString());
        dbUserGroup.put(FieldNames.UserGroup.ID.name(), group.getId().toString());
        dbUserGroup.put(FieldNames.UserGroup.NAME.name(), group.getName());
        dbUserGroup.put(FieldNames.UserGroup.USERS.name(), group.getUsernames());
        userGroupCollection.update(query, dbUserGroup, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteUserGroup(UUID id) {
        DBCollection userGroupCollection = db.getCollection(CollectionNames.USER_GROUPS.name());
        DBObject dbUserGroup = new BasicDBObject();
        dbUserGroup.put(FieldNames.UserGroup.ID.name(), id.toString());
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
        dbUser.put(FieldNames.User.ROLES.name(), user.getRoles());
        dbUser.put(FieldNames.User.PERMISSIONS.name(), user.getPermissions());
        usersCollection.update(query, dbUser, /* upsrt */true, /* multi */false, WriteConcern.SAFE);
    }
    
    @Override
    public void deleteUser(User user) {
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
