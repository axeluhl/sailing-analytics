package com.sap.sse.security.shared;

import java.util.List;
import java.util.Set;

import com.sap.sse.common.Util;

/**
 * The {@link PermissionChecker} is an implementation of the permission checking algorithm also described in <a href=
 * "https://wiki.sapsailing.com/wiki/info/security/permission-concept#algorithm-boolean-ispermitted-principalcollection-principals-wildcardpermission-permission-for-composite-realm">Permission
 * Concept Wiki article</a>. It checks permissions in four stages where earlier stages overwrite later.
 * 
 * 1. Firstly, the data object's ACL is checked if it grants or explicitly revokes the permission. 2. Thereafter, the
 * permissions directly assigned to the user are checked. 3. Finally the permissions implied by roles the user has are
 * checked. This includes roles qualified by the group/user ownerships of the objetcs to which they are applied.
 * 
 * @author Jonas Dann, Axel Uhl (d043530)
 */
public class PermissionChecker {
    public enum PermissionState {
        GRANTED,
        REVOKED,
        NONE
    }
    
    /**
     * @param permission
     *            Permission of the form "data_object_type:action:instance_id". The instance id can be omitted when a
     *            general permission for the data object type is asked after (e.g. "event:create"). If the action
     *            contains more than one sub-part (divided by {@link WildcardPermission#SUBPART_DIVIDER_TOKEN}) then
     *            this is considered an error, and an {@link IllegalArgumentException} will be thrown.
     * @param ownership
     *            may be {@code null}, causing user- or tenant-parameterized roles and no user ownership override to be
     *            applied
     * @param acl
     *            may be {@code null} in which case no ACL-specific checks are performed
     */
    public static boolean isPermitted(WildcardPermission permission, SecurityUser user,
            Iterable<UserGroup> groupsOfWhichUserIsMember, SecurityUser allUser,
            Iterable<UserGroup> allUserGroupsOfWhichUserIsMember, Ownership ownership, AccessControlList acl) {
        List<Set<String>> parts = permission.getParts();
        // permission has at least data object type and action as parts
        // and data object part only has one sub-part
        if (parts.get(0).size() != 1) {
            throw new WrongPermissionFormatException(permission);
        }
        PermissionState result = PermissionState.NONE;
        
        // 1. check ACL
        if (acl != null) {
            // if no specific action is requested then this translates to a request for all permissions ("*")
            final String action;
            if (parts.size() < 2) {
                action = WildcardPermission.WILDCARD_TOKEN;
            } else {
                if (parts.get(1).size() > 1) {
                    throw new IllegalArgumentException("Permission to check must not have more than one sub-part: "+parts.get(1));
                }
                action = (String) parts.get(1).toArray()[0];
            }
            result = acl.hasPermission(action, groupsOfWhichUserIsMember);
        }

        // anonymous can only grant it if not already decided by acl
        if (result == PermissionState.NONE) {
            PermissionState anonymous = checkUserPermissions(permission, allUser, ownership, result);
            if (anonymous == PermissionState.GRANTED) {
                result = anonymous;
            }
        }
        if (result == PermissionState.NONE) {
            result = checkUserPermissions(permission, allUser, ownership, result);
        }
        return result == PermissionState.GRANTED;
    }

    private static PermissionState checkUserPermissions(WildcardPermission permission, SecurityUser user,
            Ownership ownership, PermissionState result) {
        // 2. check direct permissions
        if (result == PermissionState.NONE && user != null) { // no direct permissions for anonymous users
            for (WildcardPermission directPermission : user.getPermissions()) {
                if (directPermission.implies(permission)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        // 3. check role permissions
        if (result == PermissionState.NONE && user != null) { // an anonymous user does not have any roles
            for (Role role : user.getRoles()) {
                if (implies(role, permission, ownership)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * @param role
     *            the role; it may have a {@link Role#getQualifiedForTenant() tenant} and/or
     *            {@link Role#getQualifiedForUser() user} qualification which means the role's permissions are granted
     *            only if the {@code ownership}'s {@link Ownership#getTenantOwner() tenant} and/or
     *            {@link Ownership#getUserOwner() user} owner match the role qualification. E.g.:
     *            {@code event-admin:tw2016 -> {event:edit:*, regatta:edit:*}} This role would grant the user edit
     *            permission for every event and regatta where the tenant owner is "tw2016".
     * @param permission
     *            the permission for which to check whether it shall be granted to the user for the object whose
     *            {@code ownership} information is provided. E.g. "regatta:edit:tw2016-dyas" (would return true if the
     *            object "tw2016-dyas" would have "tw2016" as the tenant owner in case the role is qualified with the
     *            "tw2016" tenant)
     * @param ownership
     *            Ownership of the data object for which the {@code permission} is requested
     */
   private static boolean implies(Role role, WildcardPermission permission, Ownership ownership) {
       final boolean roleIsTenantQualified = role.getQualifiedForTenant() != null;
       final boolean roleIsUserQualified = role.getQualifiedForUser() != null;
       final boolean permissionsApply;
       boolean result;
       if (roleIsTenantQualified || roleIsUserQualified) {
           if (ownership == null) {
               permissionsApply = false; // qualifications cannot be verified as no ownership info is provided; permissions do not apply
           } else {
               permissionsApply = (!roleIsTenantQualified || Util.equalsWithNull(role.getQualifiedForTenant(), ownership.getTenantOwner())) &&
                                  (!roleIsUserQualified || Util.equalsWithNull(role.getQualifiedForUser(), ownership.getUserOwner()));
           }
       } else {
           permissionsApply = true; // permissions apply without qualifications
       }
       if (permissionsApply) {
           result = false; // if the role grants no permissions at all or no permission implies the one requested for, access is not granted
            for (WildcardPermission rolePermission : role.getPermissions()) {
                if (rolePermission.implies(permission)) {
                    result = true;
                    break;
                }
            }
        } else {
            result = false;
        }
        return result;
    }
}