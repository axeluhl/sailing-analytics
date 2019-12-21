package com.sap.sailing.domain.coursetemplate;

import java.net.URL;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.Named;

/**
 * A course that does not consist of {@link Mark}s existing in a regatta but instead is defined by
 * {@link MarkConfiguration}s. This means changes to the model are easily possible without requiring to pollute the
 * {@link RegattaLog}. The effective Marks will then be created upon Course creation based on the
 * {@link MarkConfiguration}s.
 * <p>
 * 
 * This is the model represented in a course editor which deals with editing the waypoint sequence as well as changing
 * how marks are mapped to {@link MarkTemplate}s and how marks are generally configured in terms of their base
 * properties and tracking/positioning information. Any change that is incompatible with a non-{@code null}
 * {@link CourseTemplate} returned by {@link #getOptionalCourseTemplate()} leads to breaking this connection
 * such that {@link #getOptionalCourseTemplate()} from that point on will return {@code null}.<p>
 * 
 * The configuration for all marks are returned by {@link #getAllMarks()} because when pushing
 * this to a Regatta the marks for all these configurations have to be established.
 */
public interface CourseConfigurationBase<R extends IsMarkRole, P> extends WithOptionalRepeatablePart, Named {
    /**
     * If this course configuration has been obtained from a {@link CourseTemplate} without modifying the sequence of
     * waypoints and their mapping to {@link MarkTemplate}s, this method will return that {@link CourseTemplate}. In all
     * other cases it will return {@code null}. If not {@code null}, the methods of {@link WithOptionalRepeatablePart}
     * delegate to the {@link CourseTemplate} returned by this method.
     */
    CourseTemplate getOptionalCourseTemplate();
    
    URL getOptionalImageURL();
    
    /**
     * Returns all marks that are part of this course configuration. This consists of:
     * <ul>
     * <li>All {@link MarkConfiguration} that are used by at least one {@link WaypointWithMarkConfiguration}.</li>
     * <li>Any additional {@link MarkConfiguration} that is intended to be used as a spare mark.</li>
     * </ul>
     */
    Iterable<MarkConfiguration<P>> getAllMarks();

    /**
     * The waypoint sequence, with a single occurrence of any repeatable part the course may have (similar to calling
     * {@link #getWaypoints(int) getWaypoints(1)}), with {@link MarkConfiguration configuration information} for the
     * marks used. All {@link MarkConfiguration} objects referenced are part of the result of {@link #getAllMarks()}.
     */
    Iterable<WaypointWithMarkConfiguration<P>> getWaypoints();
    
    /**
     * Returns all {@link MarkConfiguration MarkConfigurations} that are part of the {@link CourseConfigurationBase} and
     * have a role name explicitly associated.
     */
    Map<MarkConfiguration<P>, R> getAssociatedRoles();
    
    Map<MarkConfiguration<P>, R> getAllMarksWithOptionalRoles();
    
    /**
     * A {@link CourseConfigurationBase} having a {@link RepeatablePart} can optionally also specify a number of laps.
     * Depending on the use-case this can be omitted (saving a course template) or is required (creating a regatta
     * course).
     */
    Integer getNumberOfLaps();

    /**
     * The waypoint sequence with lap count applied. If this course configuration has no {@link #hasRepeatablePart()
     * repeatable part}, the result is the same as that of {@link #getWaypoints()}. The result has
     * {@link MarkConfiguration configuration information} for the marks used. All {@link MarkConfiguration} objects
     * referenced are part of the result of {@link #getAllMarks()}.
     * 
     * @param numberOfLaps the repeatable part will be inserted {@code numberOfLaps-1} times. For example, a two-lap
     * course will have the repeatable part exactly once.
     */
    Iterable<WaypointWithMarkConfiguration<P>> getWaypoints(int numberOfLaps);
}
