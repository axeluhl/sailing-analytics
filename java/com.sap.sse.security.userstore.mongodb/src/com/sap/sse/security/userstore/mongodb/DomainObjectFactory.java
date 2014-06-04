package com.sap.sse.security.userstore.mongodb;

import com.sap.sse.security.userstore.shared.User;

public interface DomainObjectFactory {

    Iterable<User> loadAllUsers();
    
    User loadUser(String name);
}
