package com.sap.sse.security.shared;

import java.util.Map;
import java.util.Set;

/**
 * Grants and revokes permissions to a set of actions for an object identified by an ID provided as String on a
 * per-{@link UserGroup} basis. This way, there should usually be at most one instance of this type defined for one
 * object to which access is controlled. The sets of actions are keyed by the {@link UserGroup} to which they are
 * granted/revoked. An action would, e.g., be something like "UPDATE" in the permission
 * EVENT:UPDATE:84730-74837-47384-ab987f9. Note that nothing but the action is required because the ACL pertains to a
 * single object such that the type (e.g., "EVENT") as well as the object ID as described by
 * {@link #getIdOfAccessControlledObjectAsString()} are known and don't need to and make no sense to be specified.
 * <p>
 * 
 * The actions to be permitted or forbidden are provided as strings. To forbid a action, the action string is prefixed
 * with an exclamation mark '!'.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AccessControlList {
    PermissionChecker.PermissionState hasPermission(SecurityUser user, String action, Iterable<? extends UserGroup> groupsOfWhichUserIsMember);

    String getIdOfAccessControlledObjectAsString();

    String getDisplayNameOfAccessControlledObject();

    Map<UserGroup, Set<String>> getActionsByUserGroup();

    /**
     * @return {@code true} if the permission was added; {@code false} if the permission was already in this ACL and
     *         therefore didn't need to be added
     */
    boolean addPermission(UserGroup userGroup, String action);
   
    /**
     * @return {@code true} if the permission was removed; {@code false} if the permission was not in this ACL and
     *         therefore didn't need to be removed
     */
    boolean removePermission(UserGroup userGroup, String action);

    void setPermissions(UserGroup userGroup, Set<String> actions);

}