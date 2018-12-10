package com.sap.sse.security.userstore.mongodb.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;

import com.mongodb.BasicDBList;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.sap.sse.security.Social;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.SocialUserAccount;
import com.sap.sse.security.shared.UsernamePasswordAccount;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {

    private final MongoDatabase db;

    public MongoObjectFactoryImpl(MongoDatabase db) {
        this.db = db;
    }

    @Override
    public MongoDatabase getDatabase() {
        return db;
    }

    @Override
    public void storeUser(User user) {
        MongoCollection<org.bson.Document> usersCollection = db.getCollection(CollectionNames.USERS.name());
        usersCollection.createIndex(new Document(FieldNames.User.NAME.name(), 1));
        Document dbUser = new Document();
        Document query = new Document(FieldNames.User.NAME.name(), user.getName());
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
        usersCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(query, dbUser, new UpdateOptions().upsert(true));
    }
    
    @Override
    public void deleteUser(User user) {
        MongoCollection<org.bson.Document> usersCollection = db.getCollection(CollectionNames.USERS.name());
        Document dbUser = new Document();
        dbUser.put(FieldNames.User.NAME.name(), user.getName());
        usersCollection.deleteOne(dbUser);
    }

    private Document createAccountMapObject(Map<AccountType, Account> accounts) {
        Document dbAccounts = new Document();
        for (Entry<AccountType, Account> e : accounts.entrySet()) {
            dbAccounts.put(e.getKey().name(), createAccountObject(e.getValue()));
        }
        return dbAccounts;
    }

    private Document createAccountObject(Account a) {
        Document dbAccount = new Document();
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
        MongoCollection<org.bson.Document> settingCollection = db.getCollection(CollectionNames.SETTINGS.name());
        settingCollection.createIndex(new Document(FieldNames.Settings.NAME.name(), 1));
        Document dbSettings = new Document();
        Document query = new Document(FieldNames.Settings.NAME.name(), FieldNames.Settings.VALUES.name());
        dbSettings.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.VALUES.name());
        dbSettings.put(FieldNames.Settings.MAP.name(), createSettingsMapObject(settings));
        settingCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(query, dbSettings, new UpdateOptions().upsert(true));
    }

    @Override
    public void storePreferences(String username, Map<String, String> userMap) {
        MongoCollection<org.bson.Document> settingCollection = db.getCollection(CollectionNames.PREFERENCES.name());
        settingCollection.createIndex(new Document(FieldNames.Preferences.USERNAME.name(), 1));
        BasicDBList dbSettings = new BasicDBList();
        for (Entry<String, String> e : userMap.entrySet()) {
            Document entry = new Document();
            entry.put(FieldNames.Preferences.KEY.name(), e.getKey());
            entry.put(FieldNames.Preferences.VALUE.name(), e.getValue());
            dbSettings.add(entry);
        }
        Document query = new Document(FieldNames.Preferences.USERNAME.name(), username);
        Document update = new Document(FieldNames.Preferences.KEYS_AND_VALUES.name(), dbSettings);
        update.put(FieldNames.Preferences.USERNAME.name(), username);
        settingCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(query, update, new UpdateOptions().upsert(true));
    }

    @Override
    public void storeSettingTypes(Map<String, Class<?>> settingTypes) {
        MongoCollection<org.bson.Document> settingCollection = db.getCollection(CollectionNames.SETTINGS.name());
        settingCollection.createIndex(new Document(FieldNames.Settings.NAME.name(), 1));
        Document dbSettingTypes = new Document();
        Document query = new Document(FieldNames.Settings.NAME.name(), FieldNames.Settings.TYPES.name());
        dbSettingTypes.put(FieldNames.Settings.NAME.name(), FieldNames.Settings.TYPES.name());
        dbSettingTypes.put(FieldNames.Settings.MAP.name(), createSettingTypesMapObject(settingTypes));
        settingCollection.withWriteConcern(WriteConcern.ACKNOWLEDGED).updateOne(query, dbSettingTypes, new UpdateOptions().upsert(true));
    }

    private Document createSettingsMapObject(Map<String, Object> settings) {
        Document dbSettings = new Document();
        for (Entry<String, Object> e : settings.entrySet()) {
            dbSettings.put(e.getKey(), e.getValue());
        }
        return dbSettings;
    }

    private Document createSettingTypesMapObject(Map<String, Class<?>> settingTypes) {
        Document dbSettingTypes = new Document();
        for (Entry<String, Class<?>> e : settingTypes.entrySet()) {
            dbSettingTypes.put(e.getKey(), e.getValue().getName());
        }
        return dbSettingTypes;
    }
}
