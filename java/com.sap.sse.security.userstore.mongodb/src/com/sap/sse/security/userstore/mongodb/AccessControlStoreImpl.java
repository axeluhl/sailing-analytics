package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.OwnerImpl;
import com.sap.sse.security.AccessControlListWithStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Owner;

public class AccessControlStoreImpl implements AccessControlStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlStoreImpl.class.getName());
    
    private String name = "Access control store";
    
    private UserStore userStore;
    
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    private final ConcurrentHashMap<String, Owner> ownershipList;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    private final transient DomainObjectFactory domainObjectFactory;
    
    public AccessControlStoreImpl(final UserStore userStore) {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
    }
    
    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory, final UserStore userStore) {
        accessControlLists = new ConcurrentHashMap<>();
        ownershipList = new ConcurrentHashMap<>();
        
        this.mongoObjectFactory = mongoObjectFactory;        
        this.domainObjectFactory = domainObjectFactory;
        
        if (domainObjectFactory != null) {
            for (Owner ownership : domainObjectFactory.loadAllOwnerships()) {
                ownershipList.put(ownership.getId().toString(), ownership);
            }
        }
        
        this.userStore = userStore;
    }
    
    @Override
    public Iterable<AccessControlList> getAccessControlLists() {
        return new ArrayList<>(accessControlLists.values());
    }
    
    /**
     * Gets the ACL by name and if it is not present loads it. However there should only be the need to load them when the user store is initialized
     */
    @Override
    public AccessControlList getAccessControlList(String id) {
        AccessControlList acl = accessControlLists.get(id);
        if (acl != null) {
            return acl;
        }
        acl = domainObjectFactory.loadAccessControlList(id, userStore, this);
        accessControlLists.put(acl.getId().toString(), acl);
        return acl;
    }

    @Override
    public AccessControlList createAccessControlList(String id, String displayName) {
        AccessControlList acl = new AccessControlListWithStore(id, displayName, userStore);
        accessControlLists.put(id, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public AccessControlStore putPermissions(String id, String group, Set<String> permissions) {
        AccessControlList acl = accessControlLists.get(id);
        Map<String, Set<String>> permissionMap = acl.getPermissionMap();
        permissionMap.put(group, permissions);
        acl = new AccessControlListWithStore(id, acl.getDisplayName(), permissionMap, userStore);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlStore addPermission(String id, String group, String permission) {
        AccessControlList acl = accessControlLists.get(id);
        Map<String, Set<String>> permissionMap = acl.getPermissionMap();
        Set<String> permissionsGroup = permissionMap.get(group);
        if (permissionsGroup == null) {
            permissionsGroup = new HashSet<>();
            permissionMap.put(group, permissionsGroup);
        }
        permissionsGroup.add(permission);
        acl = new AccessControlListWithStore(id, acl.getDisplayName(), permissionMap, userStore);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlStore removePermission(String id, String group, String permission) {
        AccessControlList acl = accessControlLists.get(id);
        Map<String, Set<String>> permissionMap = acl.getPermissionMap();
        Set<String> permissionsGroup = permissionMap.get(group);
        if (permissionsGroup != null) {
            permissionsGroup.remove(permission);
            acl = new AccessControlListWithStore(id, acl.getDisplayName(), permissionMap, userStore);
            mongoObjectFactory.storeAccessControlList(acl);
        }
        return this;
    }

    @Override
    public AccessControlStore removeAccessControlList(String id) {
        AccessControlList acl = accessControlLists.remove(id);
        mongoObjectFactory.deleteAccessControlList(acl);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Loads all the ACLs that were not loaded when the user store was initialized
     */
    public void loadRemainingACLs() {
        if (domainObjectFactory != null) {
            for (AccessControlList acl : domainObjectFactory.loadAllAccessControlLists(userStore, this)) {
                if (!accessControlLists.containsKey(acl.getId().toString())) {
                    accessControlLists.put(acl.getId().toString(), acl);
                }
            }
        }
    }
    
    @Override
    public Owner createOwnership(String id, String owner, String tenantOwner, String displayName) {
        setOwnership(id, owner, tenantOwner, displayName);
        return ownershipList.get(id);
    }

    @Override
    public AccessControlStore setOwnership(String id, String owner, String tenantOwner, String displayName) {
        Owner ownership = new OwnerImpl(id, owner, tenantOwner, displayName);
        ownershipList.put(id, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return this;
    }

    @Override
    public AccessControlStore removeOwnership(String id) {
        Owner ownership = ownershipList.remove(id);
        mongoObjectFactory.deleteOwnership(ownership);
        return this;
    }

    @Override
    public Owner getOwnership(String id) {
        return ownershipList.get(id);
    }
    
    @Override 
    public Iterable<Owner> getOwnerships() {
        return new ArrayList<>(ownershipList.values());
    }

    @Override
    public void clear() {
        accessControlLists.clear();
        ownershipList.clear();        
    }

    @Override
    public void replaceContentsFrom(AccessControlStore newAclStore) {
        clear();
        for (AccessControlList acl : newAclStore.getAccessControlLists()) {
            accessControlLists.put(acl.getId().toString(), acl);
        }
        for (Owner ownership : newAclStore.getOwnerships()) {
            ownershipList.put(ownership.getId().toString(), ownership);
        }
    }
}
