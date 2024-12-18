package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class IgtimiDeviceWithSecurityDTO implements SecuredDTO {
    private static final long serialVersionUID = 176992188692729118L;
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    private long id;
    private String name;
    private String serialNumber;
    private String serviceTag;

    public IgtimiDeviceWithSecurityDTO(long id, String serialNumber, String name, String serviceTag) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.name = name;
        this.serviceTag = serviceTag;
    }

    public SecurityInformationDTO getSecurityInformation() {
        return securityInformation;
    }

    public long getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getServiceTag() {
        return serviceTag;
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
    public HasPermissions getPermissionType() {
        return SecuredDomainType.IGTIMI_DEVICE;
    }

    @Override
    public String getName() {
        return name;
    }

    private TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(serialNumber);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

}
