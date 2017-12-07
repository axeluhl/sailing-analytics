package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.Tenant;
import com.sap.sse.security.shared.UserGroup;

public interface AccessControlStore extends Named {
    Iterable<AccessControlList> getAccessControlLists();
    AccessControlList getAccessControlList(String idOfAccessControlledObjectAsString);
    /**
     * @param idOfAccessControlledObjectAsString ID of the object the ACL is attached to
     * @param displayNameOfAccessControlledObject the display name of the object the ACL is attached to
     */
    AccessControlList createAccessControlList(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject);
    void setAclPermissions(String idOfAccessControlledObjectAsString, UserGroup userGroup, Set<String> actions);
    void addAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void removeAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void removeAccessControlList(String idOfAccessControlledObjectAsString);
    
    Iterable<Ownership> getOwnerships();
    Ownership getOwnership(String idOfOwnedObjectAsString);
    Ownership createOwnership(String idOfOwnedObjectAsString, SecurityUser userOwner, Tenant tenantOwner, String displayNameOfOwnedObject);
    void setOwnership(String idOfOwnedObjectAsString, SecurityUser userOwner, Tenant tenantOwner, String displayNameOfOwnedObject);
    void removeOwnership(String idOfOwnedObjectAsString);
    
    void clear();
    void replaceContentsFrom(AccessControlStore newAccessControlStore);
}