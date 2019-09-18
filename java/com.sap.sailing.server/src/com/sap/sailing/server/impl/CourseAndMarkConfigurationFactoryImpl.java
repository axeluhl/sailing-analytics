package com.sap.sailing.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairTemplate;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.RegattaMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointWithMarkConfigurationImpl;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.server.interfaces.CourseAndMarkConfigurationFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class CourseAndMarkConfigurationFactoryImpl implements CourseAndMarkConfigurationFactory {
    
    private final SharedSailingData sharedSailingData;

    public CourseAndMarkConfigurationFactoryImpl(SharedSailingData sharedSailingData) {
        this.sharedSailingData = sharedSailingData;
    }

    @Override
    public Course createCourse(CourseTemplate courseTemplate, int numberOfLaps) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateCourse(Course courseToUpdate, CourseTemplate courseTemplate, int numberOfLaps) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public CourseTemplate resolveCourseTemplate(Course course) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkTemplate getOrCreateMarkTemplate(MarkConfiguration markConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseConfiguration createCourseTemplateAndUpdatedConfiguration(String name,
            CourseConfiguration courseWithMarkConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            CourseConfiguration courseTemplateMappingWithMarkTemplateMappings, int lapCount,
            TimePoint timePointForDefinitionOfMarksAndDeviceMappings, AbstractLogEventAuthor author) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkPropertiesBasedMarkConfiguration createOrUpdateMarkProperties(MarkConfiguration markProperties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RegattaMarkConfiguration createMark(Regatta regatta, MarkConfiguration markConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private Pair<RegattaMarkConfiguration, MarkTemplate> createMarkConfigurationForRegattaMark(CourseTemplate courseTemplate,
            Regatta regatta, Mark mark) {
        MarkTemplate markTemplate = null;
        final UUID markTemplateIdOrNull = mark.getOriginatingMarkTemplateIdOrNull();
        if (markTemplateIdOrNull != null) {
            // TODO first try to find it in the CourseTemplate
            // TODO this call may fail and should not prevent the user from creating a course for a regatta
            markTemplate = sharedSailingData.getMarkTemplateById(markTemplateIdOrNull);
        }
        // TODO get positioning
        final RegattaMarkConfiguration regattaMarkConfiguration = new RegattaMarkConfigurationImpl(mark, /* TODO optionalPositioning */ null);
        return new Pair<>(regattaMarkConfiguration, markTemplate);
    }

    @Override
    public CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate,
            Regatta optionalRegatta, Iterable<String> tagsToFilterMarkProperties) {
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();
        final Map<MarkTemplate, MarkConfiguration> markTemplateConfigurationCache = new HashMap<>();
        final Map<Mark, RegattaMarkConfiguration> regattaMarkConfigurationCache = new HashMap<>();
        if (optionalRegatta != null) {
            for (RaceColumn raceColumn : optionalRegatta.getRaceColumns()) {
                for (Mark mark : raceColumn.getAvailableMarks()) {
                    regattaMarkConfigurationCache.computeIfAbsent(mark, m -> {
                        final Pair<RegattaMarkConfiguration, MarkTemplate> configResult = createMarkConfigurationForRegattaMark(courseTemplate, optionalRegatta, m);
                        // TODO calculate last use
                        if (configResult.getB() != null) {
                            markTemplateConfigurationCache.put(configResult.getB(), configResult.getA());
                        }
                        allMarkConfigurations.add(configResult.getA());
                        return configResult.getA();
                    });
                }
            }
        }
        for (MarkTemplate markTemplate : courseTemplate.getMarkTemplates()) {
            markTemplateConfigurationCache.computeIfAbsent(markTemplate, mt -> {
                // TODO determin matching MarkProperties to associate
                MarkTemplateBasedMarkConfiguration markConfiguration = new MarkTemplateBasedMarkConfigurationImpl(markTemplate);
                allMarkConfigurations.add(markConfiguration);
                return markConfiguration;
            });
        }
        final Map<MarkConfiguration, String> resultingRoleMapping = new HashMap<>();
        for (Entry<MarkTemplate, String> markTemplateWithRole : courseTemplate.getAssociatedRoles().entrySet()) {
            resultingRoleMapping.put(markTemplateConfigurationCache.get(markTemplateWithRole.getKey()), markTemplateWithRole.getValue());
        }
        final List<WaypointWithMarkConfiguration> resultingWaypoints = new ArrayList<>();
        for (WaypointTemplate waypointTemplate : courseTemplate.getWaypointTemplates()) {
            final ControlPointTemplate controlPointTemplate = waypointTemplate.getControlPointTemplate();
            final ControlPointWithMarkConfiguration resultingControlPoint;
            if (controlPointTemplate instanceof MarkTemplate) {
                MarkTemplate markTemplate = (MarkTemplate) controlPointTemplate;
                resultingControlPoint = markTemplateConfigurationCache.get(markTemplate);
            } else {
                final MarkPairTemplate markPairTemplate = (MarkPairTemplate) controlPointTemplate;
                final MarkConfiguration left = markTemplateConfigurationCache.get(markPairTemplate.getLeft());
                final MarkConfiguration right = markTemplateConfigurationCache.get(markPairTemplate.getRight());
                resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), right, left);
            }
            resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                    waypointTemplate.getPassingInstruction()));
        }
        return new CourseConfigurationImpl(courseTemplate, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, courseTemplate.getRepeatablePart(), /* TODO numberOfLaps */ 2);
    }

    @Override
    public List<MarkProperties> createMarkPropertiesSuggestionsForMarkConfiguration(Regatta optionalRegatta,
            MarkConfiguration markConfiguration, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }

}
