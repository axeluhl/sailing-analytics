package com.sap.sailing.domain.coursetemplate;

import java.net.URL;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Mark;
import com.sap.sse.common.Named;

/**
 * A course that does not necessarily consist of {@link Mark}s existing in a regatta but instead is defined by
 * {@link MarkConfiguration}s. This means changes to the model are easily possible without requiring to pollute the
 * {@link RegattaLog}. The effective Marks will then be created upon Course creation based on the
 * {@link MarkConfiguration}s.
 * <p>
 * 
 * This is the model represented in a course editor which deals with editing the waypoint sequence as well as changing
 * how marks are mapped to {@link MarkTemplate}s and how marks are generally configured in terms of their base
 * properties and tracking/positioning information. This configuration can reference a non-{@code null}
 * {@link CourseTemplate} (returned by {@link #getOptionalCourseTemplate()}). Being an "editor model" this means that
 * the actual {@link #getWaypoints() waypoint sequence} may be inconsistent with what the course template demands. When
 * requesting a course update, the waypoint sequence takes precedence over a course template. Likewise, the
 * {@link #getNumberOfLaps() number of laps} in a request to create a course may be inconsistent with the waypoint
 * sequence which then should be interpreted such that the request processor shall adjust the waypoint sequence such
 * that the number of laps requested is reflected in the waypoint sequence consistently.
 * <p>
 * 
 * When used in a request to create a course template, the {@link #getRepeatablePart()} will be used as the
 * new course template's {@link CourseTemplate#getRepeatablePart() repeatable part}.<p>
 * 
 * In particular, the base {@link #getWaypoints() waypoints sequence}, when returned by a request, has at least one
 * occurrence of a repeatable part, should the configuration specify a course template, be consistent with it, and
 * should the course template specify a repeatable part. As a response, this configuration will tell the
 * {@link #getNumberOfLaps()} found or created during the request.
 * <p>
 * 
 * The configuration for all marks are returned by {@link #getAllMarks()} because when pushing this to a Regatta the
 * marks for all these configurations have to be established.
 *
 * @param <P>
 *            the type used to {@link MarkConfiguration#getAnnotationInfo() annotate} the {@link MarkProperties} used in
 *            this course configuration. Different annotations are expected to be used for course configurations used in
 *            requests sent to a service and for responses produced by a service.
 */
public interface CourseConfiguration<P> extends WithOptionalRepeatablePart, Named {
    String getShortName();
    
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
     * The waypoint sequence. If a repeatable part is specified, the sequence returned must contain at least one
     * occurrence of the repeatable part. All {@link MarkConfiguration} objects referenced are part of the result of
     * {@link #getAllMarks()}. If you want the waypoint sequence including as many occurrences
     * of any repeatable part, call {@link #getWaypoints(int) getWaypoints(}{@link #getNumberOfLaps() getNumberOfLaps())}.
     */
    Iterable<WaypointWithMarkConfiguration<P>> getWaypoints();
    
    /**
     * Returns all {@link MarkConfiguration MarkConfigurations} that are part of the {@link CourseConfigurationBase} and
     * have a role explicitly associated. A mark role links back to a {@link CourseTemplate} and makes it possible to
     * recognize congruence between the course described by this configuration and the course described by a course
     * template, regardless of the actual marks that are used in certain roles. This enables, for example, to use two
     * different marks for the same role in different laps and still find the course configuration to be congruent to
     * its original course template. This information is also useful when creating another course template from this
     * configuration ("save as...") because then the roles to which the mark configurations refer can be (re-)used in
     * the new course template constructed from this configuration.
     */
    Map<MarkConfiguration<P>, MarkRole> getAssociatedRoles();
    
    /**
     * Returns a map whose key set equals the result of {@link #getAllMarks()} and whose values are the result
     * of mapping the key through the result of {@link #getAssociatedRoles()}. If a mark configuration does not
     * have a role assigned, {@code null} will be the associated value in the map returned by this method.
     */
    Map<MarkConfiguration<P>, MarkRole> getAllMarksWithOptionalRoles();
    
    /**
     * A {@link CourseConfigurationBase} having a {@link #getOptionalCourseTemplate() course template} can optionally
     * also tell the number of laps in case this configuration is used to create or update a course. In a request this
     * number can be used to override the {@link CourseTemplate#getDefaultNumberOfLaps() default number of laps} that
     * the course template would use. Depending on the use-case this may be ignored (saving a course template) or is
     * required (creating a regatta course from a configuration with a course template that specifies a repeatable
     * part).
     * <p>
     * 
     * In a response the number of laps is reported as {@code null} if a non-{@code null}
     * {@link #getOptionalCourseTemplate() course template} is provided but the course is not compatible with that
     * template; {@code -1} if there is no course template specified, or the course template has no
     * {@link CourseTemplate#getRepeatablePart() repeatable part}; otherwise the number of laps (number of occurrences
     * of the repeatable part plus one).
     */
    Integer getNumberOfLaps();

    /**
     * The waypoint sequence with lap count applied. If this course configuration has no {@link #hasRepeatablePart()
     * repeatable part}, the result is the same as that of {@link #getWaypoints()}. The result has
     * {@link MarkConfiguration configuration information} for the marks used. All {@link MarkConfiguration} objects
     * referenced are part of the result of {@link #getAllMarks()}.
     * 
     * @param numberOfLaps
     *            the repeatable part will be inserted {@code numberOfLaps-1} times. For example, a two-lap course will
     *            have the repeatable part exactly once. Use {@link #getNumberOfLaps()} to
     *            obtain the waypoint sequence as specified by this course configuration.
     */
    Iterable<WaypointWithMarkConfiguration<P>> getWaypoints(int numberOfLaps);
}
