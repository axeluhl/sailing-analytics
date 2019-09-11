package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class SwissTimingArchiveConfigurationWithSecurityDTO implements IsSerializable, SecuredDTO {
    private static final long serialVersionUID = 108023338445751985L;

    private String jsonUrl;
    private String creatorName;
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    public SwissTimingArchiveConfigurationWithSecurityDTO() {
    }

    public SwissTimingArchiveConfigurationWithSecurityDTO(String jsonUrl, String creatorName) {
        super();
        this.jsonUrl = jsonUrl;
        this.creatorName = creatorName;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getCreatorName() {
        return creatorName;
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
    public void setAccessControlList(AccessControlListDTO createAccessControlListDTO) {
        securityInformation.setAccessControlList(createAccessControlListDTO);
    }

    @Override
    public void setOwnership(OwnershipDTO createOwnershipDTO) {
        securityInformation.setOwnership(createOwnershipDTO);
    }

    public SecurityInformationDTO getSecurityInformation() {
        return securityInformation;
    }

    @Override
    public String getName() {
        return jsonUrl;
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT;
    }

    private TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return creatorName == null ? new TypeRelativeObjectIdentifier(jsonUrl)
                : new TypeRelativeObjectIdentifier(jsonUrl, creatorName);
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }
}
