package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sse.common.NamedWithID;

/**
 * What a {@link WaypointTemplate} references. It is a template for the construction of a {@link ControlPoint} and
 * references the {@link MarkTemplate}s from which this control point shall be constructed.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ControlPointWithMarkTemplateMapping extends NamedWithID {
    Iterable<MarkTemplateMapping> getMarkMappings();
}
