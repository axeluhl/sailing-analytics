package com.sap.sailing.domain.coursetemplate.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfigurationBase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.IsMarkRole;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public abstract class CourseConfigurationBaseImpl<R extends IsMarkRole> implements CourseConfigurationBase<R> {
    
    private static final long serialVersionUID = -9189989170055144298L;

    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private final Set<MarkConfiguration> markConfigurations;
    private final Map<MarkConfiguration, R> associatedRoles;
    private final List<WaypointWithMarkConfiguration> waypoints;
    private final RepeatablePart optionalRepeatablePart;
    private final String name;

    private Integer numberOfLaps;
    
    public CourseConfigurationBaseImpl(CourseTemplate optionalCourseTemplate, Set<MarkConfiguration> markConfigurations,
            Map<MarkConfiguration, R> associatedRoles, List<WaypointWithMarkConfiguration> waypoints,
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
    public Map<MarkConfiguration, R> getAssociatedRoles() {
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
