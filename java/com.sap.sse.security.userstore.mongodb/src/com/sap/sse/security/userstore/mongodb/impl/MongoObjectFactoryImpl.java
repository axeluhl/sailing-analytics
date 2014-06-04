package com.sap.sse.security.userstore.mongodb.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.util.SimpleByteSource;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.sap.sse.security.userstore.mongodb.MongoObjectFactory;
import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class MongoObjectFactoryImpl implements MongoObjectFactory {

    private final DB db;

    public MongoObjectFactoryImpl(DB db) {
        this.db = db;
    }
    
    @Override
    public void storeUser(User user) {
        DBCollection usersCollection = db.getCollection(CollectionNames.USERS.name());
        usersCollection.ensureIndex(FieldNames.USER_NAME.name());
        DBObject dbUser = new BasicDBObject();
        DBObject query = new BasicDBObject(FieldNames.USER_NAME.name(), user.getName());
        dbUser.put(FieldNames.USER_NAME.name(), user.getName());
        dbUser.put(FieldNames.USER_EMAIL.name(), user.getEmail());
        dbUser.put(FieldNames.USER_ACCOUNTS.name(), createAccountMapObject(user.getAllAccounts()));
        dbUser.put(FieldNames.USER_ROLES.name(), user.getRoles());

        usersCollection.update(query, dbUser, /* upsrt */ true, /* multi */ false, WriteConcern.SAFE);
    }

    @Override
    public void deleteUser(User user) {
        DBCollection usersCollection = db.getCollection(CollectionNames.USERS.name());
        DBObject dbUser = new BasicDBObject();
        dbUser.put(FieldNames.USER_NAME.name(), user.getName());

        usersCollection.remove(dbUser);
    }
    
    private DBObject createAccountMapObject(Map<AccountType, Account> accounts){
        DBObject dbAccounts = new BasicDBObject();
        for (Entry<AccountType, Account> e : accounts.entrySet()){
            dbAccounts.put(e.getKey().name(), createAccountObject(e.getValue()));
        }
        return dbAccounts;
    }
    
    private DBObject createAccountObject(Account a){
        DBObject dbAccount = new BasicDBObject();
        if (a instanceof UsernamePasswordAccount){
            UsernamePasswordAccount upa = (UsernamePasswordAccount) a;
            dbAccount.put(FieldNames.USERNAME_PASSWORD_ACCOUNT_NAME.name(), upa.getName());
            dbAccount.put(FieldNames.USERNAME_PASSWORD_ACCOUNT_SALTED_PW.name(), upa.getSaltedPassword());
            dbAccount.put(FieldNames.USERNAME_PASSWORD_ACCOUNT_SALT.name(),((SimpleByteSource) upa.getSalt()).getBytes());
        }
        return dbAccount;
    }
}
