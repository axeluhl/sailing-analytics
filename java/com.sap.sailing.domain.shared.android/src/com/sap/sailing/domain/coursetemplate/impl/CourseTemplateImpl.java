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

    private final String shortName;
    
    private final Set<MarkTemplate> marks;
    
    private final ArrayList<WaypointTemplate> waypoints;
    
    private final Map<MarkTemplate, MarkRole> defaultRolesForMarkTemplates;

    private final Map<MarkRole, MarkTemplate> defaultMarkTemplatesForRoles;

    private Iterable<String> tags = new ArrayList<>();
    
    private final URL optionalImageURL;

    private final RepeatablePart optionalRepeatablePart;

    private final Integer defaultNumberOfLaps;

    
    /**
     * Creates a course template with a random UUID and no repeatable part.
     * 
     * @param marks
     *            all mark templates made available in this course template
     * @param waypoints
     *            may refer only to {@link MarkTemplate}s provided in the {@code marks} parameter
     * @param defaultRolesForMarkTemplates
     *            for those mark templates referenced as values in {@code defaultMarkTemplatesForRoles}, the value in
     *            this map has to be the key in {@code defaultMarkTemplatesForRoles}; for all other
     *            {@link MarkTemplates}s from {@code marks} it is optional whether a mapping to a default
     *            {@link MarkRole} is provided in this map
     * @param defaultMarkTemplatesForRoles
     *            all {@link MarkRole}s reachable through {@code waypoints} must appear as keys in this map's key set;
     *            the values represent the {@link MarkTemplate} to use when instantiating this course template for the
     *            key role
     */
    public CourseTemplateImpl(String name, String shortName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> defaultRolesForMarkTemplates, Map<MarkRole, MarkTemplate> defaultMarkTemplatesForRoles, URL optionalImageURL) {
        this(UUID.randomUUID(), name, shortName, marks, waypoints, defaultRolesForMarkTemplates,
                defaultMarkTemplatesForRoles, optionalImageURL);
    }

    /**
     * Creates a course with the given UUID and no repeatable part.
     * @param shortName TODO
     */
    public CourseTemplateImpl(UUID id, String name, String shortName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> defaultRolesForMarkTemplates, Map<MarkRole, MarkTemplate> defaultMarkTemplatesForRoles, URL optionalImageURL) {
        this(id, name, shortName, marks, waypoints, defaultRolesForMarkTemplates, defaultMarkTemplatesForRoles,
                optionalImageURL, /* optionalRepeatablePart */ null, /* default number of laps */ null);
    }
    
    public CourseTemplateImpl(UUID id, String name, String shortName, Iterable<MarkTemplate> marks,
            Iterable<WaypointTemplate> waypoints,
            Map<MarkTemplate, MarkRole> defaultRolesForMarkTemplates, Map<MarkRole, MarkTemplate> defaultMarkTemplatesForRoles,
            URL optionalImageURL, RepeatablePart optionalRepeatablePart, Integer defaultNumberOfLaps) {
        super(name, id);
        this.shortName = shortName;
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
        this.defaultRolesForMarkTemplates = new HashMap<>(defaultRolesForMarkTemplates);
        this.defaultMarkTemplatesForRoles = new HashMap<>(defaultMarkTemplatesForRoles);
        validateWaypointsAgainstRolesAndMappingSymmetry();
    }

    /**
     * Throws an {@link IllegalArgumentException} in case a waypoint from {@link #waypoints} uses a mark role that uses
     * a default mark template that is not in {@link #marks}; or if a mark role used in the waypoint sequence does not
     * define a default mark template; or if the mark template used as the default for a mark role does not refer back
     * to that same mark role as its default.
     */
    private void validateWaypointsAgainstRolesAndMappingSymmetry() {
        for (final WaypointTemplate waypoint : waypoints) {
            for (final MarkRole markRole : waypoint.getControlPointTemplate().getMarkRoles()) {
                if (!defaultMarkTemplatesForRoles.containsKey(markRole)) {
                    throw new IllegalArgumentException("Mark role "+markRole+" used by waypoint template "+
                            waypoint+" in course template "+this+" is not providing a default mark template");
                }
                if (!Util.contains(marks, defaultMarkTemplatesForRoles.get(markRole))) {
                    throw new IllegalArgumentException("Mark template " + defaultMarkTemplatesForRoles.get(markRole)
                            + " used by role " + markRole + " used by waypoint template " + waypoint
                            + " in course template " + this + " is not provided in the collection of marks");
                }
                if (!Util.equalsWithNull(defaultRolesForMarkTemplates.get(defaultMarkTemplatesForRoles.get(markRole)), markRole)) {
                    throw new IllegalArgumentException("Mark template " + defaultMarkTemplatesForRoles.get(markRole)
                            + " used as default for mark role " + markRole
                            + " does not use that same mark role as its default but instead refers to "
                            + defaultRolesForMarkTemplates.get(defaultMarkTemplatesForRoles.get(markRole))
                            + " as its default role");
                }
            }
        }
    }
    
    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public MarkTemplate getDefaultMarkTemplateForRole(MarkRole markRole) {
        return defaultMarkTemplatesForRoles.get(markRole);
    }

    @Override
    public Map<MarkRole, MarkTemplate> getDefaultMarkTemplatesForMarkRoles() {
        return defaultMarkTemplatesForRoles;
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
    public Map<MarkTemplate, MarkRole> getDefaultMarkRolesForMarkTemplates() {
        return defaultRolesForMarkTemplates;
    }

    @Override
    public MarkRole getOptionalAssociatedRole(MarkTemplate markTemplate) {
        return defaultRolesForMarkTemplates.get(markTemplate);
    }

    @Override
    public MarkRole getMarkRoleByIdIfContainedInCourseTemplate(UUID markRoleId) {
        MarkRole result = null;
        for (MarkRole markRole : defaultRolesForMarkTemplates.values()) {
            if (markRole.getId().equals(markRoleId)) {
                result = markRole;
                break;
            }
        }
        return result;
    }

    public void setAssociatedRoles(Map<MarkTemplate, MarkRole> associatedRoles) {
        this.defaultRolesForMarkTemplates.clear();
        this.defaultRolesForMarkTemplates.putAll(associatedRoles);
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
}
