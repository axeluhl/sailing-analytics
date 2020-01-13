package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.coursetemplate.CourseConfigurationBase;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public abstract class CourseConfigurationBaseImpl<P> implements CourseConfigurationBase<P> {
    
    private static final long serialVersionUID = -9189989170055144298L;

    private final String shortName;
    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private final Set<MarkConfiguration<P>> markConfigurations;
    private final Map<MarkConfiguration<P>, MarkRole> associatedRoles;
    private final List<WaypointWithMarkConfiguration<P>> waypoints;
    private final RepeatablePart optionalRepeatablePart;
    private final Integer numberOfLaps;
    private final String name;
    private final URL optionalImageURL;
    
    public CourseConfigurationBaseImpl(CourseTemplate optionalCourseTemplate,
            Set<MarkConfiguration<P>> markConfigurations, Map<MarkConfiguration<P>, MarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<P>> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name, String shortName, URL optionalImageURL) {
        super();
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.markConfigurations = markConfigurations;
        this.associatedRoles = associatedRoles;
        this.waypoints = waypoints;
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.numberOfLaps = numberOfLaps;
        this.name = name;
        this.shortName = shortName;
        this.optionalImageURL = optionalImageURL;
    }

    @Override
    public String getShortName() {
        return shortName;
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
    public Iterable<MarkConfiguration<P>> getAllMarks() {
        return markConfigurations;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration<P>> getWaypoints() {
        return waypoints;
    }

    @Override
    public Map<MarkConfiguration<P>, MarkRole> getAssociatedRoles() {
        return associatedRoles;
    }
    
    @Override
    public Map<MarkConfiguration<P>, MarkRole> getAllMarksWithOptionalRoles() {
        final Map<MarkConfiguration<P>, MarkRole> result = new HashMap<>();
        for (MarkConfiguration<P> mc : markConfigurations) {
            result.put(mc, associatedRoles.get(mc));
        }
        return result;
    }

    @Override
    public Integer getNumberOfLaps() {
        return numberOfLaps;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration<P>> getWaypoints(int numberOfLaps) {
        final Iterable<WaypointWithMarkConfiguration<P>> result;
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
