package com.sap.sse.security.userstore.mongodb;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.User;
import com.sap.sse.security.UserGroup;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;

public interface DomainObjectFactory {
    Iterable<Owner> loadAllOwnerships();
    
    Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore, AccessControlStore aclStore);
    
    AccessControlList loadAccessControlList(String id, UserStore userStore, AccessControlStore aclStore);

    Collection<String> loadAllTenantIds();
    
    Iterable<UserGroup> loadAllUserGroups();
    
    Iterable<User> loadAllUsers();
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    User loadUser(String name);

    Map<String, Map<String, String>> loadPreferences();

}
