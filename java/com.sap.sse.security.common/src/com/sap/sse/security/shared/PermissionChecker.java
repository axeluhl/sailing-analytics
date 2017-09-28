package com.sap.sse.security.shared;

public class PermissionChecker {
    public static boolean isPermitted(WildcardPermission permission, String user, Iterable<String> directPermissions, Iterable<String> roles, 
            RolePermissionModel rolePermissionModel, Owner ownership, AccessControlList acl) {
        String[] parts = permission.toString().replaceAll("\\[|\\]", "").split(":"); // TODO getParts() und parts auf Variablen
        
        if (parts.length < 2) {
            throw new WrongPermissionFormatException(permission);
        } else if (parts.length > 2) {
            if (ownership != null && user.equals(ownership.getOwner())) { // TODO check for tenant ownership
                return true;
            }
            if (acl != null) {
                if (acl.hasPermission(user, "!" + parts[1])) { // TODO: tri-state in ACL Kapseln
                    return false;
                } else if (acl.hasPermission(user, parts[1])) {
                    return true;
                }
            }
        }
        for (String directPermission : directPermissions) {
            WildcardPermission directPerm = new WildcardPermission(directPermission); // TODO: pass direct as Wildcard
            if (directPerm.implies(permission)) {
                return true;
            }
        }
        for (String role : roles) { // VSAWEvent3:eventAdmin -> {regatta:update:*, event:update:*}
            for (String rolePermission : rolePermissionModel.getPermissions(role)) { // TODO implies on role with ownership to check for parametrized roles
                WildcardPermission rolePerm = new WildcardPermission(rolePermission);
                if (rolePerm.implies(permission)) { // event:update:Event3
                    return true;
                }
            }
        }
        return false; // TODO: single point of exit
    }
}