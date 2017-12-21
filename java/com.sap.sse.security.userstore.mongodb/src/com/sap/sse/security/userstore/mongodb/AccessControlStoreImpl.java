package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;

public class AccessControlStoreImpl implements AccessControlStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlStoreImpl.class.getName());
    
    private String name = "Access control store";
    
    /**
     * maps from object ID string representations to the access control lists for the respective key object
     */
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    private final ConcurrentHashMap<String, Ownership> ownerships;
    
    private final Tenant defaultTenant;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    
    public AccessControlStoreImpl(UserStore userStore) {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
    }
    
    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory, UserStore userStore) {
        this.defaultTenant = userStore.getDefaultTenant();
        accessControlLists = new ConcurrentHashMap<>();
        ownerships = new ConcurrentHashMap<>();
        this.mongoObjectFactory = mongoObjectFactory;
        if (domainObjectFactory != null) {
            for (AccessControlList acl : domainObjectFactory.loadAllAccessControlLists(userStore)) {
                accessControlLists.put(acl.getIdOfAccessControlledObjectAsString(), acl);
            }
            for (Ownership ownership : domainObjectFactory.loadAllOwnerships(userStore)) {
                ownerships.put(ownership.getIdOfOwnedObjectAsString(), ownership);
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
    public AccessControlList createAccessControlList(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject) {
        AccessControlList acl = new AccessControlListImpl(idOfAccessControlledObjectAsString, displayNameOfAccessControlledObject);
        accessControlLists.put(idOfAccessControlledObjectAsString, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public void setAclPermissions(String idOfAccessControlledObjectAsString, UserGroup userGroup, Set<String> actions) {
        AccessControlList acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        acl.setPermissions(userGroup, actions);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    private AccessControlList getOrCreateAcl(String idOfAccessControlledObjectAsString) {
        AccessControlList acl = accessControlLists.get(idOfAccessControlledObjectAsString);
        if (acl == null) {
            acl = new AccessControlListImpl(idOfAccessControlledObjectAsString, /* displayNameOfAccessControlledObject */ null);
        }
        return acl;
    }

    @Override
    public void addAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlList acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        acl.addPermission(userGroup, action);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    @Override
    public void removeAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlList acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.removePermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void denyAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlList acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.denyPermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAclDenial(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlList acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.removeDenial(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAccessControlList(String idOfAccessControlledObjectAsString) {
        AccessControlList acl = accessControlLists.remove(idOfAccessControlledObjectAsString);
        mongoObjectFactory.deleteAccessControlList(acl);
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Ownership createOwnership(String idAsString, SecurityUser userOwnerName, Tenant tenantOwner, String displayNameOfOwnedObject) {
        Ownership ownership = new OwnershipImpl(idAsString, userOwnerName, tenantOwner, displayNameOfOwnedObject);
        ownerships.put(idAsString, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return ownerships.get(idAsString);
    }

    @Override
    public void removeOwnership(String idAsString) {
        Ownership ownership = ownerships.remove(idAsString);
        mongoObjectFactory.deleteOwnership(ownership);
    }

    @Override
    public Ownership getOwnership(String idOfOwnedObjectAsString) {
        final Ownership storedOwnership = ownerships.get(idOfOwnedObjectAsString);
        final Ownership result;
        if (storedOwnership != null) {
            result = storedOwnership;
        } else {
            result = createDefaultOwnership(idOfOwnedObjectAsString);
        }
        return result;
    }
    
    /**
     * If there is no ownership information for an object and there is a non-{@link #defaultTenant} available,
     * create a default {@link Ownership} information that lists the {@link #defaultTenant} as the tenant owner
     * for the object in question; no user owner is specified. If no {@link #defaultTenant} is available,
     * {@code null} is returned.
     */
    private Ownership createDefaultOwnership(String idOfOwnedObjectAsString) {
        final Ownership result;
        if (defaultTenant != null) {
            result = new OwnershipImpl(idOfOwnedObjectAsString, /* userOwner */ null, /* tenantOwner */ defaultTenant, /* displayNameOfOwnedObject */ null);
        } else {
            result = null;
        }
        return result;
    }

    @Override 
    public Iterable<Ownership> getOwnerships() {
        return new ArrayList<>(ownerships.values());
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
            accessControlLists.put(acl.getIdOfAccessControlledObjectAsString(), acl);
        }
        for (Ownership ownership : newAccessControlStore.getOwnerships()) {
            ownerships.put(ownership.getIdOfOwnedObjectAsString(), ownership);
        }
    }
}
