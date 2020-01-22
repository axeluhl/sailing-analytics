package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

public class UrlDTO implements SecuredDTO {
    private static final long serialVersionUID = -120992264719912427L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();
    private String url; // Can't use type URL since GWT does not support java.net.*

    @SuppressWarnings("unused")
    private UrlDTO() { } // For GWT serialization

    public UrlDTO(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return url;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(new TypeRelativeObjectIdentifier(url));
    }

    @Override
    public HasPermissions getType() {
        return SecuredDomainType.RESULT_IMPORT_URL;
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
    
}
