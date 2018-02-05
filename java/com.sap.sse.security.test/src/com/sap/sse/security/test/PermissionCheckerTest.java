package com.sap.sse.security.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.security.UserImpl;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.PermissionBuilder.DefaultActions;
import com.sap.sse.security.shared.PermissionBuilderImpl;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.shared.impl.SecurityUserImpl;
import com.sap.sse.security.shared.impl.TenantImpl;

public class PermissionCheckerTest {
    private final String eventDataObjectType = "event";
    private final UUID eventId = UUID.randomUUID();
    private final WildcardPermission permission = 
            PermissionBuilderImpl.getInstance().getPermission(eventDataObjectType, 
                    DefaultActions.EDIT, eventId.toString());
    private final UUID userTenantId = UUID.randomUUID();
    private final UUID adminTenantId = UUID.randomUUID();
    private Tenant adminTenant;
    private SecurityUser adminUser;
    private Tenant userTenant;
    private User user;
    private ArrayList<UserGroup> tenants;
    private Ownership ownership;
    private Ownership adminOwnership;
    private AccessControlList acl;
    private final UUID globalRoleId = UUID.randomUUID();
    private RoleDefinition globalRoleDefinition;
    
    @Before
    public void setUp() {
        adminUser = new SecurityUserImpl("admin", adminTenant);
        user = new UserImpl("jonas", "jonas@dann.io", userTenant);
        userTenant = new TenantImpl(userTenantId, "jonas-tenant");
        userTenant.add(user);
        ownership = new OwnershipImpl(user, userTenant);
        adminTenant = new TenantImpl(adminTenantId, "admin-tenant");
        adminTenant.add(adminUser);
        adminOwnership = new OwnershipImpl(adminUser, adminTenant);
        tenants = new ArrayList<>();
        tenants.add(userTenant);
        tenants.add(adminTenant);
        acl = new AccessControlListImpl();
        Set<WildcardPermission> permissionSet = new HashSet<>();
        permissionSet.add(permission);
        globalRoleDefinition = new RoleDefinitionImpl(globalRoleId, "event", permissionSet);
    }
    
    @Test
    public void testOwnership() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, null, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, ownership, acl));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(DefaultActions.EDIT.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(permissionMap);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.addPermission(permission);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        permissionMap = new HashMap<>();
        permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultActions.EDIT.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(permissionMap);
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, ownership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.addPermission(permission);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        final RoleImpl globalRole = new RoleImpl(globalRoleDefinition);
        user.addRole(globalRole);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        user.removeRole(globalRole);
        user.addRole(new RoleImpl(globalRoleDefinition, this.userTenant, /* user qualifier */ null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, adminOwnership, acl));
        Ownership testOwnership = new OwnershipImpl(adminUser, userTenant);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, null, acl));
    }
}