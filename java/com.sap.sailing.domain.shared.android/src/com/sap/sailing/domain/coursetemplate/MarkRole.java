package com.sap.sailing.domain.coursetemplate;

import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface MarkRole extends NamedWithUUID, WithQualifiedObjectIdentifier {

    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID markPropertiesUUID) {
        return new TypeRelativeObjectIdentifier(markPropertiesUUID.toString());
    }
    
    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier(getId()));
    }
    
    @Override
    default HasPermissions getPermissionType() {
        return SecuredDomainType.MARK_ROLE;
    }
}
