package com.sap.sse.security.shared;

import java.util.List;
import java.util.Set;

/**
 * The {@link PermissionChecker} is an implementation of the permission 
 * checking algorithm proposed in PermissionConcept.docx section 10. It 
 * checks permissions in four stages where earlier stages overwrite later.
 * 
 * 1.   Firstly,  the ownership of the data object is tested. If the 
 *      requesting user is the owner of the data object he is automatically
 *      granted every possible permission for that data object.
 * 2.   Secondly, the data object's ACL is checked if it grants or explicitly
 *      revokes the permission.
 * 3.   Thereafter, the permissions directly assigned to the user are checked.
 * 4.   Finally the permissions implied by roles the user has are checked.
 *      This includes dynamic and parametrized roles.
 *      
 * @author Jonas Dann
 */
public class PermissionChecker {
    public enum PermissionState {
        GRANTED,
        REVOKED,
        NONE
    }
    
    /**
     * @param permission Permission of the form "data_object_type:action:instance_id".
     *          The instance id can be omitted when a general permission for the data
     *          object type is asked after (e.g. "event:create").
     */
    public static boolean isPermitted(WildcardPermission permission, SecurityUser user, Iterable<UserGroup> groupsOfWhichUserIsMember,
            Iterable<Role> roles, Ownership ownership,
            AccessControlList acl) {
        List<Set<String>> parts = permission.getParts();
        // permission has at least data object type and action as parts
        // and data object part only has one sub-part
        if (parts.get(0).size() != 1) {
            throw new WrongPermissionFormatException(permission);
        }
        PermissionState result = PermissionState.NONE;
        
        // 1. check user ownership
        if (ownership != null && user.equals(ownership.getUserOwner())) {
            result = PermissionState.GRANTED;
        }
        // 2. check ACL
        else if (acl != null) {
            // if no specific action is requested then this translates to a request for all permissions ("*")
            String action = parts.size() < 2 ? WildcardPermission.WILDCARD_TOKEN : (String) parts.get(1).toArray()[0];
            result = acl.hasPermission(user, action, groupsOfWhichUserIsMember);
        }
        // 3. check direct permissions
        if (result == PermissionState.NONE) {
            for (WildcardPermission directPermission : user.getPermissions()) {
                if (directPermission.implies(permission)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        // 4. check role permissions
        if (result == PermissionState.NONE) {
            for (Role role : roles) {
                if (implies(role, permission, ownership)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        return result == PermissionState.GRANTED;
    }
    
    /**
     * @param role
     *            the role; its name can, e.g., be of the form "role_title:tenant". The tenant is an optional parameter.
     *            It restricts permissions with a * as the instance id to data objects where the tenant parameter equals
     *            the tenant owner of the data object. E.g.: event-admin:tw2016 -> {event:edit:*, regatta:edit:*} This
     *            role would grant the user edit permission for every event and regatta where the tenant owner is
     *            "tw2016".
     * @param permission
     *            E.g. "regatta:edit:tw2016-dyas" (would return true if "tw2016-dyas" would have "tw2016" as the tenant
     *            owner)
     * @param ownership
     *            Ownership of the data object for which the {@code permission} is requested
     */
   private static boolean implies(Role role, WildcardPermission permission, Ownership ownership) {
        String[] parts = role.getName().split(":");
        // if there is no parameter or the first parameter (tenant) equals the tenant owner
        // TODO consider user as Role parameter, comparing to ownership.getUserOwner()
        if (parts.length < 2 || (ownership != null && ownership.getTenantOwner().getId().toString().equals(parts[1]))) {
            for (WildcardPermission rolePermission : role.getPermissions()) {
                if (rolePermission.implies(permission)) {
                    return true;
                }
            }
        }
        return false;
    }
}