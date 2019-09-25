package com.sap.sailing.domain.coursetemplate.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationImpl implements CourseConfiguration {
    private static final long serialVersionUID = -41810982418755899L;
    
    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private final Set<MarkConfiguration> markConfigurations;
    private final Map<MarkConfiguration, String> associatedRoles;
    private final List<WaypointWithMarkConfiguration> waypoints;
    private final RepeatablePart optionalRepeatablePart;
    private final String name;

    private Integer numberOfLaps;
    
    public CourseConfigurationImpl(CourseTemplate optionalCourseTemplate, Set<MarkConfiguration> markConfigurations,
            Map<MarkConfiguration, String> associatedRoles, List<WaypointWithMarkConfiguration> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name) {
        super();
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.markConfigurations = markConfigurations;
        this.associatedRoles = associatedRoles;
        this.waypoints = waypoints;
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.numberOfLaps = numberOfLaps;
        this.name = name;
    }

    @Override
    public RepeatablePart getRepeatablePart() {
        return optionalRepeatablePart;
    }

    @Override
    public CourseTemplate getOptionalCourseTemplate() {
        return optionalCourseTemplate;
    }

    @Override
    public Iterable<MarkConfiguration> getAllMarks() {
        return markConfigurations;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration> getWaypoints() {
        return waypoints;
    }

    @Override
    public Map<MarkConfiguration, String> getAssociatedRoles() {
        return associatedRoles;
    }

    @Override
    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration> getWaypoints(int numberOfLaps) {
        final Iterable<WaypointWithMarkConfiguration> result;
        if (hasRepeatablePart()) {
            if (numberOfLaps < 1) {
                throw new IllegalArgumentException("The course template "+this+" has a repeatable part, hence the number of laps needs to be at least 1.");
            }
            result = optionalRepeatablePart.createSequence(numberOfLaps, waypoints);
        } else {
            result = waypoints;
        }
        return result;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
