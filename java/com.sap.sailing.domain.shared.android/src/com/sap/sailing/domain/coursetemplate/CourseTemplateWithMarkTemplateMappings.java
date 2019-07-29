package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.NamedWithID;

public interface CourseTemplateWithMarkTemplateMappings extends NamedWithID {

    Iterable<MarkTemplateMapping> getMarkMappings();

    Iterable<WaypointTemplateWithMarkTemplateMapping> getWaypoints(int numberOfLaps);
}
