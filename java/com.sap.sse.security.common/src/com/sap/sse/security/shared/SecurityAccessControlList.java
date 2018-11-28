package com.sap.sse.security.shared;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Grants and revokes permissions to a set of actions for an object on a per-{@link UserGroup} basis. This way, there
 * should usually be at most one instance of this type defined for one object to which access is controlled. The sets of
 * actions are keyed by the {@link UserGroup} to which they are granted/revoked. An action would, e.g., be something
 * like "UPDATE" in the permission EVENT:UPDATE:84730-74837-47384-ab987f9. Note that nothing but the action is required
 * for each group because the ACL pertains to a single object such that the type (e.g., "EVENT") as well as the object
 * ID are known and don't need to and make no sense to be specified.
 * <p>
 * 
 * It is allowed to use the {@code "*"} wildcard as an action string, granting access to all actions on the object
 * concerned. The same logic for permission implication as on {@link WildcardPermission} is applied when matching a
 * particular action passed to the {@link #hasPermission(String, Iterable)} method against the actions
 * allowed by this access control list (ACL).
 * <p>
 * 
 * The actions to be permitted or forbidden are provided as strings. To forbid a action, the action string is prefixed
 * with an exclamation mark '!'.
 * <p>
 * 
 * An access control list of this type can be combined with an object ID and display name into an
 * {@link AccessControlListAnnotation}, or it may, e.g., be attached directly to a DTO for serialization to a client.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface SecurityAccessControlList<G extends AbstractUserGroup<?>> extends Serializable {
    /**
     * Checks whether this access control list grants the {@code user} the permission to execute {@code action} on the
     * object to which this ACL pertains.
     * 
     * @param action
     *            the action to check permission for
     * @param groupsOfWhichUserIsMember
     *            must not be {@code null} but may be empty
     */
    PermissionChecker.PermissionState hasPermission(String action, Iterable<G> groupsOfWhichUserIsMember);

    /**
     * @return allowed actions are represented simply as strings and may contain the wildcard string {@code "*"} to
     *         represent "all actions;" action strings starting with an exclamation mark {@code '!'} represent actions
     *         the key user group is denied.
     */
    Map<G, Set<String>> getActionsByUserGroup();

    /**
     * @param actionToAllow
     *            the action to be permitted. The wildcard string {@code "*"} can be used to grant permission for all
     *            possible actions to the {@code userGroup}. Prefixing the action by an exclamation mark character
     *            {@code '!'} denies the action that follows. Multiple leading exclamation marks toggle accordingly.
     * @return {@code true} if the permission was added; {@code false} if the permission was already in this ACL and
     *         therefore didn't need to be added
     * @see #denyPermission(UserGroup, String)
     */
    boolean addPermission(G userGroup, String actionToAllow);

    /**
     * @param actionToDeny
     *            the action to be denied. The wildcard string {@code "*"} can be used to deny permission for all
     *            possible actions for the {@code userGroup}. Prefixing the action by an exclamation mark character
     *            {@code '!'} instead allows the action that follows. Multiple leading exclamation marks toggle
     *            accordingly.
     * @return {@code true} if the denial was added; {@code false} if the denial was already in this ACL and therefore
     *         didn't need to be added
     * @see #addPermission(UserGroup, String)
     */
    boolean denyPermission(G userGroup, String actionToDeny);

    /**
     * Removes a permission denial from those permissions denied for the user group. If the action starts with an
     * {@code "!"} exclamation mark, the exclamation mark is stripped, and {@link #removePermission(UserGroup, String)}
     * is invoked with the remaining string.
     * 
     * @return {@code true} if the permission was removed; {@code false} if the permission was not in this ACL and
     *         therefore didn't need to be removed
     */
    boolean removeDenial(G userGroup, String substring);

    /**
     * Removes a permission from those permissions granted to the user group. If the action starts with an {@code "!"}
     * exclamation mark, the exclamation mark is stripped, and {@link #removeDenial(UserGroup, String)} is invoked with
     * the remaining string.
     * 
     * @return {@code true} if the permission was removed; {@code false} if the permission was not in this ACL and
     *         therefore didn't need to be removed
     */
    boolean removePermission(G userGroup, String action);

    void setPermissions(G userGroup, Set<String> actions);

}