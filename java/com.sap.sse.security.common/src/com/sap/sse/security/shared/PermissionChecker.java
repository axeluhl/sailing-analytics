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
            Iterable<Role> roles, RolePermissionModel rolePermissionModel,
            Ownership ownership, AccessControlList acl) {
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
                if (rolePermissionModel.implies(role, permission, ownership)) {
                    result = PermissionState.GRANTED;
                    break;
                }
            }
        }
        return result == PermissionState.GRANTED;
    }
}