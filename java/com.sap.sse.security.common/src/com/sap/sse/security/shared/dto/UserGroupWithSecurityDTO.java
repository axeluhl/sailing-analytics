package com.sap.sse.security.shared.dto;

import java.util.HashSet;
import java.util.UUID;

public class UserGroupWithSecurityDTO extends UserGroupDTO implements SecuredDTO {
    private static final long serialVersionUID = 1L;
    
    private SecurityInformationDTO securityInformation = new SecurityInformationDTO();

    @Deprecated
    // GWT serializer only
    UserGroupWithSecurityDTO() {
        super();
    }

    public UserGroupWithSecurityDTO(HashSet<StrippedUserDTO> users, UUID id, String name) {
        super(users, id, name);
    }

    public UserGroupWithSecurityDTO(UUID id, String name) {
        super(new HashSet<StrippedUserDTO>(), id, name);
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
