package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

import com.sap.sse.security.userstore.shared.SimpleUser;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;


public class UserStoreImpl implements UserStore {
    
    private ConcurrentHashMap<String, SimpleUser> users;

    public UserStoreImpl() {
        users = new ConcurrentHashMap<>();
        try {
            createSimpleUser("Ben", "ben123");
            addRoleForUser("Ben", "admin");
            addRoleForUser("Ben", "moderator");
            createSimpleUser("Peter", "peter123");
            addRoleForUser("Peter", "moderator");
            createSimpleUser("Hans", "hans123");
            createSimpleUser("Hubert", "hubert123");
            createSimpleUser("Franz", "franz123");
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getName() {
        return "MongoDB user store";
    }

    @Override
    public SimpleUser createSimpleUser(String name, String password)  throws UserManagementException {
        if (users.get(name) != null){
            throw new UserManagementException(UserManagementException.USER_ALREADY_EXISTS);
        }
        if (name == null || password == null || name.length() < 3 || password.length() < 5){
            throw new UserManagementException(UserManagementException.INVALID_CREDENTIALS);
        }
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        String hashedPasswordBase64 = new Sha256Hash(password, salt, 1024).toBase64();
        SimpleUser user = new SimpleUser(name, hashedPasswordBase64, salt);
        users.put(name, user);
        return user;
    }

    @Override
    public Object getSalt(String name)  throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return users.get(name).getSalt();
    }

    @Override
    public String getSaltedPassword(String name)  throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
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

    @Override
    public Set<String> getRolesFromUser(String name) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        return users.get(name).getRoles();
    }

    @Override
    public void addRoleForUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).addRole(role);
    }

    @Override
    public void removeRoleFromUser(String name, String role) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.get(name).removeRole(role);
    }

    @Override
    public void deleteUser(String name) throws UserManagementException {
        if (users.get(name) == null){
            throw new UserManagementException(UserManagementException.USER_DOES_NOT_EXIST);
        }
        users.remove(name);
    }

}
