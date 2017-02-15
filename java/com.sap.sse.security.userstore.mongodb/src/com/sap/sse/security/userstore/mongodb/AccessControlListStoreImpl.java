package com.sap.sse.security.userstore.mongodb;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.AccessControlList;
import com.sap.sse.security.AccessControlListStore;
import com.sap.sse.security.AccessControlListWithStore;
import com.sap.sse.security.UserStore;

public class AccessControlListStoreImpl implements AccessControlListStore {
    private static final long serialVersionUID = 2165649781000936074L;

    // private static final Logger logger = Logger.getLogger(AccessControlListStoreImpl.class.getName());
    
    private String name = "Access control list store";
    
    private UserStore userStore;
    
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    private final transient DomainObjectFactory domainObjectFactory;
    
    public AccessControlListStoreImpl(final UserStore userStore) {
        this(PersistenceFactory.INSTANCE.getDefaultDomainObjectFactory(), PersistenceFactory.INSTANCE.getDefaultMongoObjectFactory(), userStore);
    }
    
    public AccessControlListStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory, final UserStore userStore) {
        accessControlLists = new ConcurrentHashMap<>();
        
        this.mongoObjectFactory = mongoObjectFactory;        
        this.domainObjectFactory = domainObjectFactory;
        
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
    public AccessControlList getAccessControlListByName(String name) {
        AccessControlList acl = accessControlLists.get(name);
        if (acl != null) {
            return acl;
        }
        acl = domainObjectFactory.loadAccessControlList(name, userStore, this);
        accessControlLists.put(acl.getName(), acl);
        return acl;
    }

    @Override
    public AccessControlList createAccessControlList(String name, String owner) {
        AccessControlList acl = new AccessControlListWithStore(name, owner, userStore, this);
        accessControlLists.put(name, acl);
        mongoObjectFactory.storeAccessControlList(acl);
        return acl;
    }

    @Override
    public AccessControlListStore putPermissions(String name, String group, Set<String> permissions) {
        AccessControlList acl = accessControlLists.get(name);
        acl.putPermissions(group, permissions);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlListStore addPermission(String name, String group, String permission) {
        AccessControlList acl = accessControlLists.get(name);
        acl.addPermission(group, permission);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlListStore removePermission(String name, String group, String permission) {
        AccessControlList acl = accessControlLists.get(name);
        acl.removePermission(group, permission);
        mongoObjectFactory.storeAccessControlList(acl);
        return this;
    }

    @Override
    public AccessControlListStore removeAccessControlList(String name) {
        AccessControlList acl = accessControlLists.remove(name);
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
                if (!accessControlLists.containsKey(acl.getName())) {
                    accessControlLists.put(acl.getName(), acl);
                }
            }
        }
    }
}
