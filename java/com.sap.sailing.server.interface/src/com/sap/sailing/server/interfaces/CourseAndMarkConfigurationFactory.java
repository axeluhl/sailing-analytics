package com.sap.sailing.server.interfaces;

import java.util.List;
import java.util.Optional;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEvent;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WithOptionalRepeatablePart;
import com.sap.sse.common.TimePoint;
import com.sap.sse.security.shared.impl.UserGroup;

/**
 * {@link CourseConfiguration} is meant as an in memory model for all kinds of course definitions. This model is not
 * stored to the DB but can be constructed from and stored to other representations of courses. Currently the
 * interaction with the following domain types is possible via {@link CourseConfiguration}s:
 * <ul>
 * <li>{@link CourseTemplate}</li>
 * <li>{@link CourseBase}</li>
 * </ul>
 * 
 * {@link CourseAndMarkConfigurationFactory} provides the functionality to convert domain types to
 * {@link CourseConfiguration}s as well as creating or updating those types from {@link CourseConfiguration}s.
 * <br>
 * For details on how the {@link CourseConfiguration} model based on {@link Course}s as well as {@link CourseTemplate}s
 * work, please consult the documentation found in /wiki/coursecreation/course-templates-and-configuration.md.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseAndMarkConfigurationFactory {

    /**
     * {@link CourseBase Courses} can optionally reference a {@link CourseTemplate} they were created from. This method
     * can be used to obtain the associated {@link CourseTemplate} for a given {@link Course}. Due to the fact that
     * {@link CourseTemplate} can be deleted without updating the {@link CourseBase}, references may be stale. In this
     * case, the reference is handled equally to {@link CourseBase Courses} without a reference.
     * 
     * @return the {@link CourseTemplate} associated to the given {@link CourseBase} or {@code null} if none is
     *         associated or a reference could not be resolved.
     */
    CourseTemplate resolveCourseTemplate(CourseBase course);
    
    /**
     * The {@link CourseConfiguration} contains everything required to produce a new {@link CourseTemplate} from it: the
     * {@link MarkTemplate}s can be obtained by mapping all {@link CourseConfiguration#getAllMarks() mark
     * configurations} through {@link #getOrCreateMarkTemplate(MarkConfiguration)}. The
     * {@link WithOptionalRepeatablePart repeatable part specification} is obtained immediately from the
     * {@link CourseConfiguration}. For each {@link CourseConfiguration#getWaypoints() waypoint configuration} of the
     * {@code courseWithMarkConfiguration}, a {@link WaypointTemplate} is created where the {@link MarkTemplate}s
     * referenced by these {@link WaypointTemplates} are those obtained by
     * {@link #getOrCreateMarkTemplate(MarkConfiguration)} for the respective {@link MarkConfiguration} objects used in
     * the {@link WaypointWithMarkConfiguration} objects.
     * @param optionalNonDefaultGroupOwnership TODO
     * 
     * @return a course with its mark configurations and a {@link CourseTemplate} returned by
     *         {@link CourseConfiguration#getOptionalCourseTemplate()} that is always valid, never {@code null}. All
     *         {@link MarkConfiguration} objects in the result all have a valid
     *         {@link MarkConfiguration#getOptionalMarkTemplate() mark template} that is part of the
     *         {@link CourseTemplate} obtained from {@link CourseConfiguration#getOptionalCourseTemplate()}.
     */
    CourseConfiguration createCourseTemplateAndUpdatedConfiguration(CourseConfiguration courseWithMarkConfiguration,
            Iterable<String> tags, Optional<UserGroup> optionalNonDefaultGroupOwnership);

    /**
     * Creates a {@link CourseConfiguration} from a {@link CourseTemplate}. The resulting waypoint sequence is
     * consistent with the one defined in the {@link CourseTemplate}. In case no {@link Regatta} is given, the resulting
     * {@link MarkConfiguration}s will 1:1 match the {@link MarkTemplate}s of the {@link CourseTemplate}. In case a
     * {@link Regatta} is given, the {@link MarkTemplate}s are mapped to contained {@link Mark}s if possible. Any
     * {@link MarkTemplate} not mapped to a {@link Mark} (in case, no {@link Regatta} is given, these are just all
     * {@link MarkTemplate}s), is a candidate to be mapped to a {@link MarkProperties} of the user's inventory. Matching
     * of {@link MarkProperties} is primarily done based on associated roles for which a {@link MarkProperties} was last
     * used (see {@link MarkProperties#getLastUsedRole()}. If no matching of {@link MarkProperties} using roles is
     * possible, a direct association is done if a {@link MarkProperties} was explicitly mapped to a specific
     * {@link MarkTemplate} before.
     * <p>
     * 
     * The {@link CourseConfiguration#getNumberOfLaps() number of laps} matches the
     * {@link CourseTemplate#getDefaultNumberOfLaps() template's default number of laps} which is relevant only if the
     * {@link CourseTemplate} specifies a {@link CourseTemplate#getRepeatablePart() repeatable part}.
     * 
     * @param optionalRegatta
     *            If given, {@link MarkTemplate}s of the given {@link CourseTemplate} are automatically mapped to their
     *            {@link Mark} counterpart of the {@link Regatta}.
     * @param tagsToFilterMarkProperties
     *            If given, any {@link MarkProperties} that is suggested to replace a {@link MarkTemplate} of the given
     *            {@link CourseTemplate} needs to match all given tags.
     */
    CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate, Regatta optionalRegatta,
            Iterable<String> tagsToFilterMarkProperties);

    /**
     * Creates a {@link CourseConfiguration} for a Regatta - either based on a {@link CourseBase} or independently. The
     * resulting waypoint sequence is consistent with the one defined in the {@link CourseBase}, if one is given. In
     * case, no {@link CourseBase} is given, no waypoint sequence is constructed but existing {@link Mark}s of the
     * {@link Regatta} are loaded as {@link MarkConfiguration}s.<br>
     * 
     * If a given {@link CourseBase} is based on a {@link CourseTemplate}
     * ({@link CourseBase#getOriginatingCourseTemplateIdOrNull() references an existing and visible CourseTemplate} and
     * the waypoint sequences are compatible, the sequence is mapped back to the {@link CourseTemplate} instead of just
     * loading the {@link CourseBase}. This means, if a {@link CourseBase} was created for a {@link CourseTemplate}
     * having a {@link RepeatablePart}, the lap count is reconstructed and the shortened waypoint sequence is used.<br>
     * 
     * If no {@link CourseTemplate} can be obtained or the waypoint sequences aren't compatible, the
     * {@link CourseTemplate} is just ignored. No lap count and {@link RepeatablePart} is recognized and the whole
     * waypoint sequence is just 1:1 mapped to the {@link CourseConfiguration}.
     * 
     * @param optionalCourse
     *            if given, the course configuration is based on the real course sequence. If not given, only available
     *            {@link Mark} are included but the course sequence is empty.
     * @param tagsToFilterMarkProperties
     *            If given, any {@link MarkProperties} that is suggested to replace a {@link MarkTemplate} of the given
     *            {@link CourseTemplate} needs to match all given tags.
     */
    CourseConfiguration createCourseConfigurationFromRegatta(CourseBase optionalCourse, Regatta regatta,
            Iterable<String> tagsToFilterMarkProperties);

    /**
     * TODO: not implemented yet
     */
    List<MarkProperties> createMarkPropertiesSuggestionsForMarkConfiguration(Regatta optionalRegatta,
            MarkConfiguration markConfiguration, Iterable<String> tagsToFilterMarkProperties);

    /**
     * Use the result to create a {@link RaceLogCourseDesignChangedEvent} or obtain the {@link ControlPoint} and
     * {@link PassingInstruction} pairs to {@code Course.update(...)} a course.
     * 
     * @param regatta
     *            the regatta whose {@link RegattaLog} to use to define the new {@link Mark}s in.
     * @param author
     *            for {@link RegattaLogDefineMarkEvent}s and the {@link RegattaLogDeviceMappingEvent}s.
     * @param optionalNonDefaultGroupOwnership TODO
     * @return the sequence of waypoints, obtained by expanding the
     */
    CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            CourseConfiguration courseConfiguration, TimePoint timePointForDefinitionOfMarksAndDeviceMappings,
            AbstractLogEventAuthor author, Optional<UserGroup> optionalNonDefaultGroupOwnership);
}
