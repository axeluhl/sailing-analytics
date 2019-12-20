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

public class CourseConfigurationWithMarkRolesImpl<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        extends CourseConfigurationBaseImpl<MarkRole, MarkConfigurationT>
        implements CourseConfigurationWithMarkRoles<MarkConfigurationT> {
    private static final long serialVersionUID = 6040984618331175522L;

    public CourseConfigurationWithMarkRolesImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfigurationT> markConfigurations, Map<MarkConfigurationT, MarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<MarkConfigurationT>> waypoints, RepeatablePart optionalRepeatablePart, Integer numberOfLaps,
            String name, URL optionalImageURL) {
        super(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints, optionalRepeatablePart,
                numberOfLaps, name, optionalImageURL);
    }

}
