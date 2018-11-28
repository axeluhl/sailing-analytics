package com.sap.sse.security.shared.dto;

public interface SecuredDTO {
    AccessControlListDTO getAccessControlList();

    OwnershipDTO getOwnership();

    void setAccessControlList(AccessControlListDTO createAccessControlListDTO);

    void setOwnership(OwnershipDTO createOwnershipDTO);
}
