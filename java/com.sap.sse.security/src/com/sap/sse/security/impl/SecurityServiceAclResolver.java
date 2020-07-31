package com.sap.sse.security.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker.AclResolver;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

// TODO Bug 5239: a more efficient implementation is possible using reverse mappings in AccessControlStore
public class SecurityServiceAclResolver implements AclResolver<AccessControlList, Ownership> {
    private final AccessControlStore accessControlStore;

    SecurityServiceAclResolver(AccessControlStore accessControlStore) {
        this.accessControlStore = accessControlStore;
    }

    @Override
    public Iterable<AccessControlList> resolveAcls(
            Ownership ownership, String type, Iterable<String> objectIdentifiersAsString) {
        final Set<AccessControlList> result = new HashSet<>();
        for (AccessControlListAnnotation annotation : accessControlStore.getAccessControlLists()) {
            // TODO bug5239: introduce shadow hash maps for ACLs that map by object type
            if (!annotation.getIdOfAnnotatedObject().getTypeIdentifier().equals(type)) {
                continue;
            }
            // TODO bug5239: if object IDs are provided, ACLs should be looked up straight from the combination of type and object ID through accessControlStore.getAccessControlList(QualifiedObjectIdentifier)
            if (objectIdentifiersAsString != null && !Util.contains(objectIdentifiersAsString, annotation.getIdOfAnnotatedObject().getTypeRelativeObjectIdentifier().toString())) {
                continue;
            }
            // If ownership is given, we can exclude objects not owned by the user or group
            // TODO bug5239: introduce shadow hash maps for ACLs that map by ownership
            if (ownership != null) {
                final OwnershipAnnotation ownershipOfObject = accessControlStore.getOwnership(annotation.getIdOfAnnotatedObject());
                if (ownershipOfObject != null) {
                    final User userOwner = ownership.getUserOwner();
                    final UserGroup tenantOwner = ownership.getTenantOwner();
                    if (!(userOwner != null && userOwner
                            .equals(ownershipOfObject.getAnnotation().getUserOwner()))
                            && !(tenantOwner != null && tenantOwner.equals(
                                    ownershipOfObject.getAnnotation().getTenantOwner()))) {
                        continue;
                    }
                }
            }
            result.add(annotation.getAnnotation());
        }
        return result;
    }
}