package com.sap.sse.security.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import com.sap.sse.common.Util;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.interfaces.AccessControlStore;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker.AclResolver;
import com.sap.sse.security.shared.impl.AccessControlList;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

// TODO Bug 5239: a more efficient implementation is possible using reverse mappings in AccessControlStore
/**
 * An {@link AclResolver} implementation backed by the {@link SecurityService}'s {@link AccessControlStore}. It makes
 * use of an explicit ACL set passed, e.g., from a cache, or otherwise will use the {@link AccessControlStore} to
 * determine the ACLs that apply based on the object type, optional object IDs, as well as ownerships. Shortcut
 * evaluation is applied in both cases (provided or computed ACLs), looking for the first ACL that matches a predicate.
 * If such an ACL is found, {@code null} is returned. Otherwise, in the provided case, the ACL set provided is returned
 * unmodified, whereas in the computed case, the full ACL set for the context of ownership, type and object IDs computed
 * is returned.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SecurityServiceAclResolver implements AclResolver<AccessControlList, Ownership> {
    private final AccessControlStore accessControlStore;

    SecurityServiceAclResolver(AccessControlStore accessControlStore) {
        this.accessControlStore = accessControlStore;
    }

    @Override
    public Iterable<AccessControlList> resolveAclsAndCheckIfAnyMatches(
            Ownership ownership, String type, Iterable<String> objectIdentifiersAsString,
            Predicate<AccessControlList> filterCondition, Iterable<AccessControlList> allAclsForTypeAndObjectIdsOrNull) {
        final Iterable<AccessControlList> result;
        if (allAclsForTypeAndObjectIdsOrNull == null) {
            result = enumerateAndCheckApplicableAcls(ownership, type, objectIdentifiersAsString, filterCondition);
        } else {
            for (AccessControlList acl : allAclsForTypeAndObjectIdsOrNull) {
                if (filterCondition.test(acl)) {
                    return null;
                }
            }
            result = allAclsForTypeAndObjectIdsOrNull;
        }
        return result;
    }
    
    private Iterable<AccessControlList> enumerateAndCheckApplicableAcls(Ownership ownership, String type,
            Iterable<String> objectIdentifiersAsString, Predicate<AccessControlList> filterCondition) {
        final List<AccessControlList> result = new LinkedList<>();
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
            final AccessControlList acl = annotation.getAnnotation();
            if (filterCondition.test(acl)) {
                return null;
            }
            result.add(acl);
        }
        return result;
    }
}