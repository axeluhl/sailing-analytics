package com.sap.sailing.domain.coursetemplate;

import java.util.UUID;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.coursetemplate.MarkTemplate.MarkTemplateResolver;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

/**
 * A template for creating a {@link Mark}. It has a globally unique ID and can be used in zero or more
 * {@link CourseTemplate}s. It is immutable and contains all {@link CommonMarkProperties common properties for a mark}.
 * Being a special {@link ControlPointTemplate}, a {@link MarkTemplate} returns a singleton collection containing itself
 * when asked for its {@link #getMarks() marks}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface MarkTemplate extends ControlPointTemplate, NamedWithUUID, CommonMarkProperties,
        WithQualifiedObjectIdentifier, IsManagedByCache<MarkTemplateResolver> {
    public interface MarkTemplateResolver {
        
        MarkTemplate getOrCreate(MarkTemplate markTemplate);
    }
    
    @Override
    default IsManagedByCache<MarkTemplateResolver> resolve(MarkTemplateResolver resolver) {
        return resolver.getOrCreate(this);
    }
    
    public static TypeRelativeObjectIdentifier getTypeRelativeObjectIdentifier(UUID markTemplateUUID) {
        return new TypeRelativeObjectIdentifier(markTemplateUUID.toString());
    }
    
    @Override
    default QualifiedObjectIdentifier getIdentifier() {
        return getPermissionType().getQualifiedObjectIdentifier(getTypeRelativeObjectIdentifier(getId()));
    }
    
    @Override
    default HasPermissions getPermissionType() {
        return SecuredDomainType.MARK_TEMPLATE;
    }
}
