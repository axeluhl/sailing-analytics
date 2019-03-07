package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class DeviceConfigurationWithSecurityDTO extends DeviceConfigurationDTO implements SecuredDTO{

    private static final long serialVersionUID = 7187283665921500148L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private QualifiedObjectIdentifier identifier;

    @Deprecated
    DeviceConfigurationWithSecurityDTO() {
    }

    public DeviceConfigurationWithSecurityDTO(QualifiedObjectIdentifier identifier) {
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
        return SecuredDomainType.RACE_MANAGER_APP_DEVICE_CONFIGURATION;
    }

    @Override
    public String getName() {
        return super.name;
    }
}
