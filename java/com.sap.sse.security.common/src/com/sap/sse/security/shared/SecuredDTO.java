package com.sap.sse.security.shared;

import com.sap.sse.security.shared.impl.AccessControlListDTO;
import com.sap.sse.security.shared.impl.OwnershipDTO;

public interface SecuredDTO {
    AccessControlListDTO getAccessControlList();

    OwnershipDTO getOwnership();

    void setAccessControlList(AccessControlListDTO createAccessControlListDTO);

    void setOwnership(OwnershipDTO createOwnershipDTO);
}
