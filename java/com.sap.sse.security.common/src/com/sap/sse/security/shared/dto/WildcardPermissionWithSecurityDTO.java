package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

public class WildcardPermissionWithSecurityDTO extends WildcardPermission implements SecuredDTO {
    private static final long serialVersionUID = 4642830840512002234L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private QualifiedObjectIdentifier identifier;

    @Deprecated
    WildcardPermissionWithSecurityDTO() {
        super();
    }

    public WildcardPermissionWithSecurityDTO(String wildcardString, QualifiedObjectIdentifier identifier) {
        super(wildcardString);
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
        return SecuredSecurityTypes.PERMISSION_ASSOCIATION;
    }

    @Override
    public String getName() {
        return this.identifier.toString();
    }

}
