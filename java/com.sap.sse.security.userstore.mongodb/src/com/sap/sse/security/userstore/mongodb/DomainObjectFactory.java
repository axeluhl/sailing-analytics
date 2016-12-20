package com.sap.sse.security.userstore.mongodb;

import java.util.Collection;
import java.util.Map;

import com.sap.sse.security.AccessControlList;
import com.sap.sse.security.AccessControlListStore;
import com.sap.sse.security.User;
import com.sap.sse.security.UserGroup;
import com.sap.sse.security.UserStore;

public interface DomainObjectFactory {
    Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore, AccessControlListStore aclStore);
    
    AccessControlList loadAccessControlList(String name, UserStore userStore, AccessControlListStore aclStore);

    Collection<String> loadAllTenantnames();
    
    Iterable<UserGroup> loadAllUserGroups(AccessControlListStore aclStore);
    
    Iterable<User> loadAllUsers();
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    User loadUser(String name);

    Map<String, Map<String, String>> loadPreferences();

}
