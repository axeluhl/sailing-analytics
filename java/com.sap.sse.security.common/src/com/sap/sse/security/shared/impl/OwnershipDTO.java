package com.sap.sse.security.shared.impl;

import com.sap.sse.security.shared.AbstractOwnership;
import com.sap.sse.security.shared.UserGroupDTO;

public class OwnershipDTO extends AbstractOwnership<UserGroupDTO, StrippedUserDTO> {
    private static final long serialVersionUID = -6379054499434958440L;

    @Deprecated // for GWT serialization only
    private OwnershipDTO() {
        super(null, null);
    }

    public OwnershipDTO(StrippedUserDTO userOwner, UserGroupDTO tenantOwner) {
        super(userOwner, tenantOwner);
    }

}
