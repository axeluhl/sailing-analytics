package com.sap.sse.security.userstore.mongodb;

import java.util.Map;

import com.sap.sse.security.Tenant;
import com.sap.sse.security.User;

public interface DomainObjectFactory {

    Iterable<Tenant> loadAllTenants();
    
    Iterable<User> loadAllUsers();
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    User loadUser(String name);

    Map<String, Map<String, String>> loadPreferences();

}
