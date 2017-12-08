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

import com.sap.sse.security.OwnershipImpl;
import com.sap.sse.security.TenantImpl;
import com.sap.sse.security.UserImpl;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.AccessControlListImpl;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.PermissionBuilder.DefaultActions;
import com.sap.sse.security.shared.PermissionBuilderImpl;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.SecurityUserImpl;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionCheckerTest implements RolePermissionModel {
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
    private ArrayList<Role> roles;
    private final RolePermissionModel rolePermissionModel = this;
    private Ownership ownership;
    private Ownership adminOwnership;
    private AccessControlList acl;
    private final UUID globalRoleId = UUID.randomUUID();
    private Role globalRole;
    private final UUID tenantRoleId = UUID.randomUUID();
    private Role tenantRole;
    private Map<UUID, Role> roleModel;
    
    @Before
    public void setUp() {
        adminUser = new SecurityUserImpl("admin", adminTenant);
        user = new UserImpl("jonas", "jonas@dann.io", userTenant);
        userTenant = new TenantImpl(userTenantId, "jonas-tenant");
        userTenant.add(user);
        ownership = new OwnershipImpl(eventId.toString(), user, userTenant, "event");
        adminTenant = new TenantImpl(adminTenantId, "admin-tenant");
        adminTenant.add(adminUser);
        adminOwnership = new OwnershipImpl(eventId.toString(), adminUser, adminTenant, "event");
        tenants = new ArrayList<>();
        tenants.add(userTenant);
        tenants.add(adminTenant);
        roles = new ArrayList<>();
        acl = new AccessControlListImpl(eventId.toString(), "event");
        Set<WildcardPermission> permissionSet = new HashSet<>();
        permissionSet.add(permission);
        roleModel = new HashMap<>();
        globalRole = new RoleImpl(globalRoleId, "event", permissionSet);
        roleModel.put(globalRoleId, globalRole);
        tenantRole = new RoleImpl(tenantRoleId, "event:" + userTenantId.toString(), permissionSet);
        roleModel.put(tenantRoleId, tenantRole);
    }
    
    @Test
    public void testOwnership() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                null, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                ownership, acl));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        
        Map<UserGroup, Set<String>> permissionMap = new HashMap<>();
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(DefaultActions.EDIT.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(eventId.toString(), "event", permissionMap);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        
        user.addPermission(permission);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        
        permissionMap = new HashMap<>();
        permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultActions.EDIT.name());
        permissionMap.put(userTenant, permissionSet);
        acl = new AccessControlListImpl(eventId.toString(), "event", permissionMap);
        
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                ownership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        user.addPermission(permission);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        roles.add(globalRole);
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        roles.remove(globalRole);
        roles.add(tenantRole);
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                adminOwnership, acl));
        Ownership testOwnership = new OwnershipImpl(eventId.toString(), adminUser, userTenant, "event");
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, roles, rolePermissionModel, 
                null, acl));
    }

    @Override
    public Iterable<WildcardPermission> getPermissions(UUID id) {
        return roleModel.get(id).getPermissions();
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return new ArrayList<Role>(roleModel.values());
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(Role role, WildcardPermission permission) {
        return implies(role, permission, null);
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(Role role, WildcardPermission permission, Ownership ownership) {
        String[] parts = role.getName().split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        // TODO consider user as Role parameter, comparing to ownership.getUserOwner()
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().getId().equals(UUID.fromString(parts[1])))) {
            for (WildcardPermission rolePermission : role.getPermissions()) {
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}