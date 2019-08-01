package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Mark;

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
 * The configuration for all marks are returned by {@link #getMarkConfigurations()} because when pushing
 * this to a Regatta the marks for all these configurations have to be established.
 */
public interface CourseWithMarkConfiguration extends WithOptionalRepeatablePart {
    /**
     * If this course configuration has been obtained from a {@link CourseTemplate} without modifying the sequence of
     * waypoints and their mapping to {@link MarkTemplate}s, this method will return that {@link CourseTemplate}. In all
     * other cases it will return {@code null}. If not {@code null}, the methods of {@link WithOptionalRepeatablePart}
     * delegate to the {@link CourseTemplate} returned by this method.
     */
    CourseTemplate getOptionalCourseTemplate();
    
    Iterable<MarkConfiguration> getMarkConfigurations();

    /**
     * The waypoint sequence, without lap repetitions, with {@link MarkConfiguration configuration information}
     * for the marks used. All {@link MarkConfiguration} objects referenced are part of the result of
     * {@link #getMarkConfigurations()}.
     */
    Iterable<WaypointWithMarkConfiguration> getWaypoints();

    /**
     * The waypoint sequence with lap count applied. If this course configuration has no {@link #hasRepeatablePart()
     * repeatable part}, the result is the same as that of {@link #getWaypoints()}. The result has
     * {@link MarkConfiguration configuration information} for the marks used. All {@link MarkConfiguration} objects
     * referenced are part of the result of {@link #getMarkConfigurations()}.
     */
    Iterable<WaypointWithMarkConfiguration> getWaypoints(int numberOfLaps);
}
