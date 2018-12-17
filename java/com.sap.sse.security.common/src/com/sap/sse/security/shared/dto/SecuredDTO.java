package com.sap.sse.security.shared.dto;

import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

public interface SecuredDTO {

    AccessControlListDTO getAccessControlList();

    OwnershipDTO getOwnership();

    TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(String... params);

    void setAccessControlList(AccessControlListDTO createAccessControlListDTO);

    void setOwnership(OwnershipDTO createOwnershipDTO);

    //void setTypeRelativeObjectIdentifier(TypeRelativeObjectIdentifier typeRelativeObjectIdentifier);

    //void setType(HasPermissions type);

}
