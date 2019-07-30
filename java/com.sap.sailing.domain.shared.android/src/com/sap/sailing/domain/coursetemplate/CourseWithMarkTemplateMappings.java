package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.NamedWithID;

public interface CourseWithMarkTemplateMappings extends NamedWithID {

    Iterable<MarkTemplateMapping> getMarkMappings();

    Iterable<WaypointWithMarkTemplateMapping> getWaypoints();
}
