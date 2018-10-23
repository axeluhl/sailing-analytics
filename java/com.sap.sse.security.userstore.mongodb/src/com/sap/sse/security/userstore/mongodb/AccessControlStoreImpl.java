package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.LockUtil.RunnableWithResult;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
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

    private final ConcurrentHashMap<SecurityUser, Set<OwnershipAnnotation>> userToOwnership;
    private final ConcurrentHashMap<UserGroup, Set<OwnershipAnnotation>> userGroupToOwnership;
    private final NamedReentrantReadWriteLock lockForOwnerShips;

    private final UserGroup defaultTenant;

    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;

    public AccessControlStoreImpl(UserStore userStore) {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(),
                PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
    }

    public AccessControlStoreImpl(final DomainObjectFactory domainObjectFactory,
            final MongoObjectFactory mongoObjectFactory, final UserStore userStore) {
        this.defaultTenant = userStore.getDefaultTenant();
        accessControlLists = new ConcurrentHashMap<>();
        ownerships = new ConcurrentHashMap<>();
        userToOwnership = new ConcurrentHashMap<>();
        userGroupToOwnership = new ConcurrentHashMap<>();
        lockForOwnerShips = new NamedReentrantReadWriteLock("owbnerShipLock", true);

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
    public AccessControlListAnnotation getAccessControlList(
            QualifiedObjectIdentifier idOfAccessControlledObjectAsString) {
        return accessControlLists.get(idOfAccessControlledObjectAsString);
    }

    @Override
    public AccessControlListAnnotation setEmptyAccessControlList(QualifiedObjectIdentifier idOfAccessControlledObject,
            String displayNameOfAccessControlledObject) {
        AccessControlListAnnotation acl = new AccessControlListAnnotation(new AccessControlListImpl(),
                idOfAccessControlledObject, displayNameOfAccessControlledObject);
        accessControlLists.put(idOfAccessControlledObject, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public void setAclPermissions(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup,
            Set<String> actions) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObject);
        acl.getAnnotation().setPermissions(userGroup, actions);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    private AccessControlListAnnotation getOrCreateAcl(QualifiedObjectIdentifier idOfAccessControlledObject) {
        AccessControlListAnnotation acl = accessControlLists.get(idOfAccessControlledObject);
        if (acl == null) {
            acl = new AccessControlListAnnotation(new AccessControlListImpl(), idOfAccessControlledObject,
                    /* display name */ null);
        }
        return acl;
    }

    @Override
    public void addAclPermission(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup,
            String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObject);
        acl.getAnnotation().addPermission(userGroup, action);
        mongoObjectFactory.storeAccessControlList(acl);
    }

    @Override
    public void removeAclPermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UserGroup userGroup,
            String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().removePermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void denyAclPermission(QualifiedObjectIdentifier idOfAccessControlledObjectAsString, UserGroup userGroup,
            String action) {
        AccessControlListAnnotation acl = getOrCreateAcl(idOfAccessControlledObjectAsString);
        if (acl.getAnnotation().denyPermission(userGroup, action)) {
            mongoObjectFactory.storeAccessControlList(acl);
        }
    }

    @Override
    public void removeAclDenial(QualifiedObjectIdentifier idOfAccessControlledObject, UserGroup userGroup,
            String action) {
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
    public OwnershipAnnotation setOwnership(final QualifiedObjectIdentifier id, final SecurityUser userOwnerName,
            final UserGroup tenantOwner, String displayNameOfOwnedObject) {
        final OwnershipAnnotation ownership = new OwnershipAnnotation(new OwnershipImpl(userOwnerName, tenantOwner), id,
                displayNameOfOwnedObject);
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {

            @Override
            public void run() {
                internalRemoveOwnershipFromUserAndGroupMapsWithoutDBWrite(ownership);
                internalSetOwnershipWithoutDBWrite(ownership);
            }
        });
        mongoObjectFactory.storeOwnership(ownership);
        return ownership;
    }

    private void internalSetOwnershipWithoutDBWrite(final OwnershipAnnotation ownership) {
        ownerships.put(ownership.getIdOfAnnotatedObject(), ownership);
        UserGroup tenantOwner = ownership.getAnnotation().getTenantOwner();
        if (tenantOwner != null) {
            Set<OwnershipAnnotation> currentGroupOwnerships = userGroupToOwnership.get(tenantOwner);
            if (currentGroupOwnerships == null) {
                currentGroupOwnerships = Collections
                        .newSetFromMap(new ConcurrentHashMap<OwnershipAnnotation, Boolean>());
                userGroupToOwnership.put(tenantOwner, currentGroupOwnerships);
            }
            currentGroupOwnerships.add(ownership);
        }
        SecurityUser userOwnerName = ownership.getAnnotation().getUserOwner();
        if (userOwnerName != null) {
            Set<OwnershipAnnotation> currentUserOwnerships = userToOwnership.get(userOwnerName);
            if (currentUserOwnerships == null) {
                currentUserOwnerships = Collections
                        .newSetFromMap(new ConcurrentHashMap<OwnershipAnnotation, Boolean>());
                userToOwnership.put(userOwnerName, currentUserOwnerships);
            }
            currentUserOwnerships.add(ownership);
        }
    }

    @Override
    public void removeOwnership(final QualifiedObjectIdentifier idAsString) {
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {
            @Override
            public void run() {
                OwnershipAnnotation ownership = ownerships.remove(idAsString);
                if (ownership != null) {
                    internalRemoveOwnershipFromUserAndGroupMapsWithoutDBWrite(ownership);
                    mongoObjectFactory.deleteOwnership(idAsString, ownership.getAnnotation());
                }
            }

        });
    }

    private void internalRemoveOwnershipFromUserAndGroupMapsWithoutDBWrite(OwnershipAnnotation ownership) {
        Set<OwnershipAnnotation> currentGroupOwnerships = userGroupToOwnership
                .get(ownership.getAnnotation().getTenantOwner());
        if (currentGroupOwnerships != null) {
            currentGroupOwnerships.remove(ownership);
            if (currentGroupOwnerships.isEmpty()) {
                // no more entries, remove entry
                userGroupToOwnership.remove(ownership.getAnnotation().getTenantOwner());
            }
        }
        Set<OwnershipAnnotation> currentUserOwnerships = userToOwnership.get(ownership.getAnnotation().getUserOwner());
        if (currentUserOwnerships != null) {
            currentUserOwnerships.remove(ownership);
            if (currentUserOwnerships.isEmpty()) {
                // no more entries, remove entry
                userToOwnership.remove(ownership.getAnnotation().getUserOwner());
            }

        }
    }

    @Override
    public OwnershipAnnotation getOwnership(final QualifiedObjectIdentifier idOfOwnedObjectAsString) {
        return LockUtil.executeWithReadLockAndResult(lockForOwnerShips, new RunnableWithResult<OwnershipAnnotation>() {

            @Override
            public OwnershipAnnotation run() {
                return ownerships.get(idOfOwnedObjectAsString);
            }
        });
    }

    @Override
    public Iterable<OwnershipAnnotation> getOwnerships() {
        return LockUtil.executeWithReadLockAndResult(lockForOwnerShips,
                new RunnableWithResult<Iterable<OwnershipAnnotation>>() {

                    @Override
                    public Iterable<OwnershipAnnotation> run() {
                        return new ArrayList<>(ownerships.values());
                    }
                });
    }

    @Override
    public void clear() {
        accessControlLists.clear();
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {

            @Override
            public void run() {
                ownerships.clear();
            }
        });
    }

    @Override
    public void replaceContentsFrom(final AccessControlStore newAccessControlStore) {
        clear();
        for (AccessControlListAnnotation acl : newAccessControlStore.getAccessControlLists()) {
            accessControlLists.put(acl.getIdOfAnnotatedObject(), acl);
        }
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {

            @Override
            public void run() {
                for (OwnershipAnnotation ownership : newAccessControlStore.getOwnerships()) {
                    ownerships.put(ownership.getIdOfAnnotatedObject(), ownership);
                }
            }
        });
    }

    @Override
    public void removeAllOwnershipsFor(final UserGroup userGroup) {
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {
            @Override
            public void run() {
                Set<OwnershipAnnotation> knownOwnerships = userGroupToOwnership.get(userGroup);
                // do not use setOwnership, we know the user will not change, and we can use the more effective remove
                // for userGroupToOwnership after deleting
                for (OwnershipAnnotation ownership : knownOwnerships) {
                    final OwnershipAnnotation groupLessOwnership = new OwnershipAnnotation(
                            new OwnershipImpl(ownership.getAnnotation().getUserOwner(), null),
                            ownership.getIdOfAnnotatedObject(), ownership.getDisplayNameOfAnnotatedObject());
                    ownerships.put(ownership.getIdOfAnnotatedObject(), groupLessOwnership);
                    mongoObjectFactory.storeOwnership(groupLessOwnership);
                }
                userGroupToOwnership.remove(userGroup);
            }
        });
    }

    @Override
    public void removeAllOwnershipsFor(final SecurityUser user) {
        LockUtil.executeWithWriteLock(lockForOwnerShips, new Runnable() {
            @Override
            public void run() {
                Set<OwnershipAnnotation> knownOwnerships = userToOwnership.get(user);
                // do not use setOwnership, we know the group will not change, and we can use the more effective remove
                // for userToOwnership after deleting
                for (OwnershipAnnotation ownership : knownOwnerships) {
                    final OwnershipAnnotation userLessOwnership = new OwnershipAnnotation(
                            new OwnershipImpl(null, ownership.getAnnotation().getTenantOwner()),
                            ownership.getIdOfAnnotatedObject(), ownership.getDisplayNameOfAnnotatedObject());
                    ownerships.put(ownership.getIdOfAnnotatedObject(), userLessOwnership);
                    mongoObjectFactory.storeOwnership(userLessOwnership);
                }
                userToOwnership.remove(user);
            }
        });
    }
}
