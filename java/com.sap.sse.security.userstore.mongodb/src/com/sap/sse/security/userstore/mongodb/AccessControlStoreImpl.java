package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
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
    private final ConcurrentHashMap<String, AccessControlListAnnotation> accessControlLists;
    
    /**
     * maps from object ID string representations to the ownership information for the respective key object
     */
    private final ConcurrentHashMap<String, OwnershipAnnotation> ownerships;
    
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
            for (AccessControlListAnnotation acl : domainObjectFactory.loadAllAccessControlLists(userStore)) {
                accessControlLists.put(acl.getIdOfAnnotatedObjectAsString(), acl);
            }
            for (OwnershipAnnotation ownership : domainObjectFactory.loadAllOwnerships(userStore)) {
                ownerships.put(ownership.getIdOfAnnotatedObjectAsString(), ownership);
            }
        }
    }
    
    @Override
    public Iterable<AccessControlListAnnotation> getAccessControlLists() {
        return new ArrayList<>(accessControlLists.values());
    }
    
    @Override
    public AccessControlListAnnotation getAccessControlList(String idOfAccessControlledObjectAsString) {
        return accessControlLists.get(idOfAccessControlledObjectAsString);
    }

    @Override
    public AccessControlListAnnotation createAccessControlList(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject) {
        AccessControlListAnnotation acl = new AccessControlListAnnotation(new AccessControlListImpl(), idOfAccessControlledObjectAsString, displayNameOfAccessControlledObject);
        accessControlLists.put(idOfAccessControlledObjectAsString, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public void setAclPermissions(String idOfAccessControlledObjectAsString, UserGroup userGroup, Set<String> actions) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        acl.getAnnotation().setPermissions(userGroup, actions);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    private AccessControlListAnnotation getOrCreateAcl(String idOfAccessControlledObjectAsString) {
        AccessControlListAnnotation acl = accessControlLists.get(idOfAccessControlledObjectAsString);
        if (acl == null) {
            acl = new AccessControlListAnnotation(new AccessControlListImpl(), idOfAccessControlledObjectAsString, /* display name */ null);
        }
        return acl;
    }

    @Override
    public void addAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        acl.getAnnotation().addPermission(userGroup, action);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    @Override
    public void removeAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().removePermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void denyAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().denyPermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAclDenial(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().removeDenial(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAccessControlList(String idOfAccessControlledObjectAsString) {
        AccessControlListAnnotation acl = accessControlLists.remove(idOfAccessControlledObjectAsString);
        mongoObjectFactory.deleteAccessControlList(idOfAccessControlledObjectAsString, acl.getAnnotation());
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public OwnershipAnnotation createOwnership(String idAsString, SecurityUser userOwnerName, Tenant tenantOwner, String displayNameOfOwnedObject) {
        OwnershipAnnotation ownership = new OwnershipAnnotation(new OwnershipImpl(userOwnerName, tenantOwner), idAsString, displayNameOfOwnedObject);
        ownerships.put(idAsString, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return ownership;
    }

    @Override
    public void removeOwnership(String idAsString) {
        OwnershipAnnotation ownership = ownerships.remove(idAsString);
        mongoObjectFactory.deleteOwnership(idAsString, ownership.getAnnotation());
    }

    @Override
    public OwnershipAnnotation getOwnership(String idOfOwnedObjectAsString) {
        final OwnershipAnnotation storedOwnership = ownerships.get(idOfOwnedObjectAsString);
        final OwnershipAnnotation result;
        if (storedOwnership != null) {
            result = storedOwnership;
        } else {
            result = createDefaultOwnership(idOfOwnedObjectAsString);
        }
        return result;
    }
    
    /**
     * If there is no ownership information for an object and there is a {@link #defaultTenant} available,
     * create a default {@link Ownership} information that lists the {@link #defaultTenant} as the tenant owner
     * for the object in question; no user owner is specified. If no {@link #defaultTenant} is available,
     * {@code null} is returned.
     */
    private OwnershipAnnotation createDefaultOwnership(String idOfOwnedObjectAsString) {
        final Ownership result;
        if (defaultTenant != null) {
            result = new OwnershipImpl(/* userOwner */ null, /* tenantOwner */ defaultTenant);
        } else {
            result = null;
        }
        return result == null ? null : new OwnershipAnnotation(result, idOfOwnedObjectAsString, /* display name */ null);
    }

    @Override 
    public Iterable<OwnershipAnnotation> getOwnerships() {
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
        for (AccessControlListAnnotation acl : newAccessControlStore.getAccessControlLists()) {
            accessControlLists.put(acl.getIdOfAnnotatedObjectAsString(), acl);
        }
        for (OwnershipAnnotation ownership : newAccessControlStore.getOwnerships()) {
            ownerships.put(ownership.getIdOfAnnotatedObjectAsString(), ownership);
        }
    }
}
