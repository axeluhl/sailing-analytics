package com.sap.sse.security.userstore.mongodb;

import java.util.Map;

import com.mongodb.DB;
import com.sap.sse.security.AccessControlList;
import com.sap.sse.security.Tenant;
import com.sap.sse.security.User;

public interface MongoObjectFactory {
    public void storeAccessControlList(AccessControlList acl);
    
    public void deleteAccessControlList(String name);
    
    public void storeTenant(Tenant tenant);
    
    public void deleteTenant(Tenant tenant);

    public void storeUser(User user);
    
    public void deleteUser(User user);
    
    public void storeSettings(Map<String, Object> settings);
    
    public void storeSettingTypes(Map<String, Class<?>> settingTypes);

    public void storePreferences(String username, Map<String, String> userMap);

    public DB getDatabase();
}
