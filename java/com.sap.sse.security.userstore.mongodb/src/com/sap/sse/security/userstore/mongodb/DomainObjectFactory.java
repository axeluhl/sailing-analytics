package com.sap.sse.security.userstore.mongodb;

import java.util.Map;
import java.util.UUID;

import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroupProvider;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;
import com.sap.sse.security.shared.impl.UserGroupImpl;
import com.sap.sse.security.userstore.mongodb.impl.FieldNames.Tenant;

public interface DomainObjectFactory {
    @FunctionalInterface
    public interface RoleMigrationConverter {
        Role convert(String oldRoleName, String username);
    }
    
    Iterable<AccessControlListAnnotation> loadAllAccessControlLists(UserStore userStore);
    
    Iterable<OwnershipAnnotation> loadAllOwnerships(UserStore userStore);
    
    Iterable<RoleDefinition> loadAllRoleDefinitions();
    
    /**
     * Loads user groups and tenants from the persistent store. The users {@link UserGroupImpl#getUsers() contained} therein
     * are proxies and must be replaced by the caller once the real {@link SecurityUser} objects have been loaded from
     * the store. The proxies only have the correct {@link SecurityUser#getName() name} field set which also acts as the
     * {@link SecurityUser#getId() user ID}. {@link UserGroupImpl#remove(SecurityUser)} and
     * {@link UserGroupImpl#add(SecurityUser)} have to be used for this process.
     * <p>
     */
    Iterable<UserGroup> loadAllUserGroupsAndTenantsWithProxyUsers(Map<UUID, RoleDefinition> roleDefinitionsById);
    
    /**
     * @param roleMigrationConverter
     *            when a string-based role is found on the user object it will be mapped to a {@link Role} object using
     *            the given roleMigrationConverter. This implementation will take care of all migration specific, logic
     *            including correct scoping of roles. In case, an existing role can not be converted to the new role
     *            model, a warning must be logged.
     * @param userGroupProvider
     *            a way for the user object that will be created to dynamically obtain the user groups to which it
     *            belongs
     * @return the user objects returned have dummy objects for their {@link SecurityUser#getDefaultTenant() default
     *         tenant} and for their {@link SecurityUser#getRoles() roles} attribute which need to be replaced by the
     *         caller once the {@link Tenant} objects and all user objects have been loaded from the DB. The only field
     *         that is set correctly in those dummy {@link Tenant} objects is their {@link Tenant#getId() ID} field. The
     *         {@link Role} objects returned from the {@link SecurityUser#getRoles()} method can be expected to have
     *         valid {@link Role#getRoleDefinition() role definitions} attached; for the
     *         {@link Role#getQualifiedForTenant()} and {@link Role#getQualifiedForUser()} fields callers can only
     *         expect valid IDs to be set; those objects need to be resolved against the full set of tenants and users
     *         loaded at a later point in time.
     */
    Iterable<User> loadAllUsers(Map<UUID, RoleDefinition> roleDefinitionsById, RoleMigrationConverter roleMigrationConverter,
            Map<UUID, UserGroup> userGroups, UserGroupProvider userGroupProvider) throws UserManagementException;
    
    Map<String, Object> loadSettings();
    
    Map<String, Class<?>> loadSettingTypes();
    
    Map<String, Map<String, String>> loadPreferences();


}
