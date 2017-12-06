package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.OwnershipImpl;
import com.sap.sse.security.AccessControlListImpl;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.WildcardPermission;

public class AccessControlStoreImpl implements AccessControlStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlStoreImpl.class.getName());
    
    private String name = "Access control store";
    
    /**
     * 
     */
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    private final ConcurrentHashMap<String, Ownership> ownerships;
    
    private final ConcurrentHashMap<UUID, Role> roles;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    
    public AccessControlStoreImpl() {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory());
    }
    
    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory) {
        accessControlLists = new ConcurrentHashMap<>();
        ownerships = new ConcurrentHashMap<>();
        roles = new ConcurrentHashMap<>();
        
        this.mongoObjectFactory = mongoObjectFactory;
        
        if (domainObjectFactory != null) {
            for (AccessControlList acl : domainObjectFactory.loadAllAccessControlLists()) {
                accessControlLists.put(acl.getId().toString(), acl);
            }
            for (Ownership ownership : domainObjectFactory.loadAllOwnerships()) {
                ownerships.put(ownership.getIdOfOwnedObjectAsString(), ownership);
            }
            for (Role role : domainObjectFactory.loadAllRoles()) {
                roles.put(UUID.fromString(role.getId().toString()), role);
            }
        }
    }
    
    @Override
    public Iterable<AccessControlList> getAccessControlLists() {
        return new ArrayList<>(accessControlLists.values());
    }
    
    @Override
    public AccessControlList getAccessControlList(String idOfAccessControlledObjectAsString) {
        return accessControlLists.get(idOfAccessControlledObjectAsString);
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
    public Ownership createOwnership(String idAsString, String owner, UUID tenantOwner, String displayName) {
        setOwnership(idAsString, owner, tenantOwner, displayName);
        return ownerships.get(idAsString);
    }

    @Override
    public AccessControlStore setOwnership(String idAsString, String owner, UUID tenantOwner, String displayName) {
        Ownership ownership = new OwnershipImpl(idAsString, owner, tenantOwner, displayName);
        ownerships.put(idAsString, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return this;
    }

    @Override
    public AccessControlStore removeOwnership(String idAsString) {
        Ownership ownership = ownerships.remove(idAsString);
        mongoObjectFactory.deleteOwnership(ownership);
        return this;
    }

    @Override
    public Ownership getOwnership(String idAsString) {
        return ownerships.get(idAsString);
    }
    
    @Override 
    public Iterable<Ownership> getOwnerships() {
        return new ArrayList<>(ownerships.values());
    }
    
    @Override
    public Iterable<Role> getRoles() {
        return new ArrayList<>(roles.values());
    }

    @Override
    public Role getRole(UUID id) {
        return roles.get(id);
    }

    @Override
    public Role createRole(UUID id, String displayName, Set<WildcardPermission> permissions) {
        Role role = new RoleImpl(id, displayName, permissions);
        roles.put(id, role);
        mongoObjectFactory.storeRole(role);
        return role;
    }

    @Override
    public AccessControlStore setRolePermissions(UUID id, Set<WildcardPermission> permissions) {
        Role role = roles.get(id);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore addRolePermission(UUID id, WildcardPermission permission) {
        Role role = roles.get(id);
        Set<WildcardPermission> permissions = role.getPermissions();
        permissions.add(permission);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore removeRolePermission(UUID id, WildcardPermission permission) {
        Role role = roles.get(id);
        Set<WildcardPermission> permissions = role.getPermissions();
        permissions.remove(permission);
        role = new RoleImpl(id, role.getName(), permissions);
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore setRoleDisplayName(UUID id, String displayName) {
        Role role = roles.get(id);
        role = new RoleImpl(id, displayName, role.getPermissions());
        mongoObjectFactory.storeRole(role);
        return this;
    }

    @Override
    public AccessControlStore removeRole(UUID id) {
        mongoObjectFactory.deleteRole(roles.get(id));
        roles.remove(id);
        return this;
    }

    @Override
    public void clear() {
        accessControlLists.clear();
        ownerships.clear();        
    }

    @Override
    public void replaceContentsFrom(AccessControlStore newAccessControlStore) {
        clear();
        for (AccessControlList acl : newAccessControlStore.getAccessControlLists()) {
            accessControlLists.put(acl.getId().toString(), acl);
        }
        for (Ownership ownership : newAccessControlStore.getOwnerships()) {
            ownerships.put(ownership.getIdOfOwnedObjectAsString(), ownership);
        }
    }
}
