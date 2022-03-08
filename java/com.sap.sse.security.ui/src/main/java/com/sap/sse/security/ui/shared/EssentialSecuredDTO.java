package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;

/**
 * TODO: Think about to implement {@link CustomFieldSerializer}.
 * @author udowessels
 *
 */
public class EssentialSecuredDTO extends SecurityInformationDTO implements SecuredDTO, IsSerializable {

    private static final long serialVersionUID = -5174060227113723186L;

    private HasPermissions permissionType;
    private String[] typeRelativeObjectIdentifierParts;
    private String name;

    @Deprecated
    EssentialSecuredDTO() {
    }

    public EssentialSecuredDTO(HasPermissions permissionType, String name, String... typeRelativeObjectIdentifierParts) {
        this.typeRelativeObjectIdentifierParts = typeRelativeObjectIdentifierParts;
        this.permissionType = permissionType;
        this.name = name;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return new TypeRelativeObjectIdentifier(typeRelativeObjectIdentifierParts);
    }

    @Override
    public HasPermissions getPermissionType() {
        return permissionType;
    }

    @Override
    public String getName() {
        return name;
    }

}
