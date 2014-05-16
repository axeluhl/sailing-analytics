package com.sap.sse.security;

import java.util.Collection;
import java.util.List;

import org.apache.shiro.mgt.SecurityManager;

import com.sap.sse.security.userstore.shared.User;

public interface SecurityService {

    SecurityManager getSecurityManager();
    
    Collection<User> getUserList();
    
    User getUserByName(String name);
    
    String login(String username, String password);
    
    void logout();
}
