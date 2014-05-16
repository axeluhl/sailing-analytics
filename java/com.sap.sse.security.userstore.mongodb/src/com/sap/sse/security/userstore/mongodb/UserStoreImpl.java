package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

import com.sap.sse.security.userstore.shared.SimpleUser;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserStore;


public class UserStoreImpl implements UserStore {
    
    private ConcurrentHashMap<String, SimpleUser> users;

    public UserStoreImpl() {
        users = new ConcurrentHashMap<>();
        createUser("Ben", "ben123");
        createUser("Peter", "peter123");
        createUser("Hans", "hans123");
    }
    
    @Override
    public String getName() {
        return "MongoDB user store";
    }

    @Override
    public boolean createUser(String name, String password) {
        if (users.get(name) != null){
            return false; // This user already exists
        }
        if (name == null || password == null || name.length() < 3 || password.length() < 5){
            return false; //Invalid credentials
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        SimpleUser user = new SimpleUser(name, hashedPasswordBase64, salt);
        users.put(name, user);
        return true;
    }

    @Override
    public Object getSalt(String name) {
        if (users.get(name) == null){
            return null;
        }
        return users.get(name).getSalt();
    }

    @Override
    public String getSaltedPassword(String name) {
        if (users.get(name) == null){
            return null;
        }
        return users.get(name).getSaltedPassword();
    }

    @Override
    public Collection<User> getUserCollection() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public User getUserByName(String name) {
        return users.get(name);
    }

}
