package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.dto.NamedSecuredObjectDTO;
import com.sap.sse.security.shared.dto.SecuredDTO;

public class EssentialSecuredDTO  extends NamedSecuredObjectDTO implements SecuredDTO, IsSerializable {

    private static final long serialVersionUID = -5174060227113723186L;

    @Deprecated
    EssentialSecuredDTO() {
        super(); // for GWT serialization only
        //this.permissionType = null;
    }

    public EssentialSecuredDTO(String name, HasPermissions permissionType) {
        super(name);
        //this.permissionType = permissionType;
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
        return null;
    }

}
