package com.sap.sse.security.accesscontrolliststore.mongodb.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sap.sse.security.AccessControlList;
import com.sap.sse.security.AccessControlListStore;
import com.sap.sse.security.Tenant;
import com.sap.sse.security.UserGroup;
import com.sap.sse.security.accesscontrolliststore.mongodb.DomainObjectFactory;
import com.sap.sse.security.accesscontrolliststore.mongodb.MongoObjectFactory;
import com.sap.sse.security.shared.Permission;

public class AccessControlListStoreImpl implements AccessControlListStore {
    private static final long serialVersionUID = 2165649781000936074L;

    private static final Logger logger = Logger.getLogger(AccessControlListStoreImpl.class.getName());
    
    private static final String ACCESS_TOKEN_KEY = "___access_token___";
    
    private String name = "Access control list store";
    
    private final ConcurrentHashMap<String, AccessControlList> accessControlLists;
    
    /**
     * Won't be serialized and remains <code>null</code> on the de-serializing end.
     */
    private final transient MongoObjectFactory mongoObjectFactory;
    
    public AccessControlListStoreImpl(final DomainObjectFactory domainObjectFactory, final MongoObjectFactory mongoObjectFactory) {
        accessControlLists = new ConcurrentHashMap<>();
        
        this.mongoObjectFactory = mongoObjectFactory;
        
        if (domainObjectFactory != null) {
            for (AccessControlList acl : domainObjectFactory.loadAllAccessControlLists()) {
                accessControlLists.put(acl.getName(), acl);
            }
        }
    }
    
    @Override
    public AccessControlList getAccessControlListByName(String name) {
        return accessControlLists.get(name);
    }

    @Override
    public AccessControlList createAccessControlList(String id, Tenant owner) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessControlListStore putPermissions(String id, UserGroup group, Set<Permission> permissions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessControlListStore addPermission(String id, UserGroup group, Permission permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessControlListStore removePermission(String id, UserGroup group, Permission permission) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessControlListStore removeAccessControlList(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

}
