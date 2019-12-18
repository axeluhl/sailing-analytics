package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.NamedWithUUIDImpl;

public class CourseTemplateImpl extends NamedWithUUIDImpl implements CourseTemplate {
    private static final long serialVersionUID = -183875832585632806L;

    private final Set<MarkTemplate> marks;
    
    private final ArrayList<WaypointTemplate> waypoints;
    
    private final Map<MarkTemplate, MarkRole> associatedRoles;

    private Iterable<String> tags = new ArrayList<>();
    
    private final URL optionalImageURL;

    private final RepeatablePart optionalRepeatablePart;

    private final Integer defaultNumberOfLaps;

    
    /**
     * Creates a course template with a random UUID.
     * 
     * @param marks
     *            all mark templates made available in this course template
     * @param waypoints
     *            may refer only to {@link MarkTemplate}s provided in the {@code marks} parameter
     * @param associatedRoles
     *            the key set is a sub-set of the {@code marks} parameter and has to contain in its key set exactly
     *            those {@link MarkTemplate}s reachable through the {@code waypoints}.
     */
    public CourseTemplateImpl(String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> associatedRoles, URL optionalImageURL) {
        this(UUID.randomUUID(), name, marks, waypoints, associatedRoles, optionalImageURL);
    }

    public CourseTemplateImpl(UUID id, String name, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints, Map<MarkTemplate, MarkRole> associatedRoles, URL optionalImageURL) {
        this(id, name, marks, waypoints, associatedRoles, optionalImageURL, /* optionalRepeatablePart */ null, null);
    }
    
    public CourseTemplateImpl(UUID id, String name, Iterable<MarkTemplate> marks, Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> associatedRoles, URL optionalImageURL, RepeatablePart optionalRepeatablePart,
            Integer defaultNumberOfLaps) {
        super(name, id);
        this.defaultNumberOfLaps = defaultNumberOfLaps;
        if (optionalRepeatablePart != null) {
            optionalRepeatablePart.validateRepeatablePartForSequence(waypoints);
        }
        this.waypoints = new ArrayList<>();
        Util.addAll(waypoints, this.waypoints);
        this.marks = new HashSet<>();
        Util.addAll(marks, this.marks);
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
    
    @Override
    public MarkTemplate getMarkTemplateByIdIfContainedInCourseTemplate(UUID markTemplateId) {
        MarkTemplate result = null;
        for (MarkTemplate markTemplate : marks) {
            if (markTemplate.getId().equals(markTemplateId)) {
                result = markTemplate;
                break;
            }
        }
        return result;
    }

    @Override
    public Iterable<WaypointTemplate> getWaypointTemplates() {
        return waypoints;
    }

    /**
     * Similar code can be found in {@link CourseConfigurationBaseImpl#getWaypoints(int)}; both methods
     * rely on {@link RepeatablePart#createSequence(int, Iterable)} to do the actual work.
     */
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
    public Map<MarkTemplate, MarkRole> getAssociatedRoles() {
        return associatedRoles;
    }

    @Override
    public Map<MarkTemplate, MarkRole> getMarkTemplatesWithOptionalRoles() {
        final Map<MarkTemplate, MarkRole> result = new HashMap<>();
        for (MarkTemplate mt : marks) {
            result.put(mt, associatedRoles.get(mt));
        }
        return result;
    }

    @Override
    public MarkRole getOptionalAssociatedRole(MarkTemplate markTemplate) {
        return associatedRoles.get(markTemplate);
    }

    @Override
    public MarkRole getMarkRoleByIdIfContainedInCourseTemplate(UUID markRoleId) {
        MarkRole result = null;
        for (MarkRole markRole : associatedRoles.values()) {
            if (markRole.getId().equals(markRoleId)) {
                result = markRole;
                break;
            }
        }
        return result;
    }

    public void setAssociatedRoles(Map<MarkTemplate, MarkRole> associatedRoles) {
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

    public void setTags(Iterable<String> tags) {
        this.tags = tags;
    }

    @Override
    public Integer getDefaultNumberOfLaps() {
        return defaultNumberOfLaps;
    }

    @Override
    public Iterable<MarkTemplate> getMarkTemplatesInWaypoints() {
        final HashSet<MarkTemplate> result = new HashSet<>();
        for (WaypointTemplate waypointTemplate : waypoints) {
            Util.addAll(waypointTemplate.getControlPointTemplate().getMarks(), result);
        }
        return result;
    }
}
