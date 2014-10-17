package com.sap.sse.security.userstore.mongodb;

import java.util.Map;

import com.sap.sse.security.shared.User;

public interface DomainObjectFactory {

    Iterable<User> loadAllUsers();
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    User loadUser(String name);
}
