package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.shared.dto.SecurityInformationDTO;
import com.sap.sse.security.ui.client.UserManagementService;

/**
 * A proxy for a {@link SecuredDTO}, reduced to its {@link HasPermissions} permission type, the type-relative
 * identifier, and an optional name, plus the security information, in particular the ownership and ACL data.
 * With this, such a proxy can be used, e.g., in permission checks also on a client which may save the transmission
 * of a large DTO only for the purpose of such a check.<p>
 * 
 * See also {@link UserManagementService#addSecurityInformation(SecuredDTO)} which can be used to augment such
 * a proxy constructed on the client with the corresponding security information coming from the server's
 * {@link SecurityService}.<p>
 * 
 * TODO: Think about to implement {@link CustomFieldSerializer}.
 * @author udowessels
 * @author Axel Uhl (d043530)
 *
 */
public class EssentialSecuredDTO extends SecurityInformationDTO implements SecuredDTO, IsSerializable {

    private static final long serialVersionUID = -5174060227113723186L;

    private HasPermissions permissionType;
    private TypeRelativeObjectIdentifier typeRelativeObjectIdentifier;
    private String name;

    @Deprecated
    EssentialSecuredDTO() {
    }

    public EssentialSecuredDTO(HasPermissions permissionType, String name, TypeRelativeObjectIdentifier typeRelativeObjectIdentifier) {
        this.typeRelativeObjectIdentifier = typeRelativeObjectIdentifier;
        this.permissionType = permissionType;
        this.name = name;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier());
    }

    public TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier() {
        return typeRelativeObjectIdentifier;
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
