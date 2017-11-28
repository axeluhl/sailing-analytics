package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.OwnerImpl;
import com.sap.sse.security.AccessControlListImpl;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.WildcardPermission;

public class AccessControlStoreImpl implements AccessControlStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlStoreImpl.class.getName());
    
    private String name = "Access control store";
    
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    private final ConcurrentHashMap<String, Owner> ownershipList;
    
    private final ConcurrentHashMap<UUID, Role> roleList;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    
    public AccessControlStoreImpl() {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory) {
        accessControlLists = new ConcurrentHashMap<>();
        ownershipList = new ConcurrentHashMap<>();
        roleList = new ConcurrentHashMap<>();
        
        this.mongoObjectFactory = mongoObjectFactory;
        
        if (domainObjectFactory != null) {
            for (AccessControlList acl : domainObjectFactory.loadAllAccessControlLists()) {
                accessControlLists.put(acl.getId().toString(), acl);
            }
            for (Owner ownership : domainObjectFactory.loadAllOwnerships()) {
                ownershipList.put(ownership.getId().toString(), ownership);
            }
            for (Role role : domainObjectFactory.loadAllRoles()) {
                roleList.put(UUID.fromString(role.getId().toString()), role);
            }
        }
    }
    
    @Override
    public Iterable<AccessControlList> getAccessControlLists() {
        return new ArrayList<>(accessControlLists.values());
    }
    
    @Override
    public AccessControlList getAccessControlList(String idAsString) {
        return accessControlLists.get(idAsString);
    }

    @Override
    public AccessControlList createAccessControlList(String idAsString, String displayName) {
        AccessControlList acl = new AccessControlListImpl(idAsString, displayName);
        accessControlLists.put(idAsString, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public AccessControlStore setAclPermissions(String idAsString, UUID group, Set<String> permissions) {
        AccessControlList acl = accessControlLists.get(idAsString);
        Map<UUID, Set<String>> permissionMap = acl.getPermissionMap();
        permissionMap.put(group, permissions);
        acl = new AccessControlListImpl(idAsString, acl.getDisplayName(), permissionMap);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlStore addAclPermission(String idAsString, UUID group, String permission) {
        AccessControlList acl = accessControlLists.get(idAsString);
        Map<UUID, Set<String>> permissionMap = acl.getPermissionMap();
        Set<String> permissionsGroup = permissionMap.get(group);
        if (permissionsGroup == null) {
            permissionsGroup = new HashSet<>();
            permissionMap.put(group, permissionsGroup);
        }
        permissionsGroup.add(permission);
        acl = new AccessControlListImpl(idAsString, acl.getDisplayName(), permissionMap);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlStore removeAclPermission(String idAsString, UUID group, String permission) {
        AccessControlList acl = accessControlLists.get(idAsString);
        Map<UUID, Set<String>> permissionMap = acl.getPermissionMap();
        Set<String> permissionsGroup = permissionMap.get(group);
        if (permissionsGroup != null) {
            permissionsGroup.remove(permission);
            acl = new AccessControlListImpl(idAsString, acl.getDisplayName(), permissionMap);
            mongoObjectFactory.storeAccessControlList(acl);
        }
        return this;
    }

    @Override
    public AccessControlStore removeAccessControlList(String idAsString) {
        AccessControlList acl = accessControlLists.remove(idAsString);
        mongoObjectFactory.deleteAccessControlList(acl);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Owner createOwnership(String idAsString, String owner, UUID tenantOwner, String displayName) {
        setOwnership(idAsString, owner, tenantOwner, displayName);
        return ownershipList.get(idAsString);
    }

    @Override
    public AccessControlStore setOwnership(String idAsString, String owner, UUID tenantOwner, String displayName) {
        Owner ownership = new OwnerImpl(idAsString, owner, tenantOwner, displayName);
        ownershipList.put(idAsString, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return this;
    }

    @Override
    public AccessControlStore removeOwnership(String idAsString) {
        Owner ownership = ownershipList.remove(idAsString);
        mongoObjectFactory.deleteOwnership(ownership);
        return this;
    }

    @Override
    public Owner getOwnership(String idAsString) {
        return ownershipList.get(idAsString);
    }
    
    @Override 
    public Iterable<Owner> getOwnerships() {
        return new ArrayList<>(ownershipList.values());
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return new ArrayList<>(roleList.values());
    }

    @Override
    public Role getRole(UUID id) {
        return roleList.get(id);
    }

    @Override
    public Role createRole(UUID id, String displayName, Set<WildcardPermission> permissions) {
        Role role = new RoleImpl(id, displayName, permissions);
        roleList.put(id, role);
        mongoObjectFactory.storeRole(role);
        return role;
    }

    @Override
    public AccessControlStore setRolePermissions(UUID id, Set<WildcardPermission> permissions) {
        Role role = roleList.get(id);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore addRolePermission(UUID id, WildcardPermission permission) {
        Role role = roleList.get(id);
        Set<WildcardPermission> permissions = role.getPermissions();
        permissions.add(permission);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore removeRolePermission(UUID id, WildcardPermission permission) {
        Role role = roleList.get(id);
        Set<WildcardPermission> permissions = role.getPermissions();
        permissions.remove(permission);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore setRoleDisplayName(UUID id, String displayName) {
        Role role = roleList.get(id);
        role = new RoleImpl(id, displayName, role.getPermissions());
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore removeRole(UUID id) {
        mongoObjectFactory.deleteRole(roleList.get(id));
        return this;
    }

    @Override
    public void clear() {
        accessControlLists.clear();
        ownershipList.clear();        
    }

    @Override
    public void replaceContentsFrom(AccessControlStore newAccessControlStore) {
        clear();
        for (AccessControlList acl : newAccessControlStore.getAccessControlLists()) {
            accessControlLists.put(acl.getId().toString(), acl);
        }
        for (Owner ownership : newAccessControlStore.getOwnerships()) {
            ownershipList.put(ownership.getId().toString(), ownership);
        }
    }
}
