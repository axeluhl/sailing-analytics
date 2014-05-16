package com.sap.sse.security.userstore.shared;

import java.util.Collection;


public interface UserStore {

    String getName();
    
    boolean createUser(String name, String password);
    
    Object getSalt(String name);
    
    String getSaltedPassword(String name);
    
    Collection<User> getUserCollection();
    User getUserByName(String name);
    
}
