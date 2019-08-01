package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.NamedWithID;

public interface CourseWithMarkConfiguration extends NamedWithID {

    Iterable<MarkConfiguration> getMarkMappings();

    Iterable<WaypointWithMarkConfiguration> getWaypoints();
}
