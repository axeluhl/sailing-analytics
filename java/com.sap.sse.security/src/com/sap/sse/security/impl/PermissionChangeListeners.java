package com.sap.sse.security.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.security.PermissionChangeListener;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.AccessControlListAnnotation;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.Ownership;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * Keeps track of a set of {@link PermissionChangeListener}s, usually in the context of {@link SecurityServiceImpl}, to
 * efficiently derive possible triggers for notifying the listeners, based on, e.g., changes to object ownerships, user
 * group memberships, role and permission assignments to users, or changes to role definitions. The class is
 * thread-safe.<p>
 * 
 * The permissions managed in here are always single permissions regarding the single type, single action, and single
 * object ID to which they refer. If the {@link #addPermissionChangeListener(WildcardPermission, PermissionChangeListener)}
 * or {@link #removePermissionChangeListener(WildcardPermission, PermissionChangeListener)} methods receive a {@link WildcardPermission}
 * that does not have this property, it is expanded into the various single permissions, and the calls behave as if the listener
 * had been added / removed for each of those expanded single permissions.
 * 
 * @author Axel Uhl (d043530)
 *
 */
/**
 * @author Axel Uhl (D043530)
 *
 */
public class PermissionChangeListeners {
    private final NamedReentrantReadWriteLock lock;

    /**
     * Keeps track of the listeners, keyed by the {@link HasPermissions#getName() secured type name} which equals the
     * first {@link WildcardPermission#getParts() part} of the permissions that occur as key of the nested map. The
     * permissions used as keys of the nested map are single permissions, with exactly one type name, one action name,
     * and one object identifier.
     * <p>
     * 
     * Modifications must be applied under the {@link NamedReentrantReadWriteLock#writeLock() write lock} of
     * {@link #lock}. Reading shall obtain the corresponding {@link NamedReentrantReadWriteLock#readLock() read lock}
     * accordingly.
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>>> permissionChangeListenersByType;
    
    /**
     * Keeps track of the listeners, keyed by the {@link QualifiedObjectIdentifier object identifier} to which the
     * permission applies and which is used in the third {@link WildcardPermission#getParts() part} of the permissions
     * used as keys in the nested map. The permissions used as keys of the nested map are single permissions, with
     * exactly one type name, one action name, and one object identifier.
     * <p>
     * Modifications must be applied under the {@link NamedReentrantReadWriteLock#writeLock() write lock} of
     * {@link #lock}. Reading shall obtain the corresponding {@link NamedReentrantReadWriteLock#readLock() read lock}
     * accordingly.
     */
    private final ConcurrentHashMap<QualifiedObjectIdentifier, ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>>> permissionChangeListenersByObject;
    
    private final SecurityService securityService;

    @FunctionalInterface
    private static interface ListenerAdderOrRemover {
         void addOrRemove(QualifiedObjectIdentifier oid, String action, PermissionChangeListener listener);
    }
    
    PermissionChangeListeners(final SecurityService securityService) {
        this.securityService = securityService;
        lock = new NamedReentrantReadWriteLock(PermissionChangeListeners.class.getSimpleName(), /* fair */ false);
        permissionChangeListenersByType = new ConcurrentHashMap<>();
        permissionChangeListenersByObject = new ConcurrentHashMap<>();
    }

    /**
     * @param permission
     *            may contain multiple types and multiple object IDs; may contain a wildcard "*" for the action part. If
     *            more than one type and/or more than one object ID and/or an action wildcard are provided, the method
     *            behaves as if it had been invoked for each expanded single permission with a single type and a single
     *            action and a single object ID. At least one type and object ID must be provided.
     */
    void addPermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
        addOrRemovePermissionChangeListener(permission, listener, this::addPermissionChangeListenerInternal);
    }

    void removePermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
        addOrRemovePermissionChangeListener(permission, listener, this::removePermissionChangeListenerInternal);
    }

    /**
     * Expands permission types, object IDs and actions into permissions with single event, single action, and single object ID and
     * then invokes {@code adderOrRemover} with the resulting single permissions. Type and object ID must not contain wildcards; actions may.
     */
    private void addOrRemovePermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener, ListenerAdderOrRemover adderOrRemover) {
        if (permission.getParts().size() < 3
        || permission.getParts().get(0).contains(WildcardPermission.WILDCARD_TOKEN)
        || permission.getParts().get(2).contains(WildcardPermission.WILDCARD_TOKEN)) {
            throw new IllegalArgumentException(
                    "PermissionChangeListener can not be registered for wildcard permission " + permission
                            + ". Use at least specific type(s) and object ID(s).");
        }
        for (final QualifiedObjectIdentifier oid : permission.getQualifiedObjectIdentifiers()) {
            for (final String action : getActions(oid.getTypeIdentifier(), permission.getParts().get(1))) {
                adderOrRemover.addOrRemove(oid, action, listener);
            }
        }
    }

    private void addPermissionChangeListenerInternal(QualifiedObjectIdentifier oid, String actionName, PermissionChangeListener listener) {
        final WildcardPermission singlePermission = oid.getPermission(actionName);
        LockUtil.executeWithWriteLock(lock, () -> {
            permissionChangeListenersByType.computeIfAbsent(oid.getTypeIdentifier(), typeName -> new ConcurrentHashMap<>())
                .computeIfAbsent(singlePermission, permission -> new ConcurrentHashMap<>())
                .put(listener, true);
            permissionChangeListenersByObject.computeIfAbsent(oid, p -> new ConcurrentHashMap<>())
                .computeIfAbsent(singlePermission, permission -> new ConcurrentHashMap<>())
                .put(listener, true);
        });
    }

    private void removePermissionChangeListenerInternal(QualifiedObjectIdentifier oid, String actionName, PermissionChangeListener listener) {
        final WildcardPermission singlePermission = oid.getPermission(actionName);
        LockUtil.executeWithWriteLock(lock, () -> {
            final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> map =
                    permissionChangeListenersByType.get(oid.getTypeIdentifier());
            if (map != null) {
                ConcurrentHashMap<PermissionChangeListener, Boolean> innerMap = map.get(singlePermission);
                if (innerMap != null) {
                    innerMap.remove(listener);
                }
            }
            final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> map2 =
                    permissionChangeListenersByObject.get(oid);
            if (map2 != null) {
                ConcurrentHashMap<PermissionChangeListener, Boolean> innerMap2 = map2.get(singlePermission);
                if (innerMap2 != null) {
                    innerMap2.remove(listener);
                }
            }
        });
    }

    private Iterable<String> getActions(String securedTypeName, Set<String> actions) {
        final Set<String> result = new HashSet<>();
        final HasPermissions securedType = securityService.getHasPermissionsByName(securedTypeName);
        if (securedType != null) {
            for (final String actionString : actions) {
                if (actionString.equals(WildcardPermission.WILDCARD_TOKEN)) {
                    for (final Action action : securedType.getAvailableActions()) {
                        result.add(action.name());
                    }
                } else {
                    result.add(actionString);
                }
            }
        }
        return result;
    }

    /**
     * Analyzes adding/removing of role on a user: we can check if the role added/removed implies any permission for
     * which a listener registered; this does still not necessarily imply a change (the user could, e.g., have obtained
     * the permission in more than one way), but this kind of change will usually happen at very low frequency and in
     * almost all cases will effectively cause a change. If the role is qualified (user/group), check the qualification
     * against the ownerships of the objects identified by the permissions for which we have listener registrations.
     */
    void roleAddedToOrRemovedFromUser(User user, Role role) {
        LockUtil.executeWithReadLock(lock, () -> {
            outer: for (final WildcardPermission permission : role.getPermissions()) {
                // filter by type if provided; for type wildcard, all listener registrations will have to be scanned
                final Iterable<String> typesToScan = getTypesToScan(permission);
                for (final String typeName : typesToScan) {
                    final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionChangeListenersForType = permissionChangeListenersByType.get(typeName);
                    if (permissionChangeListenersForType != null) {
                        for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionAndListener : permissionChangeListenersForType.entrySet()) {
                            // all listener registrations' permissions have been expanded into single permissions upon registration:
                            final QualifiedObjectIdentifier objectId = permissionAndListener.getKey().getQualifiedObjectIdentifiers().iterator().next();
                            final OwnershipAnnotation ownershipAnnotation = securityService.getOwnership(objectId);
                            if (ownershipAnnotation == null || matchesQualification(ownershipAnnotation.getAnnotation(), role.getQualifiedForTenant(), role.getQualifiedForUser())) {
                                if (permission.implies(permissionAndListener.getKey())) {
                                    notifyListeners(permissionAndListener.getKey(), permissionAndListener.getValue().keySet());
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * From a permission that may contain wildcards for the type (first part) finds all type names from
     * {@link #permissionChangeListenersByType}'s key set that match.
     */
    private Iterable<String> getTypesToScan(final WildcardPermission permission) {
        final Iterable<String> typesToScan;
        if (permission.getParts().isEmpty()) {
            typesToScan = Collections.emptySet();
        } else if (permission.getParts().get(0).contains(WildcardPermission.WILDCARD_TOKEN)) {
            typesToScan = permissionChangeListenersByType.keySet();
        } else {
            typesToScan = Util.filter(permission.getParts().get(0), typeName->permissionChangeListenersByType.containsKey(typeName));
        }
        return typesToScan;
    }

    private void notifyListeners(WildcardPermission permission, Iterable<PermissionChangeListener> listeners) {
        final Iterable<User> usersWithPermissions = securityService.getUsersWithPermissions(permission);
        for (final PermissionChangeListener listener : listeners) {
            listener.setOfUsersWithPermissionChanged(permission, usersWithPermissions);
        }
    }

    private boolean matchesQualification(Ownership ownership, UserGroup qualifiedForTenant, User qualifiedForUser) {
        return (qualifiedForTenant == null || Util.equalsWithNull(qualifiedForTenant, ownership.getTenantOwner()))
            && (qualifiedForUser == null || Util.equalsWithNull(qualifiedForUser, ownership.getUserOwner()));
    }

    /**
     * Analyzes addition/removal of a permission on a user: we can check if the permission added/removed implies any
     * permission for which a listener registered; this does still not necessarily imply a change (the user could, e.g.,
     * have obtained the permission in more than one way), but this kind of change will usually happen at very low
     * frequency and in almost all cases will effectively cause a change.
     */
    void permissionAddedToOrRemovedFromUser(User user, WildcardPermission permission) {
        notifyListenersForPermissionsImplied(permission);
    }

    private void notifyListenersForPermissionsImplied(WildcardPermission permission) {
        LockUtil.executeWithReadLock(lock, () -> {
            // filter by type if provided; for type wildcard, all listener registrations will have to be scanned
            outer: for (final String typeName : getTypesToScan(permission)) {
                final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionChangeListenersForType = permissionChangeListenersByType.get(typeName);
                if (permissionChangeListenersForType != null) {
                    for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionAndListener : permissionChangeListenersForType.entrySet()) {
                        // all listener registrations' permissions have been expanded into single permissions upon registration:
                        if (permission.implies(permissionAndListener.getKey())) {
                            notifyListeners(permissionAndListener.getKey(), permissionAndListener.getValue().keySet());
                            break outer;
                        }
                    }
                }
            }
        });
    }

    /**
     * Group membership changes only need to be considered in case of the group owning the object for which we have
     * permission change listeners. We can filter further to only those groups granting roles to its members which imply the
     * permission in question, which results in a high accuracy trigger.
     */
    void userAddedToOrRemovedFromGroup(User user, UserGroup group) {
        LockUtil.executeWithReadLock(lock, () -> {
            for (final Entry<QualifiedObjectIdentifier, ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>>> objectIdAndListeners : permissionChangeListenersByObject.entrySet()) {
                final OwnershipAnnotation ownershipAnnotation = securityService.getOwnership(objectIdAndListeners.getKey());
                if (ownershipAnnotation != null && Util.equalsWithNull(ownershipAnnotation.getAnnotation().getTenantOwner(), group)) {
                    // group ownership matches; check which permissions for which a listener is subscribed is implied by any of the group's roles:
                    Util.filter(objectIdAndListeners.getValue().entrySet(), permissionAndListeners->isImpliedByGroupRoleForMembersOrAll(permissionAndListeners.getKey(), group))
                        .forEach(pAndL->notifyListeners(pAndL.getKey(), pAndL.getValue().keySet()));
                }
            }
        });
    }

    /**
     * Tells if the {@code permission} is implied by any of those roles from {@code group} that apply only to group
     * members.
     */
    private boolean isImpliedByGroupRoleForMembersOrAll(WildcardPermission permission, UserGroup group) {
        return !Util.isEmpty(Util.filter(group.getRoleDefinitionMap().entrySet(),
                roleAndForAll->
                    !roleAndForAll.getValue() &&
                    !Util.isEmpty(Util.filter(roleAndForAll.getKey().getPermissions(), p->p.implies(permission)))));
    }

    /**
     * ACL changes only need to be considered on the object itself and can further be filtered by the specific
     * permission in question; they hardly ever occur
     */
    void aclChanged(QualifiedObjectIdentifier objectId) {
        LockUtil.executeWithReadLock(lock, () -> {
            final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionChangeListenersForObject = permissionChangeListenersByObject.get(objectId);
            if (permissionChangeListenersForObject != null) {
                for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> listenersForObject : permissionChangeListenersForObject.entrySet()) {
                    notifyListeners(listenersForObject.getKey(), listenersForObject.getValue().keySet());
                }
            }
        });
    }

    void permissionAddedToOrRemovedFromRoleDefinition(RoleDefinition roleDefinition,
            Iterable<WildcardPermission> oldPermissions, Iterable<WildcardPermission> newPermissions) {
        final Iterable<WildcardPermission> permissionsAdded = Util.filter(newPermissions, newPermission->!Util.contains(oldPermissions, newPermission));
        final Iterable<WildcardPermission> permissionsRemoved = Util.filter(oldPermissions, oldPermission->!Util.contains(newPermissions, oldPermission));
        for (final WildcardPermission permissionAdded : permissionsAdded) {
            notifyListenersForPermissionsImplied(permissionAdded);
        }
        for (final WildcardPermission permissionRemoved : permissionsRemoved) {
            notifyListenersForPermissionsImplied(permissionRemoved);
        }
    }

    /**
     * adding/removing roles that a group grants is only relevant when the group owns the object in question and the
     * role added/removed implies the permission in question; both can be determined efficiently
     */
    void roleAddedToOrRemovedFromGroup(UserGroup group, RoleDefinition roleDefinition) {
        LockUtil.executeWithReadLock(lock, () -> {
            for (final Entry<QualifiedObjectIdentifier, ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>>> listenersForObject : permissionChangeListenersByObject.entrySet()) {
                final OwnershipAnnotation ownershipAnnotation = securityService.getOwnership(listenersForObject.getKey());
                if (ownershipAnnotation != null && Util.equalsWithNull(ownershipAnnotation.getAnnotation().getTenantOwner(), group)) {
                    // so we have listener registrations for an object owned by the group; filter permissions to those implied by roleDefinition
                    for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> e : listenersForObject.getValue().entrySet()) {
                        if (!Util.isEmpty(Util.filter(roleDefinition.getPermissions(), p->p.implies(e.getKey())))) {
                            notifyListeners(e.getKey(), e.getValue().keySet());
                        }
                    }
                }
            }
        });
    }

    /**
     * Notifies all permission change listeners that are registered for any permission that is implied by any of
     * {@code roleDefinition}'s {@link RoleDefinition#getPermissions() permissions}.
     */
    void roleDefinitionRemoved(RoleDefinition roleDefinition) {
        for (final WildcardPermission permission : roleDefinition.getPermissions()) {
            notifyListenersForPermissionsImplied(permission);
        }
    }

    /**
     * ownership changes are only relevant on the object in question and hence are easy to observe; ownership
     * changes don't occur often, especially not for the object type (LANDSCAPE) key to our considerations here
     */
    void ownershipChanged(QualifiedObjectIdentifier objectId) {
        LockUtil.executeWithReadLock(lock, () -> {
            final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> listenersForObject = permissionChangeListenersByObject.get(objectId);
            if (listenersForObject != null) {
                for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> e : listenersForObject.entrySet()) {
                    notifyListeners(e.getKey(), e.getValue().keySet());
                }
            }
        });
    }

    /**
     * Call before the user and any of its permission or role assignments or ownerships has been cleared!<p>
     * 
     * Finds out if the user had any of the key permissions and notifies the corresponding listeners
     */
    void userDeleted(User user) {
        final Map<WildcardPermission, Set<PermissionChangeListener>> listenersToNotify = new HashMap<>();
        LockUtil.executeWithReadLock(lock, () -> {
            final User allUser = securityService.getAllUser();
            for (final Entry<QualifiedObjectIdentifier, ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>>> permissionsAndListeners : permissionChangeListenersByObject.entrySet()) {
                final OwnershipAnnotation ownershipAnnotation = securityService.getOwnership(permissionsAndListeners.getKey());
                final AccessControlListAnnotation aclAnnotation = securityService.getAccessControlList(permissionsAndListeners.getKey());
                for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionAndListeners : permissionsAndListeners.getValue().entrySet()) {
                    if (PermissionChecker.isPermitted(
                            permissionAndListeners.getKey(), user, allUser,
                            ownershipAnnotation==null?null:ownershipAnnotation.getAnnotation(),
                            aclAnnotation==null?null:aclAnnotation.getAnnotation())) {
                        permissionAndListeners.getValue().keySet().forEach(listener->Util.addToValueSet(listenersToNotify, permissionAndListeners.getKey(), listener));
                    }
                }
            }
        });
        listenersToNotify.forEach((permission, listeners)->notifyListeners(permission, listeners));
    }
}
