package com.sap.sailing.domain.coursetemplate.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.IsMarkRole;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationImpl extends CourseConfigurationBaseImpl<IsMarkRole> implements CourseConfiguration {

    private static final long serialVersionUID = 4471259140392773170L;

    public CourseConfigurationImpl(CourseTemplate optionalCourseTemplate, Set<MarkConfiguration> markConfigurations,
            Map<MarkConfiguration, IsMarkRole> associatedRoles, List<WaypointWithMarkConfiguration> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name) {
        super(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints, optionalRepeatablePart,
                numberOfLaps, name);
    }

}
