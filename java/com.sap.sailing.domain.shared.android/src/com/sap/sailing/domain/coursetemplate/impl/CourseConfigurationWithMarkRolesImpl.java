package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfigurationWithMarkRoles;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationWithMarkRolesImpl<P>
        extends CourseConfigurationBaseImpl<P>
        implements CourseConfigurationWithMarkRoles<P> {
    private static final long serialVersionUID = 6040984618331175522L;

    public CourseConfigurationWithMarkRolesImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfiguration<P>> markConfigurations, Map<MarkConfiguration<P>, MarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<P>> waypoints, RepeatablePart optionalRepeatablePart, Integer numberOfLaps,
            String name, String shortName, URL optionalImageURL) {
        super(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints, optionalRepeatablePart,
                numberOfLaps, name, shortName, optionalImageURL);
    }

}
