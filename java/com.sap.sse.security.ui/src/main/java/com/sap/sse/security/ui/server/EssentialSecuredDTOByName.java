package com.sap.sse.security.ui.server;

import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class EssentialSecuredDTOByName implements SecuredDTO {

    private static final long serialVersionUID = -5174060227113723186L;

    private AccessControlListDTO accessControlListDTO;

    private OwnershipDTO ownershipDTO;

    private final String name;

    private final HasPermissions permissionType;

    public EssentialSecuredDTOByName(String name, HasPermissions permissionType) {
        this.name = name;
        this.permissionType = permissionType;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(getName());
    }

    @Override
    public HasPermissions getPermissionType() {
        return permissionType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AccessControlListDTO getAccessControlList() {
        return accessControlListDTO;
    }

    @Override
    public OwnershipDTO getOwnership() {
        return ownershipDTO;
    }

    @Override
    public void setAccessControlList(AccessControlListDTO accessControlListDTO) {
        this.accessControlListDTO = accessControlListDTO;
    }

    @Override
    public void setOwnership(OwnershipDTO ownershipDTO) {
        this.ownershipDTO = ownershipDTO;
    }

}
