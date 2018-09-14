package com.sap.sse.security.shared.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.PermissionChecker;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;

public class AccessControlListImpl implements AccessControlList {
    private static final long serialVersionUID = -8587238587604749862L;

    /**
     * Maps from {@link UserGroup} to the actions allowed for this group on the
     * {@link #idOfAccessControlledObjectAsString object to which this ACL belongs}.
     * The {@link WildcardPermission} objects stored in the value sets represent only the
     * action part, not the type or instance part. The {@link WildcardPermission} abstraction
     * is used for its wildcard implication logic. The {@link #hasPermission(SecurityUser, String, Iterable)}
     * method will construct a {@link WildcardPermission} from the action requested, and this
     * permission will then be matched against the permissions in this map's value sets.<p>
     * 
     * Note that no negated actions are part of this map. See also {@link #deniedActionsByUserGroup}.
     */
    private Map<UserGroup, Set<WildcardPermission>> allowedActionsByUserGroup;
    
    /**
     * Maps from {@link UserGroup} to the actions denied for this group on the
     * {@link #idOfAccessControlledObjectAsString object to which this ACL belongs}.
     * The {@link WildcardPermission} objects stored in the value sets represent only the
     * action part, not the type or instance part. The {@link WildcardPermission} abstraction
     * is used for its wildcard implication logic. The {@link #hasPermission(SecurityUser, String, Iterable)}
     * method will construct a {@link WildcardPermission} from the action requested, and this
     * permission will then be matched against the permissions in this map's value sets.<p>
     * 
     * Note that no negated actions are part of this map. See also {@link #allowedActionsByUserGroup}.
     */
    private Map<UserGroup, Set<WildcardPermission>> deniedActionsByUserGroup;

    public AccessControlListImpl() {
        this(new HashMap<UserGroup, Set<String>>());
    }
    
    public AccessControlListImpl(Map<UserGroup, Set<String>> permissionMap) {
        this.allowedActionsByUserGroup = new HashMap<>();
        this.deniedActionsByUserGroup = new HashMap<>();
        for (final Entry<UserGroup, Set<String>> permissionMapEntry : permissionMap.entrySet()) {
            setPermissions(permissionMapEntry.getKey(), permissionMapEntry.getValue());
        }
    }

    @Override
    public PermissionChecker.PermissionState hasPermission(SecurityUser user, String action, Iterable<? extends UserGroup> groupsOfWhichUserIsMember) {
        WildcardPermission requestedAction = new WildcardPermission(action, /* case sensitive */ true);
        for (final UserGroup userGroupOfWhichUserIsMember : groupsOfWhichUserIsMember) {
            final Set<WildcardPermission> allowedActions = allowedActionsByUserGroup.get(userGroupOfWhichUserIsMember);
            if (allowedActions != null) {
                for (final WildcardPermission allowedAction : allowedActions) {
                    if (allowedAction.implies(requestedAction)) {
                        return PermissionState.GRANTED;
                    }
                }
            }
            final Set<WildcardPermission> deniedActions = deniedActionsByUserGroup.get(userGroupOfWhichUserIsMember);
            if (deniedActions != null) {
                for (final WildcardPermission deniedAction : deniedActions) {
                    if (deniedAction.implies(requestedAction)) {
                        return PermissionState.REVOKED;
                    }
                }
            }
        }
        return PermissionState.NONE;
    }
    
    @Override
    public Map<UserGroup, Set<String>> getActionsByUserGroup() {
        final Map<UserGroup, Set<String>> result = new HashMap<>();
        for (final Entry<UserGroup, Set<WildcardPermission>> allowedEntry : allowedActionsByUserGroup.entrySet()) {
            for (final WildcardPermission permission : allowedEntry.getValue()) {
                Util.addToValueSet(result, allowedEntry.getKey(), permission.toString());
            }
        }
        for (final Entry<UserGroup, Set<WildcardPermission>> allowedEntry : deniedActionsByUserGroup.entrySet()) {
            for (final WildcardPermission permission : allowedEntry.getValue()) {
                Util.addToValueSet(result, allowedEntry.getKey(), "!"+permission.toString());
            }
        }
        return result;
    }
    
    @Override
    public boolean denyPermission(UserGroup userGroup, String action) {
        final boolean result;
        if (action.startsWith("!")) {
            result = addPermission(userGroup, action.substring(1));
        } else {
            result = Util.addToValueSet(deniedActionsByUserGroup, userGroup, new WildcardPermission(action, /* case sensitive */ true));
        }
        return result;
    }
    
    @Override
    public boolean addPermission(UserGroup userGroup, String action) {
        final boolean result;
        if (action.startsWith("!")) {
            result = denyPermission(userGroup, action.substring(1));
        } else {
            result = Util.addToValueSet(allowedActionsByUserGroup, userGroup, new WildcardPermission(action, /* case sensitive */ true));
        }
        return result;
    }

    @Override
    public boolean removePermission(UserGroup userGroup, String action) {
        final boolean result;
        if (action.startsWith("!")) {
            result = removeDenial(userGroup, action.substring(1));
        } else {
            result = Util.removeFromValueSet(allowedActionsByUserGroup, userGroup, new WildcardPermission(action, /* case sensitive */ true));
        }
        return result;
    }

    @Override
    public boolean removeDenial(UserGroup userGroup, String action) {
        final boolean result;
        if (action.startsWith("!")) {
            result = removeDenial(userGroup, action.substring(1));
        } else {
            result = Util.removeFromValueSet(deniedActionsByUserGroup, userGroup, new WildcardPermission(action, /* case sensitive */ true));
        }
        return result;
    }

    @Override
    public void setPermissions(UserGroup userGroup, Set<String> actions) {
        allowedActionsByUserGroup.remove(userGroup);
        deniedActionsByUserGroup.remove(userGroup);
        for (final String actionAsString : actions) {
            if (actionAsString.startsWith("!")) {
                denyPermission(userGroup, actionAsString.substring(1));
            } else {
                addPermission(userGroup, actionAsString);
            }
        }
    }

    @Override
    public String toString() {
        return "AccessControlList [actionsByUserGroup=" + getActionsByUserGroup() + "]";
    }

}
