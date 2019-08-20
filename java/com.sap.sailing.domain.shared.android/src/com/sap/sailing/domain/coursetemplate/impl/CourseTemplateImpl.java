package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedWithUUIDImpl;

public class CourseTemplateImpl extends NamedWithUUIDImpl implements CourseTemplate {
    private static final long serialVersionUID = -183875832585632806L;

    private final Iterable<MarkTemplate> marks;
    
    private final ArrayList<WaypointTemplate> waypoints;
    
    private final Map<MarkTemplate, String> associatedRoles;

    private Iterable<String> tags = new ArrayList<>();
    
    private final URL optionalImageURL;

    private final RepeatablePart optionalRepeatablePart;

    
    /** Creates a course template with a random UUID. */
    public CourseTemplateImpl(String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, String> associatedRoles, URL optionalImageURL) {
        this(UUID.randomUUID(), name, marks, waypoints, associatedRoles, optionalImageURL);
    }

    public CourseTemplateImpl(UUID id, String name, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, Map<MarkTemplate, String> associatedRoles, URL optionalImageURL) {
        this(id, name, marks, waypoints, associatedRoles, optionalImageURL, /* optionalRepeatablePart */ null);
    }
    
    public CourseTemplateImpl(UUID id, String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, String> associatedRoles, URL optionalImageURL, RepeatablePart optionalRepeatablePart) {
        super(name, id);
        if (optionalRepeatablePart != null) {
            optionalRepeatablePart.validateRepeatablePartForSequence(waypoints);
        }
        final Set<MarkTemplate> theMarks = new HashSet<>();
        Util.addAll(marks, theMarks);
        this.waypoints = new ArrayList<>();
        Util.addAll(waypoints, this.waypoints);
        this.marks = theMarks;
        this.optionalImageURL = optionalImageURL;
        this.optionalRepeatablePart = optionalRepeatablePart;
        this.associatedRoles = new HashMap<>(associatedRoles);
        validateWaypointsAgainstMarks();
    }

    /**
     * Throws an {@link IllegalArgumentException} in case a waypoint from {@link #waypoints} uses a mark template that
     * is not in {@link #marks}.
     */
    private void validateWaypointsAgainstMarks() {
        for (final WaypointTemplate waypoint : waypoints) {
            for (final MarkTemplate mark : waypoint.getControlPointTemplate().getMarks()) {
                if (!Util.contains(marks, mark)) {
                    throw new IllegalArgumentException("Mark "+mark+" used by waypoint template "+
                            waypoint+" in course template "+this+" is not provided in the collection of marks");
                }
            }
        }
    }

    @Override
    public Iterable<MarkTemplate> getMarkTemplates() {
        return marks;
    }

    // TODO move to CourseTemplateConfigurations
    @Override
    public Iterable<WaypointTemplate> getWaypointTemplates(int numberOfLaps) {
        final Iterable<WaypointTemplate> result;
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
    public RepeatablePart getRepeatablePart() {
        return optionalRepeatablePart;
    }
    

    @Override
    public Map<MarkTemplate, String> getAssociatedRoles() {
        return associatedRoles;
    }

    public void setAssociatedRoles(Map<MarkTemplate, String> associatedRoles) {
        this.associatedRoles.clear();
        this.associatedRoles.putAll(associatedRoles);
    }
    
    @Override
    public URL getOptionalImageURL() {
        return optionalImageURL;
    }

    @Override
    public Iterable<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
