package com.sap.sse.security.userstore.mongodb.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.sap.sse.security.Social;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UsernamePasswordAccount;
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
