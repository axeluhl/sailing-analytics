package com.sap.sse.security.userstore.mongodb;

import java.util.Map;
import java.util.UUID;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;

public interface DomainObjectFactory {
    Iterable<AccessControlList> loadAllAccessControlLists(UserStore userStore);
    
    Iterable<Ownership> loadAllOwnerships(UserStore userStore);
    
    Iterable<RoleDefinition> loadAllRoleDefinitions();
    
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
     *         tenant} and for their {@link SecurityUser#getRoles() roles} attribute which need to be replaced by the
     *         caller once the {@link Tenant} objects and all user objects have been loaded from the DB. The only field
     *         that is set correctly in those dummy {@link Tenant} objects is their {@link Tenant#getId() ID} field.
     *         The {@link Role} objects returned from the {@link SecurityUser#getRoles()} method can be expected to have
     *         valid {@link Role#getRoleDefinition() role definitions} attached; for the {@link Role#getQualifiedForTenant()}
     *         and {@link Role#getQualifiedForUser()} fields callers can only expect valid IDs to be set; those objects need
     *         to be resolved against the full set of tenants and users loaded at a later point in time.
     */
    Iterable<UserImpl> loadAllUsers(Map<UUID, RoleDefinition> roleDefinitionsById);
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    Map<String, Map<String, String>> loadPreferences();


}
