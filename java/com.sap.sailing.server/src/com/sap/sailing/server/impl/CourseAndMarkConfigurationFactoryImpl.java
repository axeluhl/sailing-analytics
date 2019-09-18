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
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
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
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.interfaces.CourseAndMarkConfigurationFactory;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
    
    @Override
    public CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate,
            Regatta optionalRegatta, Iterable<String> tagsToFilterMarkProperties) {
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();
        final Map<MarkTemplate, MarkConfiguration> markTemplateConfigurationCache = new HashMap<>();
        if (optionalRegatta != null) {
            RegattaMarkConfigurations regattaMarkConfigurations = new RegattaMarkConfigurations(courseTemplate, optionalRegatta);
            allMarkConfigurations.addAll(regattaMarkConfigurations.regattaConfigurationsByMark.values());
            markTemplateConfigurationCache.putAll(regattaMarkConfigurations.markConfigurationsByMarkTemplate);
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
    public CourseConfiguration createCourseConfigurationFromCourse(CourseBase course,
            Regatta regatta, Iterable<String> tagsToFilterMarkProperties) {
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();
        final Map<MarkTemplate, MarkConfiguration> markTemplateConfigurationCache = new HashMap<>();
        
        final RegattaMarkConfigurations regattaMarkConfigurations = new RegattaMarkConfigurations(/* TODO courseTemplate */ null, regatta);
        allMarkConfigurations.addAll(regattaMarkConfigurations.regattaConfigurationsByMark.values());
        markTemplateConfigurationCache.putAll(regattaMarkConfigurations.markConfigurationsByMarkTemplate);
        
        final Map<MarkConfiguration, String> resultingRoleMapping = new HashMap<>();
        // TODO
//        for (Entry<Mark, String> markWithRole : course.getAssociatedRoles().entrySet()) {
//            resultingRoleMapping.put(regattaMarkConfigurations.regattaConfigurationsByMark.get(markWithRole.getKey()), markWithRole.getValue());
//        }
        
        final List<WaypointWithMarkConfiguration> resultingWaypoints = new ArrayList<>();
        // TODO reconstruct base course in case it is based on a template with repeatable parts
        for (Waypoint waypoint : course.getWaypoints()) {
            final ControlPoint controlPoint = waypoint.getControlPoint();
            final ControlPointWithMarkConfiguration resultingControlPoint;
            if (controlPoint instanceof Mark) {
                final Mark mark = (Mark) controlPoint;
                resultingControlPoint = regattaMarkConfigurations.regattaConfigurationsByMark.get(mark);
            } else {
                final ControlPointWithTwoMarks markPairTemplate = (ControlPointWithTwoMarks) controlPoint;
                final MarkConfiguration left = regattaMarkConfigurations.regattaConfigurationsByMark.get(markPairTemplate.getLeft());
                final MarkConfiguration right = regattaMarkConfigurations.regattaConfigurationsByMark.get(markPairTemplate.getRight());
                resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), right, left);
            }
            resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                    waypoint.getPassingInstructions()));
        }
        return new CourseConfigurationImpl(/* TODO courseTemplate */ null, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, /* optionalRepeatablePart */ null, /* TODO numberOfLaps */ 2);
    }

    @Override
    public List<MarkProperties> createMarkPropertiesSuggestionsForMarkConfiguration(Regatta optionalRegatta,
            MarkConfiguration markConfiguration, Iterable<String> tagsToFilterMarkProperties) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * First tries to resolve from the course template and then by ID using sharedSailingData. This allows users having
     * access to a course template to use all mark templates being included even if they don't have explicit read
     * permissions for those.
     */
    private MarkTemplate resolveMarkTemplateByID(CourseTemplate courseTemplate, UUID markTemplateID) {
        MarkTemplate resolvedMarkTemplate = null;
        if (courseTemplate != null) {
            for (MarkTemplate markTemplate : courseTemplate.getMarkTemplates()) {
                if (markTemplate.getId().equals(markTemplateID)) {
                    resolvedMarkTemplate = markTemplate;
                    break;
                }
            }
        }
        if (resolvedMarkTemplate == null) {
            // TODO this call may fail and should not prevent the user from creating a course for a regatta
            resolvedMarkTemplate = sharedSailingData.getMarkTemplateById(markTemplateID);
        }
        return resolvedMarkTemplate;
    }

    private class RegattaMarkConfigurations {
        final Map<MarkTemplate, RegattaMarkConfiguration> markConfigurationsByMarkTemplate = new HashMap<>();
        final Map<Mark, RegattaMarkConfiguration> regattaConfigurationsByMark = new HashMap<>();
        final Map<RegattaMarkConfiguration, TimePoint> lastUsages = new HashMap<>();

        public RegattaMarkConfigurations(CourseTemplate courseTemplate, Regatta regatta) {
            for (RaceColumn raceColumn : regatta.getRaceColumns()) {
                for (Mark mark : raceColumn.getAvailableMarks()) {
                    final RegattaMarkConfiguration regattaMarkConfiguration = regattaConfigurationsByMark
                            .computeIfAbsent(mark,
                                    m -> createMarkConfigurationForRegattaMark(courseTemplate, regatta, m));
                    
                    for (Fleet fleet : raceColumn.getFleets()) {
                        final TrackedRace trackedRaceOrNull = raceColumn.getTrackedRace(fleet);
                        if (Util.contains(raceColumn.getCourseMarks(), mark)) {
                            TimePoint usage = null;
                            if (trackedRaceOrNull != null) {
                                usage = trackedRaceOrNull.getStartOfRace();
                            }
                            if (usage == null) {
                                usage = trackedRaceOrNull.getStartOfTracking();
                            }
                            if (usage != null) {
                                final TimePoint effectiveUsageTP = usage;
                                lastUsages.compute(regattaMarkConfiguration,
                                        (mc, existingTP) -> (existingTP == null || existingTP.before(effectiveUsageTP))
                                                ? effectiveUsageTP
                                                : existingTP);
                            }
                        }
                    }
                }
            }

            for (Entry<Mark, RegattaMarkConfiguration> regattaMarkEntry : regattaConfigurationsByMark.entrySet()) {
                final RegattaMarkConfiguration regattaMarkConfiguration = regattaMarkEntry.getValue();
                final MarkTemplate associatedMarkTemplateOrNull = regattaMarkConfiguration.getOptionalMarkTemplate();
                if (associatedMarkTemplateOrNull != null) {
                    markConfigurationsByMarkTemplate.compute(associatedMarkTemplateOrNull, (mt, rmc) -> {
                        if (rmc == null) {
                            return regattaMarkConfiguration;
                        }
                        final TimePoint lastUsageOrNull = lastUsages.get(regattaMarkConfiguration);
                        if (lastUsageOrNull == null) {
                            return rmc;
                        }
                        final TimePoint lastUsageOfExistingOrNull = lastUsages.get(rmc);
                        if (lastUsageOfExistingOrNull == null) {
                            return regattaMarkConfiguration;
                        }
                        return lastUsageOrNull.after(lastUsageOfExistingOrNull) ? regattaMarkConfiguration : rmc;
                    });
                }
            }
        }

        private RegattaMarkConfiguration createMarkConfigurationForRegattaMark(CourseTemplate courseTemplate,
                Regatta regatta, Mark mark) {
            MarkTemplate markTemplate = null;
            final UUID markTemplateIdOrNull = mark.getOriginatingMarkTemplateIdOrNull();
            if (markTemplateIdOrNull != null) {
                markTemplate = resolveMarkTemplateByID(courseTemplate, markTemplateIdOrNull);
            }
            // TODO get positioning
            final RegattaMarkConfiguration regattaMarkConfiguration = new RegattaMarkConfigurationImpl(mark,
                    /* TODO optionalPositioning */ null, markTemplate);
            return regattaMarkConfiguration;
        }
    }
}
