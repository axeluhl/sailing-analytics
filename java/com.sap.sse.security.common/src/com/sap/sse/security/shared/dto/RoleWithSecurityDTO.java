package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class RoleWithSecurityDTO extends RoleDTO implements SecuredDTO {
    private static final long serialVersionUID = 8806221826223137989L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private QualifiedObjectIdentifier identifier;

    @Deprecated
    RoleWithSecurityDTO() {

    }

    public RoleWithSecurityDTO(StrippedRoleDefinitionDTO roleDefinition, StrippedUserGroupDTO qualifiedForTenant,
            StrippedUserDTO qualifiedForUser, QualifiedObjectIdentifier identifier) {
        super(roleDefinition, qualifiedForTenant, qualifiedForUser);
        this.identifier = identifier;
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public void setAccessControlList(AccessControlListDTO accessControlList) {
        securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public void setOwnership(OwnershipDTO ownership) {
        securityInformation.setOwnership(ownership);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return this.identifier;
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.ROLE_ASSOCIATION;
    }

    @Override
    public String getName() {
        return super.getRoleDefinition().getName();
    }

}
