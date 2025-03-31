package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public class PermissionAndRoleAssociation {
    public static TypeRelativeObjectIdentifier get(Role role, User userWithRole) {
        final String ownerTenantString;
        UserGroup ownerTenant = role.getQualifiedForTenant();
        if (ownerTenant != null) {
            ownerTenantString = UserGroupImpl.getTypeRelativeObjectIdentifierAsString(ownerTenant);
        } else {
            ownerTenantString = "null";
        }
        final String ownerUserString;
        User ownerUser = role.getQualifiedForUser();
        if (ownerUser != null) {
            ownerUserString = ownerUser.getIdentifier().getTypeRelativeObjectIdentifier().toString();
        } else {
            ownerUserString = "null";
        }
        String roleDefinitionString = RoleDefinitionImpl.getTypeRelativeObjectIdentifierAsString(role.getRoleDefinition());
        String userWithRoleString = userWithRole.getIdentifier().getTypeRelativeObjectIdentifier().toString();
        TypeRelativeObjectIdentifier associationTypeRelativeId = new TypeRelativeObjectIdentifier(userWithRoleString, roleDefinitionString,
                ownerUserString, ownerTenantString);
        return associationTypeRelativeId;
    }
    
    public static WithQualifiedObjectIdentifier getWithQualifiedObjectIdentifier(final Role role, final User userWithRole) {
        return new WithQualifiedObjectIdentifier() {
            private static final long serialVersionUID = 2862539885831272118L;

            @Override
            public String getName() {
                return "Role "+role+" for user "+userWithRole.getName();
            }
            
            @Override
            public HasPermissions getPermissionType() {
                return SecuredSecurityTypes.ROLE_ASSOCIATION;
            }
            
            @Override
            public QualifiedObjectIdentifier getIdentifier() {
                return getPermissionType().getQualifiedObjectIdentifier(get(role, userWithRole));
            }
        };
    }

    public static WithQualifiedObjectIdentifier getWithQualifiedObjectIdentifier(final WildcardPermission permission, final User userWithPermission) {
        return new WithQualifiedObjectIdentifier() {
            private static final long serialVersionUID = 2862539885831272118L;

            @Override
            public String getName() {
                return "Permission "+permission+" for user "+userWithPermission.getName();
            }
            
            @Override
            public HasPermissions getPermissionType() {
                return SecuredSecurityTypes.PERMISSION_ASSOCIATION;
            }
            
            @Override
            public QualifiedObjectIdentifier getIdentifier() {
                return getPermissionType().getQualifiedObjectIdentifier(get(permission, userWithPermission));
            }
        };
    }

    public static TypeRelativeObjectIdentifier get(WildcardPermission permission, User userWithPermission) {
        String permissionDefinitionString = permission.toString();
        TypeRelativeObjectIdentifier userWithRoleString = userWithPermission.getIdentifier()
                .getTypeRelativeObjectIdentifier();
        TypeRelativeObjectIdentifier associationTypeRelativeId = new TypeRelativeObjectIdentifier(
                userWithRoleString.toString(), permissionDefinitionString);
        return associationTypeRelativeId;
    }
}

