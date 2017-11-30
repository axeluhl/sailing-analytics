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

import com.sap.sse.security.AccessControlListImpl;
import com.sap.sse.security.OwnerImpl;
import com.sap.sse.security.Tenant;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.PermissionBuilder.DefaultActions;
import com.sap.sse.security.shared.PermissionBuilderImpl;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.RolePermissionModel;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;

public class PermissionCheckerTest implements RolePermissionModel {
    private final String eventDataObjectType = "event";
    private final UUID eventId = UUID.randomUUID();
    private final WildcardPermission permission = 
            PermissionBuilderImpl.getInstance().getPermission(eventDataObjectType, 
                    DefaultActions.EDIT, eventId.toString());
    private final String user = "jonas";
    private final String adminUser = "admin";
    private final UUID userTenantId = UUID.randomUUID();
    private final UUID adminTenantId = UUID.randomUUID();
    private Tenant userTenant = new Tenant(userTenantId, user + "-tenant");
    private Tenant adminTenant = new Tenant(adminTenantId, adminUser + "-tenant");
    private ArrayList<UserGroup> tenants;
    private ArrayList<WildcardPermission> directPermissions;
    private ArrayList<UUID> roles;
    private final RolePermissionModel rolePermissionModel = this;
    private final Owner ownership = new OwnerImpl(eventId.toString(), user, (UUID) userTenant.getId(), "event");
    private final Owner adminOwnership = new OwnerImpl(eventId.toString(), adminUser, (UUID) adminTenant.getId(), "event");
    private AccessControlList acl;
    private final UUID globalRoleId = UUID.randomUUID();
    private Role globalRole;
    private final UUID tenantRoleId = UUID.randomUUID();
    private Role tenantRole;
    private Map<UUID, Role> roleModel;
    
    @Before
    public void setUp() {
        userTenant.add(user);
        adminTenant.add(adminUser);
        tenants = new ArrayList<>();
        tenants.add(userTenant);
        tenants.add(adminTenant);
        directPermissions = new ArrayList<>();
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
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, null, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, ownership, acl));
    }
    
    @Test
    public void testAccessControlList() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, null));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        Map<UUID, Set<String>> permissionMap = new HashMap<>();
        Set<String> permissionSet = new HashSet<>();
        permissionSet.add(DefaultActions.EDIT.name());
        permissionMap.put((UUID) userTenant.getId(), permissionSet);
        acl = new AccessControlListImpl(eventId.toString(), "event", permissionMap);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        directPermissions.add(permission);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        permissionMap = new HashMap<>();
        permissionSet = new HashSet<>();
        permissionSet.add("!" + DefaultActions.EDIT.name());
        permissionMap.put((UUID) userTenant.getId(), permissionSet);
        acl = new AccessControlListImpl(eventId.toString(), "event", permissionMap);
        
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, ownership, acl));
    }
    
    @Test
    public void testDirectPermission() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        directPermissions.add(permission);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
    }
    
    @Test
    public void testRole() {
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        roles.add(globalRoleId);
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        roles.remove(globalRoleId);
        roles.add(tenantRoleId);
        
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, adminOwnership, acl));
        
        Owner testOwnership = new OwnerImpl(eventId.toString(), adminUser, (UUID) userTenant.getId(), "event");
        
        assertTrue(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, testOwnership, acl));
        assertFalse(PermissionChecker.isPermitted(permission, user, tenants, directPermissions, roles, 
                rolePermissionModel, null, acl));
    }

    @Override
    public String getName(UUID id) {
        return roleModel.get(id).getName();
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
    public boolean implies(UUID id, WildcardPermission permission) {
        return implies(id, permission, null);
    }
    
    @Override
    public boolean implies(UUID id, WildcardPermission permission, Owner ownership) {
        return implies(id, roleModel.get(id).getName(), permission, ownership);
    }
    
    // TODO as default implementation in interface
    @Override
    public boolean implies(UUID id, String name, WildcardPermission permission, Owner ownership) {
        String[] parts = name.split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().equals(UUID.fromString(parts[1])))) {
            for (WildcardPermission rolePermission : getPermissions(id)) {
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}