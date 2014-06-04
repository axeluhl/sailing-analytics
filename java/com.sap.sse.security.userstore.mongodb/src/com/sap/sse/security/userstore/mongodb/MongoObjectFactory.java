package com.sap.sse.security.userstore.mongodb;

import com.sap.sse.security.userstore.shared.User;

public interface MongoObjectFactory {

    public void storeUser(User user);
    
    public void deleteUser(User user);
}
