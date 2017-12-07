package com.sap.sse.security.userstore.mongodb;

import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;

public interface DomainObjectFactory {
    Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore);
    
    Iterable<Ownership> loadAllOwnerships(UserStore userStore);
    
    Iterable<Role> loadAllRoles();
    
    /**
     * {@link Tenant}s are special {@link UserGroup}s whose content is stored in the same DB collection as the
     * {@link UserGroup} objects, but in addition their ID is kept in a separate table, thus marking them as tenants.
     * 
     * @param usersByName
     *            the already loaded user base that can be used to resolve user names found in the group's DB
     *            representation to {@code User} objects
     * 
     * @return those {@link UserGroup}s that are not {@link Tenant}s in the first component of the pair; all
     *         {@link Tenant}s found in the second component
     */
    Pair<Iterable<UserGroup>, Iterable<Tenant>> loadAllUserGroupsAndTenants(Map<String, UserImpl> usersByName);
    
    /**
     * @return the user objects returned have dummy objects for their {@link SecurityUser#getDefaultTenant() default
     *         tenant} attribute which need to be replaced by the caller once the {@link Tenant} objects have been
     *         loaded from the DB. The only field that is set correctly in those dummy {@link Tenant} objects
     *         is their {@link Tenant#getId() ID} field.
     */
    Iterable<UserImpl> loadAllUsers(Map<UUID, Role> rolesById);
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    Map<String, Map<String, String>> loadPreferences();


}
