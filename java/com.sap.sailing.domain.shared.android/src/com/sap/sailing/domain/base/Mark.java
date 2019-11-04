package com.sap.sailing.domain.base;

import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sse.common.IsManagedByCache;


/**
 * A marks name is used as its ID which is only identifying the mark uniquely within a single race or course
 * definition.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Mark extends CommonMarkProperties, ControlPoint, IsManagedByCache<SharedDomainFactory<?>> {

    UUID getOriginatingMarkTemplateIdOrNull();

    UUID getOriginatingMarkPropertiesIdOrNull();
}
