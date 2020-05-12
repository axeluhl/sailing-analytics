package com.sap.sailing.domain.coursetemplate.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplateCompatibilityChecker;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class CourseConfigurationImpl<P> implements CourseConfiguration<P> {
    private static final long serialVersionUID = -9189989170055144298L;

    private final String shortName;
    private final CourseTemplate optionalCourseTemplate;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private final Iterable<MarkConfiguration<P>> markConfigurations;
    private final Map<MarkConfiguration<P>, MarkRole> associatedRoles;
    
    /**
     * The waypoints sequence, with at least a single occurrence of any {@link #getRepeatablePart() repeatable part},
     * in order to obtain a sequence for more than only one lap (meaning zero occurrences of the repeatable part).
     * There may be more occurrences of the repeatable part than only one, in particular to allow subsequent repetitions
     * to use different mark configurations in their lap. See also {@link #getWaypoints()} and
     * {@link #getWaypoints(int)}. So, if {@link #numberOfLaps} is less than or equal to {@code 1}, an occurrence
     * of the repeatable part will be in this sequence although it isn't used in the current configuration.
     * This is, however, required in order to interpret the index-based repeatable part in case one or more
     * occurrences of the repeatable part will be requested.<p>
     * 
     * This sequence may contain more occurrences of the repeatable part than requested through {@link #numberOfLaps}
     * even if one or more occurrences of the repeatable part are requested this way. These extra occurrences
     * will then be used if {@link #getWaypoints(int)} is called with more laps than {@link #numberOfLaps}. This way,
     * a course configuration can prepare for more laps being sailed and prepare a configuration for extra laps
     * that differ from the configurations used for the laps currently selected.
     */
    private final List<WaypointWithMarkConfiguration<P>> waypoints;
    
    /**
     * Refers to a repeatable part in {@link #waypoints}. Other than in a {@link CourseTemplate} that always
     * has exactly one occurrence of a repeatable part in its waypoint template sequence, here the {@link #waypoints}
     * sequence must have at least one but may have multiple occurrences of the repeatable part. The special handling
     * that happens in this class is that when more than one occurrence already exists in {@link #waypoints}, upon
     * adding more repetitions the <em>last</em> occurrence will be repeated. See also {@link #getWaypoints(int)}.
     */
    private final RepeatablePart optionalRepeatablePart;
    
    /**
     * The number of laps this course configuration has, in case there is a {@link #getRepeatablePart() repeatable part}
     * defined for a valid {@link #optionalCourseTemplate}. In this case, {@link #getWaypoints()} will consider this
     * number of laps and will return 
     */
    private final Integer numberOfLaps;
    
    private final String name;
    private final URL optionalImageURL;
    
    /**
     * @param numberOfLaps
     *            {@code null} if a non-{@code null} {@code optionalCourseTemplate} is provided but the course is not
     *            compatible with that template; {@code -1} if there is no course template specified, or the course
     *            template has a {@code null} {@link CourseTemplate#getRepeatablePart() repeatable part}; otherwise the
     *            number of laps (number of occurrences of the repeatable part plus one).
     */
    public CourseConfigurationImpl(CourseTemplate optionalCourseTemplate,
            Iterable<MarkConfiguration<P>> markConfigurations, Map<MarkConfiguration<P>, MarkRole> associatedRoles,
            List<WaypointWithMarkConfiguration<P>> waypoints,
            RepeatablePart optionalRepeatablePart, Integer numberOfLaps, String name, String shortName, URL optionalImageURL) {
        super();
        assert !associatedRoles.values().contains(null);
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.markConfigurations = markConfigurations;
        this.associatedRoles = associatedRoles;
        this.waypoints = waypoints;
        if (optionalRepeatablePart != null && optionalCourseTemplate == null) {
            throw new IllegalArgumentException("Inconsistent course configuration "+name+": When a repeatable part is defined, a course template must be provided");
        }
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
    
    private class CourseTemplateCompatibilityCheckerForCourseConfiguration extends
    CourseTemplateCompatibilityChecker<CourseConfiguration<P>, MarkConfiguration<P>, WaypointWithMarkConfiguration<P>> {
        public CourseTemplateCompatibilityCheckerForCourseConfiguration() {
            super(CourseConfigurationImpl.this, optionalCourseTemplate);
        }

        @Override
        protected MarkRole getMarkRole(MarkConfiguration<P> markFromCourse) {
            return getAssociatedRoles().get(markFromCourse);
        }

        @Override
        protected Iterable<MarkConfiguration<P>> getMarks(WaypointWithMarkConfiguration<P> waypoint) {
            return waypoint.getControlPoint().getMarkConfigurations();
        }

        @Override
        protected Iterable<WaypointWithMarkConfiguration<P>> getWaypoints(CourseConfiguration<P> course) {
            return waypoints;
        }
    }
    
    /**
     * @return the number of repeatable part occurrences if the {@link CourseTemplate} has a {@link CourseTemplate#getRepeatablePart()
     *         repeatable part}, or {@code -1} if the course does not have a repeatable part, and {@code null} if the
     *         course is not a valid instance of the course template
     */
    private Integer getNumberOfRepeatablePartOccurrencesInWaypoints() {
        final Integer numberOfLaps = new CourseTemplateCompatibilityCheckerForCourseConfiguration().isCourseInstanceOfCourseTemplate();
        return numberOfLaps == null ? null : numberOfLaps == -1 ? -1 : numberOfLaps-1;
    }

    @Override
    public Iterable<WaypointWithMarkConfiguration<P>> getWaypoints(int numberOfLaps) {
        final Iterable<WaypointWithMarkConfiguration<P>> result;
        if (hasRepeatablePart()) {
            if (numberOfLaps < 1) {
                throw new IllegalArgumentException("The course template "+this+" has a repeatable part, hence the number of laps needs to be at least 1.");
            }
            final Integer numberOfRepeatablePartOccurrences = getNumberOfRepeatablePartOccurrencesInWaypoints();
            assert numberOfRepeatablePartOccurrences != null; // expected to be enforced by a check in the constructor
            final List<WaypointWithMarkConfiguration<P>> waypointsWithCorrectNumberOfLaps = new LinkedList<>();
            // Copy the prefix of the repeatable part without modifications:
            for (int i=0; i<getRepeatablePart().getZeroBasedIndexOfRepeatablePartStart(); i++) {
                waypointsWithCorrectNumberOfLaps.add(waypoints.get(i));
            }
            // Copy as many occurrences of repeatable parts as required (numberbOfLaps-1) and keep repeating
            // the last occurrence from waypoints if more are requested than are present there:
            for (int repetitions=0; repetitions<numberOfLaps-1; repetitions++) {
                final int occurrenceInWaypointsToUse = Math.min(repetitions, numberOfRepeatablePartOccurrences-1);
                for (int i=getRepeatablePart().getZeroBasedIndexOfRepeatablePartStart(); i<getRepeatablePart().getZeroBasedIndexOfRepeatablePartEnd(); i++) {
                    waypointsWithCorrectNumberOfLaps.add(waypoints.get(
                            getRepeatablePart().length()*occurrenceInWaypointsToUse + i));
                }
            }
            // Add the postfix from after the last repeatable part occurrence in waypoints:
            for (int i=getRepeatablePart().getZeroBasedIndexOfRepeatablePartStart()+
                    numberOfRepeatablePartOccurrences*getRepeatablePart().length(); i<waypoints.size(); i++) {
                waypointsWithCorrectNumberOfLaps.add(waypoints.get(i));
            }
            assert waypointsWithCorrectNumberOfLaps.size() == waypoints.size() +
                    (numberOfLaps-1-numberOfRepeatablePartOccurrences)*getRepeatablePart().length();
            result = waypointsWithCorrectNumberOfLaps;
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
