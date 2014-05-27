package com.sap.sse.security;

import java.util.Collection;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.SecurityManager;

import com.sap.sse.security.userstore.shared.SimpleUser;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;

public interface SecurityService {

    SecurityManager getSecurityManager();
    
    Collection<User> getUserList();
    
    User getUserByName(String name);
    
    String login(String username, String password)  throws AuthenticationException;
    
    void logout();
    
    SimpleUser createSimpleUser(String name, String password) throws UserManagementException;
    
    void deleteUser(String username) throws UserManagementException;
    
    Set<String> getRolesFromUser(String name) throws UserManagementException;
    
    void addRoleForUser(String name, String role) throws UserManagementException;
    void removeRoleFromUser(String name, String role) throws UserManagementException;
}
