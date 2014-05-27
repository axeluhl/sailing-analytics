package com.sap.sse.security.userstore.shared;

import java.util.Collection;
import java.util.Set;


public interface UserStore {

    String getName();
    
    Object getSalt(String name) throws UserManagementException;
    
    String getSaltedPassword(String name) throws UserManagementException;
    
    Collection<User> getUserCollection();
    
    User getUserByName(String name);
    
    SimpleUser createSimpleUser(String name, String password) throws UserManagementException;
    
    Set<String> getRolesFromUser(String name) throws UserManagementException;
    
    void addRoleForUser(String name, String role) throws UserManagementException;
    void removeRoleFromUser(String name, String role) throws UserManagementException;
    
    void deleteUser(String name) throws UserManagementException;
    
    public static enum DefaultRoles {
        ADMIN("admin");
        
        private final String name;

        private DefaultRoles(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
