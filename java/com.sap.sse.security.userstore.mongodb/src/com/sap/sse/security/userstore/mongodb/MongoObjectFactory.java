package com.sap.sse.security.userstore.mongodb;

import java.util.Map;
import java.util.UUID;

import com.mongodb.DB;
import com.sap.sse.security.User;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.UserGroup;

public interface MongoObjectFactory {
    public void storeAccessControlList(AccessControlList acl);
    
    public void deleteAccessControlList(AccessControlList acl);
    
    public void storeOwnership(Owner owner);
    
    public void deleteOwnership(Owner owner);
    
    public void storeRole(Role role);
    
    public void deleteRole(Role role);
    
    public void storeTenant(UUID id);
    
    public void deleteTenant(UUID id);
    
    public void storeUserGroup(UserGroup group);
    
    public void deleteUserGroup(UUID id);

    public void storeUser(User user);
    
    public void deleteUser(User user);
    
    public void storeSettings(Map<String, Object> settings);
    
    public void storeSettingTypes(Map<String, Class<?>> settingTypes);

    public void storePreferences(String username, Map<String, String> userMap);

    public DB getDatabase();
}
