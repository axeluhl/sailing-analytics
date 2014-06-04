package com.sap.sse.security.userstore.mongodb.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.shiro.util.SimpleByteSource;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sse.security.userstore.mongodb.DomainObjectFactory;
import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class DomainObjectFactoryImpl implements DomainObjectFactory {
    private static final Logger logger = Logger.getLogger(DomainObjectFactoryImpl.class.getName());

    private final DB db;

    public DomainObjectFactoryImpl(DB db) {
        this.db = db;
    }

    @Override
    public Iterable<User> loadAllUsers() {
        ArrayList<User> result = new ArrayList<User>();
        DBCollection userCollection = db.getCollection(CollectionNames.USERS.name());

        try {
            for (DBObject o : userCollection.find()) {
                result.add(loadUser(o));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error connecting to MongoDB, unable to load events.");
            logger.log(Level.SEVERE, "loadAllEvents", e);
        }

        return result;
    }
    
    @Override
    public User loadUser(String name) {
        User result;
        BasicDBObject query = new BasicDBObject();
        query.put(FieldNames.USER_NAME.name(), name);
        DBCollection userCollection = db.getCollection(CollectionNames.USERS.name());
        DBObject userDBObject = userCollection.findOne(query);
        if (userDBObject != null) {
            result = loadUser(userDBObject);
        } else {
            result = null;
        }
        return result;
    }
    
    private User loadUser(DBObject userDBObject) {
        String name = (String) userDBObject.get(FieldNames.USER_NAME.name());
        String email = (String) userDBObject.get(FieldNames.USER_EMAIL.name());
        Set<String> roles = new HashSet<String>((Collection<String>) userDBObject.get(FieldNames.USER_ROLES.name()));
        DBObject accountsMap = (DBObject) userDBObject.get(FieldNames.USER_ACCOUNTS.name());
        Map<AccountType, Account> accounts = createAccountMapFromdDBObject(accountsMap);
        User result = new User(name, email, accounts.values());
        result.setRoles(roles);
        return result;
    }

    private Map<AccountType, Account> createAccountMapFromdDBObject(DBObject accountsMap) {
        Map<AccountType, Account> accounts = new HashMap<>();
        Set<Entry<String, Object>> entrySet = accountsMap.toMap().entrySet();
        for (Entry<String, Object> e : entrySet){
            AccountType type = AccountType.valueOf(e.getKey());
            Account account = createAccountFromDBObject((DBObject) e.getValue(), type);
            accounts.put(type, account);
        }
        return accounts;
    }

    private Account createAccountFromDBObject(DBObject dbAccount, final AccountType type) {
        switch (type) {
        case USERNAME_PASSWORD:
            String name = (String) dbAccount.get(FieldNames.USERNAME_PASSWORD_ACCOUNT_NAME.name());
            String saltedPassword = (String) dbAccount.get(FieldNames.USERNAME_PASSWORD_ACCOUNT_SALTED_PW.name());
            Object salt = new SimpleByteSource((byte[]) dbAccount.get(FieldNames.USERNAME_PASSWORD_ACCOUNT_SALT.name()));
            return new UsernamePasswordAccount(name, saltedPassword, salt);
            //TODO [D056866] add other Account-types
        default:
            return null;
        }
    }
}
