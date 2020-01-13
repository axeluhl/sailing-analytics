package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationImpl<P> extends CourseConfigurationBaseImpl<P> implements CourseConfiguration<P> {
    private static final long serialVersionUID = 4471259140392773170L;

    public CourseConfigurationImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfiguration<P>> markConfigurations,
            Map<MarkConfiguration<P>, MarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<P>> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name, String shortName, URL optionalImageURL) {
        super(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints, optionalRepeatablePart,
                numberOfLaps, name, shortName, optionalImageURL);
    }
}
