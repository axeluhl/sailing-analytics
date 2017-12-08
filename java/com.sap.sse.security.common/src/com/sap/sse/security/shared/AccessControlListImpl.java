package com.sap.sse.security.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.PermissionChecker.PermissionState;

public class AccessControlListImpl implements AccessControlList {
    private String idOfAccessControlledObjectAsString;
    private String displayNameOfAccessControlledObject;
    
    /**
     * Maps from UserGroup name to the actions allowed/forbidden for this group on the
     * {@link #idOfAccessControlledObjectAsString object to which this ACL belongs}.
     * "Negated" permissions which forbid an action for a user in a group are expressed
     * by an exclamation mark '!' action name prefix.
     */
    private ConcurrentHashMap<UserGroup, Set<String>> actionsByUserGroup;

    @Deprecated
    protected AccessControlListImpl() {} // for GWT serialization only

    public AccessControlListImpl(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject) {
        this(idOfAccessControlledObjectAsString, displayNameOfAccessControlledObject, new HashMap<UserGroup, Set<String>>());
    }
    
    public AccessControlListImpl(String idOfAccessControlledObjectAsString, String displayNameOfAccessControlledObject, Map<UserGroup, Set<String>> permissionMap) {
        this.idOfAccessControlledObjectAsString = idOfAccessControlledObjectAsString;
        this.displayNameOfAccessControlledObject = displayNameOfAccessControlledObject;
        this.actionsByUserGroup = new ConcurrentHashMap<>(permissionMap);
    }

    @Override
    public PermissionChecker.PermissionState hasPermission(SecurityUser user, String action, Iterable<? extends UserGroup> groupsOfWhichUserIsMember) {
        for (final UserGroup userGroupOfWhichUserIsMember : groupsOfWhichUserIsMember) {
            final Set<String> actions = actionsByUserGroup.get(userGroupOfWhichUserIsMember);
            if (actions != null) {
                if (actions.contains("!" + action)) {
                    return PermissionState.REVOKED;
                } else if (actions.contains(action)) {
                    return PermissionState.GRANTED;
                }
            }
        }
        return PermissionState.NONE;
    }
    
    @Override
    public String getIdOfAccessControlledObjectAsString() {
        return idOfAccessControlledObjectAsString;
    }

    @Override
    public String getDisplayNameOfAccessControlledObject() {
        return displayNameOfAccessControlledObject;
    }

    @Override
    public Map<UserGroup, Set<String>> getActionsByUserGroup() {
        return Collections.unmodifiableMap(actionsByUserGroup);
    }
    
    @Override
    public boolean addPermission(UserGroup userGroup, String action) {
        return Util.addToValueSet(actionsByUserGroup, userGroup, action);
    }

    @Override
    public boolean removePermission(UserGroup userGroup, String action) {
        return Util.removeFromValueSet(actionsByUserGroup, userGroup, action);
    }

    @Override
    public void setPermissions(UserGroup userGroup, Set<String> actions) {
        actionsByUserGroup.put(userGroup, actions);
    }

    @Override
    public String toString() {
        return "AccessControlList for Object ID " + idOfAccessControlledObjectAsString
                + " (" + displayNameOfAccessControlledObject
                + "), actionsByUserGroup=" + actionsByUserGroup + "]";
    }

}
