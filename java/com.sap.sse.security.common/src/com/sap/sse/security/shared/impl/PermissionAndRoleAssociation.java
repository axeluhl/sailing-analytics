package com.sap.sse.security.shared.impl;

public class PermissionAndRoleAssociation {
    public static String get(Role role, User userWithRole) {
        String ownerTenantString = "null";
        UserGroup ownerTenant = role.getQualifiedForTenant();
        if (ownerTenant != null) {
            ownerTenantString = ownerTenant.getIdentifier().getTypeRelativeObjectIdentifier();
        }
        String ownerUserString = "null";
        User ownerUser = role.getQualifiedForUser();
        if (ownerUser != null) {
            ownerUserString = ownerUser.getIdentifier().getTypeRelativeObjectIdentifier();
        }
        String roleDefinitionString = role.getRoleDefinition().getId().toString();
        String userWithRoleString = userWithRole.getIdentifier().getTypeRelativeObjectIdentifier();
        String associationTypeRelativeId = WildcardPermissionEncoder.encode(userWithRoleString, roleDefinitionString,
                ownerUserString, ownerTenantString);
        return associationTypeRelativeId;
    }
}

