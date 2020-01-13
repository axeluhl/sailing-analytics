package com.sap.sailing.domain.coursetemplate;

import java.util.Collections;
import java.util.UUID;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sse.common.Named;
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
 * 
 * TODO after removing IsMarkRole, all needs to revert to MarkRole. The original idea
 * was to represent mark role "proxies" by objects of a type compatible to an
 * interface that MarkRole and the proxy type have in common, leading to
 * MarkRoleName[Impl]. However, I think it would be better to represent the
 * request for the creation of a role with a specific name during the creation
 * of a course template from a CourseConfiguration by annotating the MarkConfiguration
 * not only with storeToInventory and an optional Positioning object but also
 * by an optional mark role creation specification. This can be part of the
 * annotation type used for MarkConfiguration objects during the "request phase"
 * as implemented on branch bug5168 and already merged to bug5085. The merge
 * of those changes into branch bug5165 currently is causing conflicts that should
 * be solved.

 */
public interface MarkRole extends Named, NamedWithUUID, WithQualifiedObjectIdentifier, ControlPointTemplate {
    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID markPropertiesUUID) {
        return new TypeRelativeObjectIdentifier(markPropertiesUUID.toString());
    }
    
    @Override
    default Iterable<MarkRole> getMarkRoles() {
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
