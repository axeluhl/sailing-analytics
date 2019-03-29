package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class AccountWithSecurityDTO implements SecuredDTO {
    private static final long serialVersionUID = 176992188692729118L;
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    private String email;
    private String name;

    public AccountWithSecurityDTO(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public AccountWithSecurityDTO() {
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
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
    public HasPermissions getType() {
        return SecuredDomainType.IGTIMI_ACCOUNT;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    private TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(email);
    }

}
