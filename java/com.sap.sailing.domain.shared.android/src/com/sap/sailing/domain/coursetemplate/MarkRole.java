package com.sap.sailing.domain.coursetemplate;

import java.util.Collections;
import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * A mark role defines the purpose of a mark used in the waypoint sequences of a regatta course or course template and
 * allows users to swap out marks or mark templates without changing the the effective waypoint sequence. Having this, a
 * course template and regatta course may define a compatible waypoint sequence while being based on different mark
 * definitions.<p>
 * 
 * An example of a role would be "1", representing the windward mark in a course sequence diagram.
 */
public interface MarkRole extends IsMarkRole, NamedWithUUID, WithQualifiedObjectIdentifier, ControlPointTemplate {
    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID markPropertiesUUID) {
        return new TypeRelativeObjectIdentifier(markPropertiesUUID.toString());
    }
    
    @Override
    default Iterable<MarkRole> getMarks() {
        return Collections.singleton(this);
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
