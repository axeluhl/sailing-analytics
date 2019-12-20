package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfigurationBase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.IsMarkRole;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public abstract class CourseConfigurationBaseImpl<R extends IsMarkRole, MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
implements CourseConfigurationBase<R, MarkConfigurationT> {
    
    private static final long serialVersionUID = -9189989170055144298L;

    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private final Set<MarkConfigurationT> markConfigurations;
    private final Map<MarkConfigurationT, R> associatedRoles;
    private final List<WaypointWithMarkConfiguration<MarkConfigurationT>> waypoints;
    private final RepeatablePart optionalRepeatablePart;
    private final Integer numberOfLaps;
    private final String name;
    private final URL optionalImageURL;
    
    public CourseConfigurationBaseImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfigurationT> markConfigurations, Map<MarkConfigurationT, R> associatedRoles,
            List<WaypointWithMarkConfiguration<MarkConfigurationT>> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name, URL optionalImageURL) {
        super();
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.markConfigurations = markConfigurations;
        this.associatedRoles = associatedRoles;
        this.waypoints = waypoints;
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.numberOfLaps = numberOfLaps;
        this.name = name;
        this.optionalImageURL = optionalImageURL;
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
    public Iterable<? extends MarkConfigurationT> getAllMarks() {
        return markConfigurations;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration<MarkConfigurationT>> getWaypoints() {
        return waypoints;
    }

    @Override
    public Map<MarkConfigurationT, R> getAssociatedRoles() {
        return associatedRoles;
    }
    
    @Override
    public Map<MarkConfigurationT, R> getAllMarksWithOptionalRoles() {
        final Map<MarkConfigurationT, R> result = new HashMap<>();
        for (MarkConfigurationT mc : markConfigurations) {
            result.put(mc, associatedRoles.get(mc));
        }
        return result;
    }

    @Override
    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration<MarkConfigurationT>> getWaypoints(int numberOfLaps) {
        final Iterable<WaypointWithMarkConfiguration<MarkConfigurationT>> result;
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

    @Override
    public URL getOptionalImageURL() {
        return optionalImageURL;
    }
}
