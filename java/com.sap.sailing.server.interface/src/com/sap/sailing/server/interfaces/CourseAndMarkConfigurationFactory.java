package com.sap.sailing.server.interfaces;

import java.net.URL;
import java.util.List;

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
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WithOptionalRepeatablePart;
import com.sap.sse.common.TimePoint;

/**
 * From a {@link CourseTemplate} constructs or updates a {@link CourseBase}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CourseAndMarkConfigurationFactory {

    /**
     * {@link CourseBase Courses} can optionally reference a {@link CourseTemplate} they were created from. This method can
     * be used to obtain the associated {@link CourseTemplate} for a given {@link Course}. Due to the fact that
     * {@link CourseTemplate} can be deleted without updating the {@link CourseBase}, references may be stale. In this case,
     * the reference is handled equally to {@link CourseBase Courses} without a reference.
     * 
     * @return the {@link CourseTemplate} associated to the given {@link CourseBase} or null if none is associated or a
     *         reference could not be resolved.
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
     * @param optionalRegatta TODO
     * 
     * @return a course with its mark configurations and a {@link CourseTemplate} returned by
     *         {@link CourseConfiguration#getOptionalCourseTemplate()} that is always valid, never {@code null}. All
     *         {@link MarkConfiguration} objects in the result all have a valid
     *         {@link MarkConfiguration#getOptionalMarkTemplate() mark template} that is part of the
     *         {@link CourseTemplate} obtained from {@link CourseConfiguration#getOptionalCourseTemplate()}.
     */
    CourseConfiguration createCourseTemplateAndUpdatedConfiguration(CourseConfiguration courseWithMarkConfiguration,
            Iterable<String> tags, URL optionalImageUrl);

    CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate, Regatta optionalRegatta,
            Iterable<String> tagsToFilterMarkProperties);

    CourseConfiguration createCourseConfigurationFromCourse(CourseBase course, Regatta regatta,
            Iterable<String> tagsToFilterMarkProperties);

    List<MarkProperties> createMarkPropertiesSuggestionsForMarkConfiguration(Regatta optionalRegatta,
            MarkConfiguration markConfiguration, Iterable<String> tagsToFilterMarkProperties);

    // TODO Do we need to loosely couple creation of DefineMarkEvents for the Regatta
    /**
     * Use the result to create a {@link RaceLogCourseDesignChangedEvent} or obtain the {@link ControlPoint} and
     * {@link PassingInstruction} pairs to {@code Course.update(...)} a course.
     * 
     * @param regatta
     *            the regatta whose {@link RegattaLog} to use to define the new {@link Mark}s in.
     * @param author
     *            for {@link RegattaLogDefineMarkEvent}s and the {@link RegattaLogDeviceMappingEvent}s.
     * @return the sequence of waypoints, obtained by expanding the
     */
    CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            CourseConfiguration courseConfiguration, TimePoint timePointForDefinitionOfMarksAndDeviceMappings,
            AbstractLogEventAuthor author);
}
