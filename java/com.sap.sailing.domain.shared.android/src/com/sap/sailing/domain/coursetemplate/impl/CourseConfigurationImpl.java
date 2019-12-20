package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.IsMarkRole;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationImpl<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
extends CourseConfigurationBaseImpl<IsMarkRole, MarkConfigurationT> implements CourseConfiguration<MarkConfigurationT> {

    private static final long serialVersionUID = 4471259140392773170L;

    public CourseConfigurationImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfigurationT> markConfigurations,
            Map<MarkConfigurationT, IsMarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<MarkConfigurationT>> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name, URL optionalImageURL) {
        super(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints, optionalRepeatablePart,
                numberOfLaps, name, optionalImageURL);
    }

}
