package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlStore;
import com.sap.sse.security.UserStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.impl.AccessControlListImpl;
import com.sap.sse.security.shared.impl.OwnershipImpl;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class AccessControlStoreImpl implements AccessControlStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlStoreImpl.class.getName());
    
    private String name = "Access control store";
    
    /**
     * maps from object ID string representations to the access control lists for the respective key object
     */
    private final ConcurrentHashMap<QualifiedObjectIdentifier, AccessControlListAnnotation> accessControlLists;
    
    /**
     * maps from object ID string representations to the ownership information for the respective key object
     */
    private final ConcurrentHashMap<QualifiedObjectIdentifier, OwnershipAnnotation> ownerships;
    
    private final UserGroup defaultTenant;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    
    public AccessControlStoreImpl(UserStore userStore) {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
    }
    
    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory,
            final MongoObjectFactory mongoObjectFactory, UserStore userStore) {
        this.defaultTenant = userStore.getDefaultTenant();
        accessControlLists = new ConcurrentHashMap<>();
        ownerships = new ConcurrentHashMap<>();
        this.mongoObjectFactory = mongoObjectFactory;
        if (domainObjectFactory != null) {
            for (AccessControlListAnnotation acl : domainObjectFactory.loadAllAccessControlLists(userStore)) {
                accessControlLists.put(acl.getIdOfAnnotatedObject(), acl);
            }
            for (OwnershipAnnotation ownership : domainObjectFactory.loadAllOwnerships(userStore)) {
                ownerships.put(ownership.getIdOfAnnotatedObject(), ownership);
            }
        }

        // check if we already have an ownership for the server, create if it is missing
        QualifiedObjectIdentifier expectedServerOwner = SecuredSecurityTypes.SERVER
                .getQualifiedObjectIdentifier(defaultTenant.getName());
        if (!ownerships.containsKey(expectedServerOwner)) {
            setOwnership(expectedServerOwner, null, defaultTenant, defaultTenant.getName());
        }

    }
    
    @Override
    public Iterable<AccessControlListAnnotation> getAccessControlLists() {
        return new ArrayList<>(accessControlLists.values());
    }
    
    @Override
    public AccessControlListAnnotation getAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        return accessControlLists.get(idOfAccessControlledObjectAsString);
    }

    @Override
    public AccessControlListAnnotation setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject, String displayNameOfAccessControlledObject) {
        AccessControlListAnnotation acl = new AccessControlListAnnotation(new AccessControlListImpl(), idOfAccessControlledObject, displayNameOfAccessControlledObject);
        accessControlLists.put(idOfAccessControlledObject, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public void setAclPermissions(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup, Set<String> actions) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObject);
        acl.getAnnotation().setPermissions(userGroup, actions);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    private AccessControlListAnnotation getOrCreateAcl(QualifiedObjectIdentifier idOfAccessControlledObject) {
        AccessControlListAnnotation acl = accessControlLists.get(idOfAccessControlledObject);
        if (acl == null) {
            acl = new AccessControlListAnnotation(new AccessControlListImpl(), idOfAccessControlledObject, /* display name */ null);
        }
        return acl;
    }

    @Override
    public void addAclPermission(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObject);
        acl.getAnnotation().addPermission(userGroup, action);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    @Override
    public void removeAclPermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().removePermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void denyAclPermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().denyPermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAclDenial(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup, String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObject);
        if (acl.getAnnotation().removeDenial(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject) {
        AccessControlListAnnotation acl = accessControlLists.remove(idOfAccessControlledObject);
        if (acl != null) {
            mongoObjectFactory.deleteAccessControlList(idOfAccessControlledObject, acl.getAnnotation());
        }
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public OwnershipAnnotation setOwnership(QualifiedObjectIdentifier id, SecurityUser userOwnerName, UserGroup tenantOwner, String displayNameOfOwnedObject) {
        OwnershipAnnotation ownership = new OwnershipAnnotation(new OwnershipImpl(userOwnerName, tenantOwner), id, displayNameOfOwnedObject);
        ownerships.put(id, ownership);
        mongoObjectFactory.storeOwnership(ownership);
        return ownership;
    }

    @Override
    public void removeOwnership(QualifiedObjectIdentifier idAsString) {
        OwnershipAnnotation ownership = ownerships.remove(idAsString);
        if (ownership != null) {
            mongoObjectFactory.deleteOwnership(idAsString, ownership.getAnnotation());
        }
    }

    @Override
    public OwnershipAnnotation getOwnership(QualifiedObjectIdentifier idOfOwnedObjectAsString) {
        return ownerships.get(idOfOwnedObjectAsString);
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
            accessControlLists.put(acl.getIdOfAnnotatedObject(), acl);
        }
        for (OwnershipAnnotation ownership : newAccessControlStore.getOwnerships()) {
            ownerships.put(ownership.getIdOfAnnotatedObject(), ownership);
        }
    }
}
