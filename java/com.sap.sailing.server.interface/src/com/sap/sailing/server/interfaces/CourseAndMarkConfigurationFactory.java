package com.sap.sailing.server.interfaces;

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
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
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
    // TODO do we still need this?
    // You can create a CourseConfiguration from the CourseTemplate, update the lap count and create the Course from this.
    Course createCourse(CourseTemplate courseTemplate, int numberOfLaps);

    /**
     * The given {@link Course} need to exactly match the structure of the given {@link CourseTemplate} regarding the
     * following specifics:
     * <ul>
     * <li>All used {@link Mark Marks} need to reference a {@link MarkTemplate} being part of the given
     * {@link CourseTemplate}</li>
     * <li>The parts before and after the repeatable part need to exactly match the start and end of the course</li>
     * <li>Between the start end only full cycles of the repeatable part need to exist</li>
     * </ul>
     * 
     * In addition, the given {@link CourseTemplate} needs to provide a repeatable part.
     * 
     */
    // TODO do we still need this?
    // You can create a CourseConfiguration from the Course, update tha lap count and save back to solve this.
    void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps);

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
     * If {@link MarkConfiguration#getOptionalMarkTemplate() markConfiguration.getOptionalMarkTemplate()} returns a
     * non-{@code null} {@link MarkTemplate} then that is returned. Otherwise, a new {@link MarkTemplate} object is
     * created which reflects the {@link #getEffectiveProperties() effective properties} that this mark configuration
     * currently has.
     */
    MarkTemplate getOrCreateMarkTemplate(MarkConfiguration markConfiguration);
    
    /**
     * There are cases where a {@link MarkProperties} instance is required while editing a {@link CourseConfiguration} in the UI.
     * This can e.g. be handy if tracking needs to get set up for via the user's mark inventory.
     * 
     * @return Replacement definition for the given {@link MarkConfiguration}
     */
    MarkPropertiesBasedMarkConfiguration createOrUpdateMarkProperties(MarkConfiguration markProperties);
    
    /**
     * There are cases where a {@link Mark} is required while editing a {@link CourseConfiguration} in the UI.
     * This can e.g. be handy if tracking needs to get set up while the course isn't ready to save.
     * 
     * @return Replacement definition for the given {@link MarkConfiguration}
     */
    RegattaMarkConfiguration createMark(Regatta regatta, MarkConfiguration markConfiguration);

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
     * 
     * @param name
     *            the name of the new course template; returned by calling
     *            {@link CourseConfiguration#getOptionalCourseTemplate()
     *            getOptionalCourseTemplate()}.{@link CourseTemplate#getName() getName()} on the result of this method.
     * @param optionalRegatta TODO
     * @return a course with its mark configurations and a {@link CourseTemplate} returned by
     *         {@link CourseConfiguration#getOptionalCourseTemplate()} that is always valid, never {@code null}. All
     *         {@link MarkConfiguration} objects in the result all have a valid
     *         {@link MarkConfiguration#getOptionalMarkTemplate() mark template} that is part of the
     *         {@link CourseTemplate} obtained from {@link CourseConfiguration#getOptionalCourseTemplate()}.
     */
    CourseConfiguration createCourseTemplateAndUpdatedConfiguration(String name,
            CourseConfiguration courseWithMarkConfiguration);

    CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate, Regatta optionalRegatta,
            Iterable<String> tagsToFilterMarkProperties, String courseConfigurationName);

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
            CourseConfiguration courseConfiguration, int lapCount,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings, AbstractLogEventAuthor author);
}
