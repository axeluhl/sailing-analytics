package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Mark;

/**
 * A course that does not consist of {@link Mark Marks} existing in a regatta but instead is defined by
 * {@link MarkConfiguration MarkConfigurations}. This means changes to the model are easily possible without requiring
 * to pollute the {@link RegattaLog}. The effective Marks will then be created upon Course creation based on the
 * {@link MarkConfiguration MarkConfigurations}.
 */
public interface CourseWithMarkConfiguration {

    Iterable<MarkConfiguration> getMarkMappings();

    Iterable<WaypointWithMarkConfiguration> getWaypoints();
}
