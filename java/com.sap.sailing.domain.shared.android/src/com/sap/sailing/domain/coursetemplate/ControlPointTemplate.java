package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sse.common.Named;

/**
 * What a {@link WaypointTemplate} references. It is a template for the construction of a {@link ControlPoint} and
 * references the {@link MarkRole}s that describe the "logical" marks. The {@link MarkTemplate}s from which the mark
 * properties are to be drawn during construction of the actual {@link ControlPoint} are provided by the
 * {@link CourseTemplate} (see {@link CourseTemplate#getDefaultMarkTemplateForRole(MarkRole)}).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ControlPointTemplate extends Named {
    Iterable<MarkRole> getMarkRoles();

    String getShortName();
}
