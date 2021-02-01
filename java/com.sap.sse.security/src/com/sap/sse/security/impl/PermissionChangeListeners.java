package com.sap.sse.security.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.security.PermissionChangeListener;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.Role;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * Keeps track of a set of {@link PermissionChangeListener}s, usually in the context of {@link SecurityServiceImpl}, to
 * efficiently derive possible triggers for notifying the listeners, based on, e.g., changes to object ownerships, user
 * group memberships, role and permission assignments to users, or changes to role definitions. The class is
 * thread-safe.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class PermissionChangeListeners {
    private final NamedReentrantReadWriteLock lock;

    // TODO the following data structure is not sufficient; we probably need the listeners grouped by
    // QualifiedObjectIdentifier, then action
    private final ConcurrentHashMap<WildcardPermission, ConcurrentHashMap<PermissionChangeListener, Boolean>> permissionChangeListeners;

    public PermissionChangeListeners() {
        lock = new NamedReentrantReadWriteLock(PermissionChangeListeners.class.getSimpleName(), /* fair */ false);
        permissionChangeListeners = new ConcurrentHashMap<>();
    }

    public void addPermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
        for (final Set<String> partParts : permission.getParts()) {
            if (partParts.contains(WildcardPermission.WILDCARD_TOKEN)) {
                throw new IllegalArgumentException(
                        "PermissionChangeListener can not be registered for wildcard permission " + permission
                                + ". Use specific type(s), operation(s), and object ID(s).");
            }
        }
        LockUtil.executeWithWriteLock(lock, () -> {
            permissionChangeListeners.computeIfAbsent(permission, p -> new ConcurrentHashMap<>()).put(listener, true);
        });
    }

    public void removePermissionChangeListener(WildcardPermission permission, PermissionChangeListener listener) {
        LockUtil.executeWithWriteLock(lock, () -> {
            final ConcurrentHashMap<PermissionChangeListener, Boolean> listenersForPermission = permissionChangeListeners
                    .get(permission);
            if (listenersForPermission != null) {
                listenersForPermission.remove(listener);
                if (listenersForPermission.isEmpty()) {
                    permissionChangeListeners.remove(permission);
                }
            }
        });
    }

    public void roleAddedToOrRemovedFromUser(User user, Role role) {
        // TODO change permission/role on user: this happens on a specific user, and we can if the permission
        // added/removed implies the permission in question; this does still not necessarily imply a change (the user
        // could, e.g., have obtained the permission in more than one way), but this kind of change will usually happen
        // at very low frequency and in almost all cases will effectively cause a change
        //
        // We need:
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
