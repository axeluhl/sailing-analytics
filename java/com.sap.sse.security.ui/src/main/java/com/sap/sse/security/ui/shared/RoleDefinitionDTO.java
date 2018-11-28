package com.sap.sse.security.ui.shared;

import java.util.UUID;

import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.SecuredDTO;
import com.sap.sse.security.shared.SecurityInformationDTO;
import com.sap.sse.security.shared.impl.AccessControlListDTO;
import com.sap.sse.security.shared.impl.OwnershipDTO;

public class RoleDefinitionDTO extends RoleDefinitionImpl implements SecuredDTO {
    
    private static final long serialVersionUID = -3340211553071045099L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    @Deprecated
    RoleDefinitionDTO() {} // for GWT serialization only
    
    public RoleDefinitionDTO(final UUID id, final String name) {
        super(id, name);
    }

    @Override
    public final AccessControlListDTO getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final OwnershipDTO getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlListDTO accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final OwnershipDTO ownership) {
        this.securityInformation.setOwnership(ownership);
    }

}
