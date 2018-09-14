package com.sap.sse.security.userstore.mongodb;

import java.util.Map;

import com.mongodb.DB;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;

public interface MongoObjectFactory {
    public void storeAccessControlList(AccessControlListAnnotation acl);
    
    public void deleteAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, AccessControlList acl);
    
    public void storeOwnership(OwnershipAnnotation owner);
    
    public void deleteOwnership(QualifiedObjectIdentifier ownedObjectId, Ownership owner);
    
    public void storeRoleDefinition(RoleDefinition role);
    
    public void deleteRoleDefinition(RoleDefinition role);
    
    public void storeUserGroup(UserGroup group);
    
    public void deleteUserGroup(UserGroup userGroup);

    public void storeUser(User user);
    
    public void deleteUser(SecurityUser user);
    
    public void storeSettings(Map<String, Object> settings);
    
    public void storeSettingTypes(Map<String, Class<?>> settingTypes);

    public void storePreferences(String username, Map<String, String> userMap);

    public DB getDatabase();
}
