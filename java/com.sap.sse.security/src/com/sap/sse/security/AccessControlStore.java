package com.sap.sse.security;

import java.util.Set;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;

public interface AccessControlStore extends Named {
    Iterable<AccessControlListAnnotation> getAccessControlLists();
    
    /**
     * Looks up an ACL for the object identified by the {@code idOfAccessControlledObjectAsString}. If no
     * ACL is found for that object, {@code null} is returned.
     */
    AccessControlListAnnotation getAccessControlList(String idOfAccessControlledObjectAsString);
    
    /**
     * @param idOfAccessControlledObjectAsString ID of the object the ACL is attached to
     * @param displayNameOfAccessControlledObject the display name of the object the ACL is attached to
     */
    AccessControlListAnnotation createAccessControlList(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject);
    void setAclPermissions(String idOfAccessControlledObjectAsString, UserGroup userGroup, Set<String> actions);
    void addAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void removeAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void removeAclDenial(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void denyAclPermission(String idOfAccessControlledObjectAsString, UserGroup userGroup, String action);
    void removeAccessControlList(String idOfAccessControlledObjectAsString);
    
    Iterable<OwnershipAnnotation> getOwnerships();

    /**
     * Return the ownership information for the object identified by {@code idOfOwnedObjectAsString}. If there is no
     * ownership information for that object and there is a default tenant available, create a default {@link Ownership}
     * information that lists the default tenant as the tenant owner for the object in question; no user owner is
     * specified. If no default tenant is available and no ownership information for the object with the ID specified
     * is found, {@code null} is returned.
     */
    OwnershipAnnotation getOwnership(String idOfOwnedObjectAsString);
    
    OwnershipAnnotation createOwnership(String idOfOwnedObjectAsString, SecurityUser userOwner, UserGroup tenantOwner, String displayNameOfOwnedObject);
    void removeOwnership(String idOfOwnedObjectAsString);
    
    void clear();
    void replaceContentsFrom(AccessControlStore newAccessControlStore);
}