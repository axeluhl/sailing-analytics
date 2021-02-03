package com.sap.sse.security.impl;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.security.PermissionChangeListener;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.shared.OwnershipAnnotation;
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
    
    public PermissionChangeListeners(final SecurityService securityService) {
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
    public void addPermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
        addOrRemovePermissionChangeListener(permission, listener, this::addPermissionChangeListenerInternal);
    }

    public void removePermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
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
     * Analyzes a change of permission/role on user: this happens on a specific {@code user}, and we can check if the
     * permission added/removed implies any permission for which a listener registered; this does still not necessarily
     * imply a change (the user could, e.g., have obtained the permission in more than one way), but this kind of change
     * will usually happen at very low frequency and in almost all cases will effectively cause a change. If the role is
     * qualified (user/group), check the qualification against the ownerships of the objects identified by the
     * permissions for which we have listener registrations.
     */
    public void roleAddedToOrRemovedFromUser(User user, Role role) {
        LockUtil.executeWithReadLock(lock, () -> {
            outer: for (final WildcardPermission permission : role.getPermissions()) {
                // filter by type if provided; for type wildcard, all listener registrations will have to be scanned
                final Iterable<String> typesToScan;
                if (!permission.getParts().isEmpty()) {
                    if (permission.getParts().get(0).contains(WildcardPermission.WILDCARD_TOKEN)) {
                        typesToScan = permissionChangeListenersByType.keySet();
                    } else {
                        typesToScan = permission.getParts().get(0);
                    }
                    for (final String typeName : typesToScan) {
                        for (final Entry<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionAndListener : permissionChangeListenersByType.get(typeName).entrySet()) {
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

    public void permissionAddedToOrRemovedFromUser(User user, WildcardPermission permission) {
        // TODO change permission/role on user: this happens on a specific user, and we can if the permission
        // added/removed implies the permission in question; this does still not necessarily imply a change (the user
        // could, e.g., have obtained the permission in more than one way), but this kind of change will usually happen
        // at very low frequency and in almost all cases will effectively cause a change
    }

    public void userAddedToOrRemovedFromGroup(User user, UserGroup group) {
        // TODO group membership changes only need to be considered in case of the group owning the object in question;
        // filtered further to only those groups granting roles to its members which imply the permission in question
        // results in a high accuracy trigger
    }

    public void aclChanged(QualifiedObjectIdentifier objectId) {
        // TODO ACL changes only need to be considered on the object itself and can further be filtered by the specific
        // permission in question; they hardly ever occur
    }

    void permissionAddedToOrRemovedFromRoleDefinition(RoleDefinition roleDefinition,
            Iterable<WildcardPermission> oldPermissions, Iterable<WildcardPermission> newPermissions) {
        // TODO adding the permission to or removing it from a role is easy to detect because we can check whether the
        // permission(s) added/removed imply the permission in question; permissions implied by roles are not changed
        // frequently
        // TODO we need to find the users who have a role based on roleDefinition assigned if the permission
        // added/removed implies a permission that has listeners here
    }

    public void roleAddedToOrRemovedFromGroup(UserGroup group, RoleDefinition roleDefinition) {
        // TODO adding/removing roles a group grants is only relevant when the group owns the object in question and the
        // role(s) imply the permission in question; both are efficient (O(1)) to determine
    }

    public void roleDefinitionRemoved(RoleDefinition roleDefinition) {
        // TODO
    }

    public void userGroupDeleted(UserGroup group) {
        // TODO
    }

    public void ownershipChanged(QualifiedObjectIdentifier objectId) {
        // TODO ownership changes are only relevant on the object in question and hence are easy to observe; ownership
        // changes don't occur often, especially not for the object type (LANDSCAPE) key to our considerations here
    }

    /**
     * Call before the user and any of its permission or role assignments or ownerships has been cleared!
     */
    public void userDeleted(User user) {
        // TODO find out if the user had any of the key permissions and notify the corresponding listeners
    }
}
