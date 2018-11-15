package com.sap.sse.security.ui.shared;

import java.util.UUID;

import com.sap.sse.security.shared.AccessControlList;
import com.sap.sse.security.shared.Ownership;
import com.sap.sse.security.shared.RoleDefinitionImpl;
import com.sap.sse.security.shared.SecuredObject;
import com.sap.sse.security.shared.SecurityInformationDTO;

public class RoleDefinitionDTO extends RoleDefinitionImpl implements SecuredObject {
    
    private static final long serialVersionUID = -3340211553071045099L;

    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    @Deprecated
    RoleDefinitionDTO() {} // for GWT serialization only
    
    public RoleDefinitionDTO(final UUID id, final String name) {
        super(id, name);
    }

    @Override
    public final AccessControlList getAccessControlList() {
        return securityInformation.getAccessControlList();
    }

    @Override
    public final Ownership getOwnership() {
        return securityInformation.getOwnership();
    }

    @Override
    public final void setAccessControlList(final AccessControlList accessControlList) {
        this.securityInformation.setAccessControlList(accessControlList);
    }

    @Override
    public final void setOwnership(final Ownership ownership) {
        this.securityInformation.setOwnership(ownership);
    }

}
