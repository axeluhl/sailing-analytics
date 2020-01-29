package com.sap.sailing.domain.coursetemplate;

import java.util.Iterator;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sse.common.Util;

/**
 * Checks compatibility of a course with a {@link CourseTemplate}. As a generic type, subclasses
 * can specify what to use as a course, simply by defining how to get to the waypoint sequence of
 * the course and from the waypoints to the marks and from the marks to the {@link MarkRole}s.
 * This way, the core method {@link #isCourseInstanceOfCourseTemplate()} becomes re-usable across
 * types as different as {@link CourseBase} or {@link CourseConfiguration}.
 * 
 * @param <C>
 *            the type of the actual course to check for compatibility with a {@link CourseTemplate}
 * @param <M>
 *            type type of the marks used by the course of type {@code C}
 * @param <W>
 *            type of the waypoints produced by the course of type {@code C}
 */
public abstract class CourseTemplateCompatibilityChecker<C, M, W> {
    private final C course;
    private final CourseTemplate courseTemplate;
    
    public CourseTemplateCompatibilityChecker(C course, CourseTemplate courseTemplate) {
        super();
        assert course != null;
        assert courseTemplate != null;
        this.course = course;
        this.courseTemplate = courseTemplate;
    }

    /**
     * Takes a {@code course} and a {@link CourseTemplate} and checks whether the course is a valid "instance" of the
     * {@link CourseTemplate}. A course is a valid instance of a course template if for all marks in the course (as
     * defined by the {@link #getMarks(Object)} method) there is a role associated with the mark (as defined by the
     * {@link #getMarkRole(Object)} method), and that role is consistent with the role at the corresponding place in the
     * course template. Furthermore, the waypoint sequence of the course (as defined by the
     * {@link #getWaypoints(Object)} method) needs to conform to the course template's waypoint sequence, modulo the
     * number of laps. The result is the number of laps if the {@link CourseTemplate} has a
     * {@link CourseTemplate#getRepeatablePart() repeatable part}, or {@code -1} if the course does not have a
     * repeatable part, and {@code null} if the course is not a valid instance of the course template.
     * <p>
     * 
     * This means in particular that the course can use different marks for the same role in different laps and still
     * conform to the course template.
     * <p>
     * 
     * @return the number of laps if the {@link CourseTemplate} has a {@link CourseTemplate#getRepeatablePart()
     *         repeatable part}, or {@code -1} if the course does not have a repeatable part, and {@code null} if the
     *         course is not a valid instance of the course template
     */
    public Integer isCourseInstanceOfCourseTemplate() {
        int numberOfLaps = -1;
        boolean validCourseTemplateUsage = true;
        final Iterable<WaypointTemplate> effectiveCourseSequence;
        if (courseTemplate.hasRepeatablePart()) {
            final RepeatablePart optionalRepeatablePart = courseTemplate.getRepeatablePart();
            final int numberOfWaypointsInTemplate = Util.size(courseTemplate.getWaypointTemplates());
            final int numberOfWaypointsInCourse = Util.size(getWaypoints(course));
            final int lengthOfRepeatablePart = optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartEnd()
                    - optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartStart();
            final int lengthOfNonRepeatablePart = numberOfWaypointsInTemplate - lengthOfRepeatablePart;
            final int lengthOfRepetitions = numberOfWaypointsInCourse - lengthOfNonRepeatablePart;
            if (lengthOfRepetitions % lengthOfRepeatablePart == 0) {
                numberOfLaps = lengthOfRepetitions / lengthOfRepeatablePart + 1;
                effectiveCourseSequence = courseTemplate.getWaypointTemplates(numberOfLaps);
            } else {
                validCourseTemplateUsage = false;
                effectiveCourseSequence = courseTemplate.getWaypointTemplates();
            }
        } else {
            effectiveCourseSequence = courseTemplate.getWaypointTemplates();
        }
        if (validCourseTemplateUsage) {
            final Iterator<WaypointTemplate> waypointTemplateIterator = effectiveCourseSequence.iterator();
            final Iterator<W> waypointIterator = getWaypoints(course).iterator();
            while (waypointTemplateIterator.hasNext() && validCourseTemplateUsage) {
                final WaypointTemplate waypointTemplate = waypointTemplateIterator.next();
                final Iterable<MarkRole> markRolesOfControlPoint = waypointTemplate.getControlPointTemplate().getMarkRoles();
                final W waypoint = waypointIterator.next();
                final Iterable<M> marksOfControlPoint = getMarks(waypoint);
                if (Util.size(markRolesOfControlPoint) != Util.size(marksOfControlPoint)) {
                    validCourseTemplateUsage = false;
                } else {
                    final Iterator<MarkRole> markRoleIterator = markRolesOfControlPoint.iterator();
                    final Iterator<M> markIterator = marksOfControlPoint.iterator();
                    while (markRoleIterator.hasNext()) {
                        final MarkRole markRoleFromCourseTemplate = markRoleIterator.next();
                        final M markFromRegatta = markIterator.next();
                        final MarkRole roleForMarkOrNull = getMarkRole(markFromRegatta);
                        if (!Util.equalsWithNull(markRoleFromCourseTemplate, roleForMarkOrNull)) {
                            validCourseTemplateUsage = false;
                        }
                    }
                }
            }
        }
        return validCourseTemplateUsage ? numberOfLaps : null;
    }
    
    protected C getCourse() {
        return course;
    }

    protected CourseTemplate getCourseTemplate() {
        return courseTemplate;
    }

    protected abstract MarkRole getMarkRole(M markFromCourse);

    protected abstract Iterable<M> getMarks(W waypoint);

    protected abstract Iterable<W> getWaypoints(C course);
}
