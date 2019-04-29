package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface SecuredDTO extends WithQualifiedObjectIdentifier {

    AccessControlListDTO getAccessControlList();

    OwnershipDTO getOwnership();

    void setAccessControlList(AccessControlListDTO createAccessControlListDTO);

    void setOwnership(OwnershipDTO createOwnershipDTO);
}
