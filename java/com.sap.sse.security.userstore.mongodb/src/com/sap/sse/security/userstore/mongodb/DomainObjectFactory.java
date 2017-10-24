package com.sap.sse.security.userstore.mongodb;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.User;
import com.sap.sse.security.UserGroup;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;

public interface DomainObjectFactory {
    Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore, AccessControlStore aclStore);
    
    AccessControlList loadAccessControlList(String id, UserStore userStore, AccessControlStore aclStore);

    Iterable<Owner> loadAllOwnerships();
    
    Iterable<Role> loadAllRoles();
    
    Collection<UUID> loadAllTenantIds();
    
    Iterable<UserGroup> loadAllUserGroups();
    
    Iterable<User> loadAllUsers();
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    User loadUser(String name);

    Map<String, Map<String, String>> loadPreferences();

}
