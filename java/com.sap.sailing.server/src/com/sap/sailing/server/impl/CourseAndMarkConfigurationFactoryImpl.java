package com.sap.sailing.server.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.FixedPositioning;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairTemplate;
import com.sap.sailing.domain.coursetemplate.MarkPairTemplate.MarkPairTemplateFactory;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.SmartphoneUUIDPositioning;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.RegattaMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointWithMarkConfigurationImpl;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.PingDeviceIdentifierImpl;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.deserialization.impl.CourseConfigurationBuilder;
import com.sap.sailing.server.interfaces.CourseAndMarkConfigurationFactory;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class CourseAndMarkConfigurationFactoryImpl implements CourseAndMarkConfigurationFactory {

    private static final Logger log = Logger.getLogger(CourseAndMarkConfigurationFactoryImpl.class.getName());
    
    private final SharedSailingData sharedSailingData;
    private final SensorFixStore sensorFixStore;
    private final Function<DeviceIdentifier, Position> positionResolver;

    public CourseAndMarkConfigurationFactoryImpl(SharedSailingData sharedSailingData, SensorFixStore sensorFixStore) {
        this.sharedSailingData = sharedSailingData;
        this.sensorFixStore = sensorFixStore;
        positionResolver = identifier -> {
            Position lastPosition = null;
            try {
                final Map<DeviceIdentifier, Timed> lastFix = sensorFixStore
                        .getLastFix(Collections.singleton(identifier));

                final Timed t = lastFix.get(identifier);
                if (t instanceof GPSFix) {
                    lastPosition = ((GPSFix) t).getPosition();
                }
            } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                log.log(Level.WARNING, "Could not load associated fix for device " + identifier, e);
            }
            return lastPosition;
        };
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
    public CourseTemplate resolveCourseTemplate(CourseBase course) {
        if (course.getOriginatingCourseTemplateIdOrNull() == null) {
            return null;
        }
        // TODO this call may fail due to required permissions
        return sharedSailingData.getCourseTemplateById(course.getOriginatingCourseTemplateIdOrNull());
    }

    @Override
    public MarkTemplate getOrCreateMarkTemplate(MarkConfiguration markConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CourseConfiguration createCourseTemplateAndUpdatedConfiguration(
            CourseConfiguration courseWithMarkConfiguration, Iterable<String> tags, URL optionalImageUrl) {
        final Map<MarkConfiguration, MarkTemplate> markTemplatesByMarkConfigurations = new HashMap<>();
        final Map<MarkConfiguration, MarkConfiguration> marksConfigurationsMapping = new HashMap<>();
        for (MarkConfiguration markConfiguration : courseWithMarkConfiguration.getAllMarks()) {
            final MarkConfiguration effectiveConfiguration;
            final MarkTemplate effectiveMarkTemplate;
            if (markConfiguration instanceof MarkTemplateBasedMarkConfiguration) {
                effectiveConfiguration = (MarkTemplateBasedMarkConfiguration) markConfiguration;
                effectiveMarkTemplate = effectiveConfiguration.getOptionalMarkTemplate();
            } else {
                final MarkTemplate markTemplateOrNull = markConfiguration.getOptionalMarkTemplate();
                if (markTemplateOrNull != null && markTemplateOrNull.hasEqualAppeareanceWith(markConfiguration.getEffectiveProperties())) {
                    effectiveMarkTemplate = markTemplateOrNull;
                } else {
                    effectiveMarkTemplate = sharedSailingData
                            .createMarkTemplate(markConfiguration.getEffectiveProperties());
                }
                if (markConfiguration instanceof RegattaMarkConfiguration) {
                    final Mark mark = ((RegattaMarkConfiguration) markConfiguration).getMark();
                    final UUID markPropertiesIdOrNull = mark.getOriginatingMarkPropertiesIdOrNull();
                    try {
                        final MarkProperties markPropertiesOrNull = markPropertiesIdOrNull == null ? null
                                : sharedSailingData.getMarkPropertiesById(markPropertiesIdOrNull);
                        if (markPropertiesOrNull != null) {
                            sharedSailingData.recordUsage(effectiveMarkTemplate, markPropertiesOrNull);
                        }
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Could not record usage for mark properties " + markPropertiesIdOrNull
                                + " an mark template " + effectiveMarkTemplate.getId(), e);
                    }
                    // The configuration is used as is. We can't enrich the Mark with the newly created MarkTemplate.
                    // In the UI, the regatta Mark still needs to be selected to ensure that all connections to regatta
                    // Marks are unchanged when saving the course to a race afterwards.
                    effectiveConfiguration = markConfiguration;
                } else if (markConfiguration instanceof MarkPropertiesBasedMarkConfiguration) {
                    // In this case the appearance of the created MarkTemplate is identical to the MarkProperties it is
                    // based on.
                    final MarkProperties markProperties = ((MarkPropertiesBasedMarkConfiguration) markConfiguration)
                            .getMarkProperties();
                    effectiveConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(markProperties,
                            effectiveMarkTemplate, markConfiguration.getOptionalPositioning(), getPositioningIfAvailable(markProperties));
                    sharedSailingData.recordUsage(effectiveMarkTemplate, markProperties);
                } else if (markConfiguration instanceof FreestyleMarkConfiguration) {
                    final MarkProperties markPropertiesOrNull = ((FreestyleMarkConfiguration) markConfiguration)
                            .getOptionalMarkProperties();
                    if (markPropertiesOrNull != null) {
                        sharedSailingData.recordUsage(effectiveMarkTemplate, markPropertiesOrNull);
                        if (markPropertiesOrNull.hasEqualAppeareanceWith(effectiveMarkTemplate)) {
                            effectiveConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(markPropertiesOrNull,
                                    effectiveMarkTemplate, markConfiguration.getOptionalPositioning(), getPositioningIfAvailable(markPropertiesOrNull));
                        } else {
                            effectiveConfiguration = new FreestyleMarkConfigurationImpl(effectiveMarkTemplate,
                                    markPropertiesOrNull, effectiveMarkTemplate, markConfiguration.getOptionalPositioning(),
                                    getPositioningIfAvailable(markPropertiesOrNull));
                        }
                    } else {
                        effectiveConfiguration = new MarkTemplateBasedMarkConfigurationImpl(effectiveMarkTemplate,
                                markConfiguration.getOptionalPositioning());
                    }
                } else {
                    // Should never happen but could in case a new MarkConfiguration type is defined
                    throw new IllegalStateException("Unknown mark configuration type found");
                }
            }
            markTemplatesByMarkConfigurations.put(markConfiguration, effectiveConfiguration.getOptionalMarkTemplate());
            marksConfigurationsMapping.put(markConfiguration, effectiveConfiguration);
        }

        final Map<MarkConfiguration, String> associatedRolesInConfiguration = new HashMap<>();
        final Map<MarkTemplate, String> associatedRolesInTemplate = new HashMap<>();
        final List<WaypointTemplate> waypointTemplates = new ArrayList<>();
        final List<WaypointWithMarkConfiguration> effectiveWaypoints = new ArrayList<>();
        final MarkPairTemplateFactory markPairTemplateFactory = new MarkPairTemplateFactory();
        final Function<MarkConfiguration, MarkConfiguration> markConfigurationMapper = mc -> {
            final MarkConfiguration result = marksConfigurationsMapping.get(mc);
            String roleName = courseWithMarkConfiguration.getAssociatedRoles().get(mc);
            if (roleName == null) {
                // ensure a defined role name for any MarkTemplate used in the CourseTemplate
                roleName = result.getEffectiveProperties().getShortName();
            }
            associatedRolesInConfiguration.put(result, roleName);
            associatedRolesInTemplate.put(result.getOptionalMarkTemplate(), roleName);
            return result;
        };
        // TODO record usages for MarkProperties by role name
        // Caches to allow reusing objects for mark pairs that are based on the same MarkPairWithConfiguration
        final Map<MarkPairWithConfiguration, MarkPairTemplate> markPairTemplateCache = new HashMap<>();
        final Map<MarkPairWithConfiguration, MarkPairWithConfiguration> markPairConfigurationCache = new HashMap<>();
        
        for (WaypointWithMarkConfiguration waypointWithMarkConfiguration : courseWithMarkConfiguration.getWaypoints()) {
            final ControlPointWithMarkConfiguration controlPoint = waypointWithMarkConfiguration.getControlPoint();
            final ControlPointWithMarkConfiguration effectiveControlPointConfiguration;
            final ControlPointTemplate effectiveControlPointTemplate;
            if (controlPoint instanceof MarkConfiguration) {
                effectiveControlPointConfiguration = markConfigurationMapper.apply((MarkConfiguration)controlPoint);
                effectiveControlPointTemplate = markTemplatesByMarkConfigurations.get(controlPoint);
            } else {
                final MarkPairWithConfiguration markPairTemplate = (MarkPairWithConfiguration) controlPoint;
                final MarkConfiguration leftConfiguration = markConfigurationMapper.apply(markPairTemplate.getLeft());
                final MarkTemplate leftTemplate = markTemplatesByMarkConfigurations.get(markPairTemplate.getLeft());
                final MarkConfiguration rightConfiguration = markConfigurationMapper.apply(markPairTemplate.getRight());
                final MarkTemplate rightTemplate = markTemplatesByMarkConfigurations.get(markPairTemplate.getRight());
                effectiveControlPointConfiguration = markPairConfigurationCache.computeIfAbsent(markPairTemplate,
                        mpt -> new MarkPairWithConfigurationImpl(markPairTemplate.getName(), leftConfiguration,
                                rightConfiguration, markPairTemplate.getShortName()));
                effectiveControlPointTemplate = markPairTemplateCache.computeIfAbsent(markPairTemplate,
                        mpt -> markPairTemplateFactory.create(markPairTemplate.getName(),
                                markPairTemplate.getShortName(),
                                leftTemplate, rightTemplate));
            }
            waypointTemplates.add(new WaypointTemplateImpl(effectiveControlPointTemplate, waypointWithMarkConfiguration.getPassingInstruction()));
            effectiveWaypoints.add(new WaypointWithMarkConfigurationImpl(effectiveControlPointConfiguration, waypointWithMarkConfiguration.getPassingInstruction()));
        }
        final CourseTemplate newCourseTemplate = sharedSailingData.createCourseTemplate(courseWithMarkConfiguration.getName(), new HashSet<>(markTemplatesByMarkConfigurations.values()),
                waypointTemplates, associatedRolesInTemplate, courseWithMarkConfiguration.getRepeatablePart(),
                tags, optionalImageUrl,
                courseWithMarkConfiguration.getNumberOfLaps());
        return new CourseConfigurationImpl(newCourseTemplate,
                new HashSet<>(marksConfigurationsMapping.values()),
                associatedRolesInConfiguration, effectiveWaypoints, courseWithMarkConfiguration.getRepeatablePart(),
                courseWithMarkConfiguration.getNumberOfLaps(),
                courseWithMarkConfiguration.getName());
    }

    private void savePositioningToMark(Regatta regatta, Mark mark, Positioning optionalExplicitPositioning,
            MarkProperties optionalAssociatedMarkProperties, TimePoint timePointForDefinitionOfMarksAndDeviceMappings,
            AbstractLogEventAuthor author) {
        Position position = null;
        DeviceIdentifier deviceIdentifier = null;
        if (optionalExplicitPositioning != null) {
            if (optionalExplicitPositioning instanceof FixedPositioning) {
                final FixedPositioning fixedPositioning = (FixedPositioning) optionalExplicitPositioning;
                position = fixedPositioning.getPosition();
            } else if (optionalExplicitPositioning instanceof SmartphoneUUIDPositioning) {
                final SmartphoneUUIDPositioning smartphoneUUIDPositioning = (SmartphoneUUIDPositioning) optionalExplicitPositioning;
                deviceIdentifier = new SmartphoneUUIDIdentifierImpl(smartphoneUUIDPositioning.getDeviceUUID());
            }
        }
        if (optionalAssociatedMarkProperties != null && position == null && deviceIdentifier == null) {
            position = optionalAssociatedMarkProperties.getFixedPosition();
            deviceIdentifier = optionalAssociatedMarkProperties.getTrackingDeviceIdentifier();
        }

        if (position != null ^ deviceIdentifier != null) {
            final DeviceMappingWithRegattaLogEvent<Mark> existingDeviceMapping = CourseConfigurationBuilder.findMostRecentOrOngoingMapping(regatta, mark);
            if (deviceIdentifier != null) {
                if (existingDeviceMapping == null || !(deviceIdentifier.equals(existingDeviceMapping.getDevice()) && existingDeviceMapping.getTimeRange().hasOpenEnd())) {
                    regatta.getRegattaLog()
                    .add(new RegattaLogDeviceMarkMappingEventImpl(
                            timePointForDefinitionOfMarksAndDeviceMappings, author, mark, deviceIdentifier,
                            timePointForDefinitionOfMarksAndDeviceMappings, null));
                }
            } else if (position != null) {
                final boolean update;
                if (existingDeviceMapping != null) {
                    if (!PingDeviceIdentifier.TYPE.equals(existingDeviceMapping.getDevice().getIdentifierType())) {
                        update = true;
                    } else {
                        final Position lastPingedPositionOrNull = positionResolver.apply(existingDeviceMapping.getDevice());
                        update = lastPingedPositionOrNull == null || !lastPingedPositionOrNull.equals(position);
                    }
                } else {
                    update = true;
                }
                if (update) {
                    final PingDeviceIdentifierImpl pingIdentifier = new PingDeviceIdentifierImpl(UUID.randomUUID());
                    
                    sensorFixStore.storeFix(pingIdentifier,
                            new GPSFixImpl(position, timePointForDefinitionOfMarksAndDeviceMappings));
                    
                    regatta.getRegattaLog()
                    .add(new RegattaLogDeviceMarkMappingEventImpl(
                            timePointForDefinitionOfMarksAndDeviceMappings, author, mark, pingIdentifier,
                            timePointForDefinitionOfMarksAndDeviceMappings,
                            timePointForDefinitionOfMarksAndDeviceMappings));
                }
            }
        }
    }

    @Override
    public CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            CourseConfiguration courseConfiguration, TimePoint timePointForDefinitionOfMarksAndDeviceMappings,
            AbstractLogEventAuthor author) {
        final Map<MarkConfiguration, Mark> marksByMarkConfigurations = new HashMap<>();
        final RegattaLog regattaLog = regatta.getRegattaLog();
        for (MarkConfiguration markConfiguration : courseConfiguration.getAllMarks()) {
            if (markConfiguration instanceof RegattaMarkConfiguration) {
                final Mark mark = ((RegattaMarkConfiguration) markConfiguration).getMark();
                marksByMarkConfigurations.put(markConfiguration,
                        mark);
                savePositioningToMark(regatta, mark, markConfiguration.getOptionalPositioning(), null,
                        timePointForDefinitionOfMarksAndDeviceMappings, author);
            } else {
                final MarkTemplate optionalMarkTemplate = markConfiguration.getOptionalMarkTemplate();
                final MarkProperties optionalMarkProperties;
                if (markConfiguration instanceof MarkPropertiesBasedMarkConfiguration) {
                    optionalMarkProperties = ((MarkPropertiesBasedMarkConfiguration) markConfiguration)
                            .getMarkProperties();
                } else if (markConfiguration instanceof FreestyleMarkConfiguration) {
                    optionalMarkProperties = ((FreestyleMarkConfiguration) markConfiguration)
                            .getOptionalMarkProperties();
                } else {
                    optionalMarkProperties = null;
                }
                final CommonMarkProperties effectiveProperties = markConfiguration.getEffectiveProperties();
                final Mark markToCreate = new MarkImpl(UUID.randomUUID(), effectiveProperties.getName(),
                        effectiveProperties.getShortName(), effectiveProperties.getType(),
                        effectiveProperties.getColor(), effectiveProperties.getShape(),
                        effectiveProperties.getPattern(),
                        optionalMarkTemplate == null ? null : optionalMarkTemplate.getId(),
                        optionalMarkProperties == null ? null : optionalMarkProperties.getId());
                regattaLog.add(new RegattaLogDefineMarkEventImpl(timePointForDefinitionOfMarksAndDeviceMappings, author,
                        timePointForDefinitionOfMarksAndDeviceMappings, UUID.randomUUID(), markToCreate));
                marksByMarkConfigurations.put(markConfiguration, markToCreate);
                
                savePositioningToMark(regatta, markToCreate, markConfiguration.getOptionalPositioning(),
                        optionalMarkProperties, timePointForDefinitionOfMarksAndDeviceMappings, author);
            }
        }
        final CourseDataImpl course = new CourseDataImpl(courseConfiguration.getName(),
                courseConfiguration.getOptionalCourseTemplate() == null ? null
                        : courseConfiguration.getOptionalCourseTemplate().getId());

        final Map<Mark, String> associatedRolesToSave = new HashMap<>();
        final Iterable<WaypointWithMarkConfiguration> waypoints;
        if (courseConfiguration.hasRepeatablePart()) {
            if (courseConfiguration.getNumberOfLaps() == null) {
                throw new IllegalStateException("A course with repeatable part requires a lap count");
            }
            waypoints = courseConfiguration.getWaypoints(courseConfiguration.getNumberOfLaps());
        } else {
            waypoints = courseConfiguration.getWaypoints();
        }
        // Cache to allow reusing ControlPointWithTwoMarks objects that are based on the same MarkPairWithConfiguration
        final Map<MarkPairWithConfiguration, ControlPointWithTwoMarks> markPairCache = new HashMap<>();
        
        for (WaypointWithMarkConfiguration waypointWithMarkConfiguration : waypoints) {
            final ControlPointWithMarkConfiguration controlPointWithMarkConfiguration = waypointWithMarkConfiguration
                    .getControlPoint();
            final Function<MarkConfiguration, Mark> markMapper = markConfiguration -> {
                final Mark mark = marksByMarkConfigurations.get(markConfiguration);
                if (mark == null) {
                    throw new IllegalStateException("Non declared mark found in waypoint sequence");
                }
                if (!associatedRolesToSave.containsKey(mark)) {
                    final String associatedRoleOrNull = courseConfiguration.getAssociatedRoles().get(markConfiguration);
                    if (associatedRoleOrNull != null) {
                        associatedRolesToSave.put(mark, associatedRoleOrNull);
                    }
                }
                return mark;
            };

            if (controlPointWithMarkConfiguration instanceof MarkConfiguration) {
                final MarkConfiguration markConfiguration = (MarkConfiguration) controlPointWithMarkConfiguration;
                course.addWaypoint(Util.size(course.getWaypoints()),
                        new WaypointImpl(markMapper.apply(markConfiguration), waypointWithMarkConfiguration.getPassingInstruction()));
            } else {
                final ControlPointWithTwoMarks controlPoint = markPairCache
                        .computeIfAbsent((MarkPairWithConfiguration) controlPointWithMarkConfiguration, mpwc -> {
                            final Mark left = markMapper.apply(mpwc.getLeft());
                            final Mark right = markMapper.apply(mpwc.getRight());
                            return new ControlPointWithTwoMarksImpl(UUID.randomUUID(), left, right, mpwc.getName(),
                                    mpwc.getShortName());
                        });
                course.addWaypoint(Util.size(course.getWaypoints()),
                        new WaypointImpl(controlPoint, waypointWithMarkConfiguration.getPassingInstruction()));
            }
        }
        associatedRolesToSave.forEach(course::addRoleMapping);
        // TODO record usages for MarkProperties by role name
        return course;
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
        final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations = new HashMap<>();
        if (optionalRegatta != null) {
            // If we have a regatta context, we first try to get all existing marks and their association to
            // MarkTemplates from the regatta
            final RegattaMarkConfigurations regattaMarkConfigurations = new RegattaMarkConfigurations(courseTemplate,
                    optionalRegatta);
            allMarkConfigurations.addAll(regattaMarkConfigurations.regattaConfigurationsByMark.values());
            markTemplatesToMarkConfigurations.putAll(regattaMarkConfigurations.markConfigurationsByMarkTemplate);
        }
        for (MarkTemplate markTemplate : courseTemplate.getMarkTemplates()) {
            // For any MarkTemplate that wasn't resolved from the regatta, an explicit entry needs to get created
            markTemplatesToMarkConfigurations.computeIfAbsent(markTemplate, mt -> {

                final MarkConfiguration markConfiguration = new MarkTemplateBasedMarkConfigurationImpl(markTemplate,
                        null);
                allMarkConfigurations.add(markConfiguration);
                return markConfiguration;
            });
        }

        replaceTemplateBasedConfigurationCandidatesBySuggestedPropertiesBasedConfigurations(markTemplatesToMarkConfigurations, allMarkConfigurations, tagsToFilterMarkProperties,
                courseTemplate.getAssociatedRoles());

        final Map<MarkConfiguration, String> resultingRoleMapping = createRoleMappingWithMarkTemplateMapping(
                courseTemplate, markTemplatesToMarkConfigurations);
        final List<WaypointWithMarkConfiguration> resultingWaypoints = createWaypointConfigurationsWithMarkTemplateMapping(
                courseTemplate, markTemplatesToMarkConfigurations);
        return new CourseConfigurationImpl(courseTemplate, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, courseTemplate.getRepeatablePart(), courseTemplate.getDefaultNumberOfLaps(),
                courseTemplate.getName());
    }

    private Map<MarkConfiguration, String> createRoleMappingWithMarkTemplateMapping(CourseTemplate courseTemplate,
            final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations) {
        final Map<MarkConfiguration, String> resultingRoleMapping = new HashMap<>();
        for (Entry<MarkTemplate, String> markTemplateWithRole : courseTemplate.getAssociatedRoles().entrySet()) {
            resultingRoleMapping.put(markTemplatesToMarkConfigurations.get(markTemplateWithRole.getKey()),
                    markTemplateWithRole.getValue());
        }
        return resultingRoleMapping;
    }

    private List<WaypointWithMarkConfiguration> createWaypointConfigurationsWithMarkTemplateMapping(
            CourseTemplate courseTemplate,
            final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations) {
        final List<WaypointWithMarkConfiguration> resultingWaypoints = new ArrayList<>();
        for (WaypointTemplate waypointTemplate : courseTemplate.getWaypointTemplates()) {
            final ControlPointTemplate controlPointTemplate = waypointTemplate.getControlPointTemplate();
            final ControlPointWithMarkConfiguration resultingControlPoint;
            if (controlPointTemplate instanceof MarkTemplate) {
                MarkTemplate markTemplate = (MarkTemplate) controlPointTemplate;
                resultingControlPoint = markTemplatesToMarkConfigurations.get(markTemplate);
            } else {
                final MarkPairTemplate markPairTemplate = (MarkPairTemplate) controlPointTemplate;
                final MarkConfiguration left = markTemplatesToMarkConfigurations.get(markPairTemplate.getLeft());
                final MarkConfiguration right = markTemplatesToMarkConfigurations.get(markPairTemplate.getRight());
                resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), right, left,
                        markPairTemplate.getShortName());
            }
            resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                    waypointTemplate.getPassingInstruction()));
        }
        return resultingWaypoints;
    }

    @Override
    public CourseConfiguration createCourseConfigurationFromCourse(CourseBase course, Regatta regatta,
            Iterable<String> tagsToFilterMarkProperties) {
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();

        // TODO this call may fail due to required permissions. Most probably we should just handle it like a course
        // without template reference instead of letting it fail.
        final CourseTemplate courseTemplateOrNull = resolveCourseTemplate(course);
        final RegattaMarkConfigurations regattaMarkConfigurations = new RegattaMarkConfigurations(courseTemplateOrNull,
                regatta);
        allMarkConfigurations.addAll(regattaMarkConfigurations.regattaConfigurationsByMark.values());

        boolean validCourseTemplateUsage = false;
        RepeatablePart optionalRepeatablePart = null;
        Integer numberOfLaps = null;
        final String name = course.getName();
        final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations = new HashMap<>();
        final Map<MarkConfiguration, String> roleMappingBasedOnCourseTemplate = new HashMap<>();
        if (courseTemplateOrNull != null) {
            validCourseTemplateUsage = true;
            final Iterable<WaypointTemplate> effectiveCourseSequence;
            if (courseTemplateOrNull.hasRepeatablePart()) {
                optionalRepeatablePart = courseTemplateOrNull.getRepeatablePart();
                final int numberOfWaypointsInTemplate = Util.size(courseTemplateOrNull.getWaypointTemplates());
                final int numberOfWaypointsInCourse = Util.size(course.getWaypoints());
                final int lengthOfRepeatablePart = optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartEnd()
                        - optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartStart();
                final int lengthOfNonRepeatablePart = numberOfWaypointsInTemplate - lengthOfRepeatablePart;
                final int lengthOfRepetitions = numberOfWaypointsInCourse - lengthOfNonRepeatablePart;
                if (lengthOfRepetitions % lengthOfRepeatablePart == 0) {
                    numberOfLaps = lengthOfRepetitions / lengthOfRepeatablePart + 1;
                    effectiveCourseSequence = optionalRepeatablePart.createSequence(numberOfLaps,
                            courseTemplateOrNull.getWaypointTemplates());
                } else {
                    validCourseTemplateUsage = false;
                    effectiveCourseSequence = courseTemplateOrNull.getWaypointTemplates();
                }
            } else {
                effectiveCourseSequence = courseTemplateOrNull.getWaypointTemplates();
            }
            if (validCourseTemplateUsage) {
                final Iterator<WaypointTemplate> waypointTemplateIterator = effectiveCourseSequence.iterator();
                final Iterator<Waypoint> waypointIterator = course.getWaypoints().iterator();
                while (waypointTemplateIterator.hasNext() && validCourseTemplateUsage) {
                    final WaypointTemplate waypointTemplate = waypointTemplateIterator.next();
                    final Iterable<MarkTemplate> markTemplatesOfControlPoint = waypointTemplate
                            .getControlPointTemplate().getMarks();
                    final Waypoint waypoint = waypointIterator.next();
                    final Iterable<Mark> marksOfControlPoint = waypoint.getControlPoint().getMarks();
                    if (Util.size(markTemplatesOfControlPoint) != Util.size(marksOfControlPoint)) {
                        validCourseTemplateUsage = false;
                        break;
                    }
                    final Iterator<MarkTemplate> markTemplateIterator = markTemplatesOfControlPoint.iterator();
                    final Iterator<Mark> markIterator = marksOfControlPoint.iterator();
                    while (markTemplateIterator.hasNext()) {
                        final MarkTemplate markTemplate = markTemplateIterator.next();
                        final Mark mark = markIterator.next();
                        final String roleForMarkTempalte = courseTemplateOrNull.getAssociatedRoles().get(markTemplate);
                        if (roleForMarkTempalte == null) {
                            validCourseTemplateUsage = false;
                            break;
                        }
                        String roleForMark = course.getAssociatedRoles().get(mark);
                        if (roleForMark == null) {
                            roleForMark = mark.getShortName();
                        }
                        if (!Util.equalsWithNull(roleForMarkTempalte, roleForMark)) {
                            validCourseTemplateUsage = false;
                            break;
                        }
                        final RegattaMarkConfiguration regattaMarkConfiguration = regattaMarkConfigurations.regattaConfigurationsByMark
                                .get(mark);
                        markTemplatesToMarkConfigurations.putIfAbsent(markTemplate, regattaMarkConfiguration);
                        roleMappingBasedOnCourseTemplate.putIfAbsent(regattaMarkConfiguration, roleForMark);
                    }
                }
            }
        }

        final Map<MarkConfiguration, String> resultingRoleMapping;
        final List<WaypointWithMarkConfiguration> resultingWaypoints;

        if (validCourseTemplateUsage) {
            resultingRoleMapping = roleMappingBasedOnCourseTemplate;
            if (courseTemplateOrNull.hasRepeatablePart() && numberOfLaps == 1) {
                // In case of just 1 lap, it is possible that MarkTemplates are left unmapped
                for (int i = optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartStart(); i < optionalRepeatablePart
                        .getZeroBasedIndexOfRepeatablePartEnd(); i++) {
                    final WaypointTemplate waypointTemplate = Util.get(courseTemplateOrNull.getWaypointTemplates(), i);

                    for (final MarkTemplate markTemplate : waypointTemplate.getControlPointTemplate().getMarks()) {
                        markTemplatesToMarkConfigurations.computeIfAbsent(markTemplate, mt -> {

                            final MarkConfiguration markConfiguration = new MarkTemplateBasedMarkConfigurationImpl(
                                    markTemplate, null);

                            allMarkConfigurations.add(markConfiguration);
                            resultingRoleMapping.put(markConfiguration,
                                    courseTemplateOrNull.getAssociatedRoles().get(mt));
                            return markConfiguration;
                        });
                    }

                    replaceTemplateBasedConfigurationCandidatesBySuggestedPropertiesBasedConfigurationsForRoleMapping(
                            resultingRoleMapping, allMarkConfigurations, tagsToFilterMarkProperties,
                            course.getAssociatedRoles());
                }
            }
            resultingWaypoints = createWaypointConfigurationsWithMarkTemplateMapping(courseTemplateOrNull,
                    markTemplatesToMarkConfigurations);
        } else {
            resultingRoleMapping = new HashMap<>();
            resultingWaypoints = new ArrayList<>();
            for (Entry<Mark, String> markWithRole : course.getAssociatedRoles().entrySet()) {
                resultingRoleMapping.put(
                        regattaMarkConfigurations.regattaConfigurationsByMark.get(markWithRole.getKey()),
                        markWithRole.getValue());
            }
            for (Waypoint waypoint : course.getWaypoints()) {
                final ControlPoint controlPoint = waypoint.getControlPoint();
                final ControlPointWithMarkConfiguration resultingControlPoint;
                if (controlPoint instanceof Mark) {
                    final Mark mark = (Mark) controlPoint;
                    resultingControlPoint = regattaMarkConfigurations.regattaConfigurationsByMark.get(mark);
                } else {
                    final ControlPointWithTwoMarks markPairTemplate = (ControlPointWithTwoMarks) controlPoint;
                    final MarkConfiguration left = regattaMarkConfigurations.regattaConfigurationsByMark
                            .get(markPairTemplate.getLeft());
                    final MarkConfiguration right = regattaMarkConfigurations.regattaConfigurationsByMark
                            .get(markPairTemplate.getRight());
                    resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), right, left,
                            markPairTemplate.getShortName());
                }
                resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                        waypoint.getPassingInstructions()));
            }
        }

        return new CourseConfigurationImpl(courseTemplateOrNull, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, optionalRepeatablePart, numberOfLaps, name);
    }
    
    private void replaceTemplateBasedConfigurationCandidatesBySuggestedPropertiesBasedConfigurationsForRoleMapping(
            Map<MarkConfiguration, String> resultingRoleMapping, Set<MarkConfiguration> markConfigurationsToEdit,
            Iterable<String> tagsToFilterMarkProperties, Map<Mark, String> associatedRoles) {
        // find candidates for replacement of mark configuration
        final Map<MarkConfiguration, String> replacementCandidates = resultingRoleMapping.entrySet().stream()
                .filter(e -> e.getKey() instanceof MarkTemplateBasedMarkConfigurationImpl)
                .collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));

        Set<MarkTemplate> associatedTemplates = replacementCandidates.entrySet().stream()
                .map(e -> e.getKey().getOptionalMarkTemplate()).collect(Collectors.toSet());
        // determine matching MarkProperties to associate
        final Map<MarkTemplate, MarkProperties> suggestedMappings = new MarkTemplatesMarkPropertiesAssociater()
                .getSuggestions(associatedRoles, associatedTemplates,
                        sharedSailingData.getAllMarkProperties(tagsToFilterMarkProperties), false);

        // replace candidates if possible
        for (Map.Entry<MarkConfiguration, String> entr : replacementCandidates.entrySet()) {
            final MarkTemplate keyTemplate = entr.getKey().getOptionalMarkTemplate();
            if (suggestedMappings.containsKey(keyTemplate)) {
                final MarkProperties suggestedPropertiesMapping = suggestedMappings.get(keyTemplate);
                final MarkPropertiesBasedMarkConfigurationImpl newMarkPropertiesBasedConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(
                        suggestedPropertiesMapping, keyTemplate, /* optionalPositioning */ null, getPositioningIfAvailable(suggestedPropertiesMapping));

                resultingRoleMapping.remove(entr.getKey());
                resultingRoleMapping.put(newMarkPropertiesBasedConfiguration, entr.getValue());
                markConfigurationsToEdit.remove(entr.getKey());
                markConfigurationsToEdit.add(newMarkPropertiesBasedConfiguration);
            }
        }
    }

    private void replaceTemplateBasedConfigurationCandidatesBySuggestedPropertiesBasedConfigurations(Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurationsToReplace,
            Set<MarkConfiguration> markConfigurationsToEdit, Iterable<String> tagsToFilterMarkProperties,
            Map<MarkTemplate, String> associatedRoles) {
        // find candidates for replacement of mark configuration
        final Map<MarkTemplate, MarkConfiguration> replacementCandidates = markTemplatesToMarkConfigurationsToReplace.entrySet()
                .stream().filter(e -> e.getValue() instanceof MarkTemplateBasedMarkConfigurationImpl)
                .collect(Collectors.toMap(s -> s.getKey(), s -> s.getValue()));

        // determine matching MarkProperties to associate
        final Map<MarkTemplate, MarkProperties> suggestedMappings = new MarkTemplatesMarkPropertiesAssociater()
                .getSuggestions(associatedRoles, replacementCandidates.keySet(),
                        sharedSailingData.getAllMarkProperties(tagsToFilterMarkProperties));

        // replace candidates if possible
        for (Map.Entry<MarkTemplate, MarkConfiguration> entr : replacementCandidates.entrySet()) {
            final MarkTemplate keyTemplate = entr.getKey();
            if (suggestedMappings.containsKey(keyTemplate)) {
                final MarkProperties suggestedPropertiesMapping = suggestedMappings.get(keyTemplate);
                final MarkPropertiesBasedMarkConfigurationImpl newMarkPropertiesBasedConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(
                        suggestedPropertiesMapping, keyTemplate, /* optionalPositioning */ null, getPositioningIfAvailable(suggestedPropertiesMapping));

                markTemplatesToMarkConfigurationsToReplace.put(keyTemplate, newMarkPropertiesBasedConfiguration);
                markConfigurationsToEdit.remove(entr.getValue());
                markConfigurationsToEdit.add(newMarkPropertiesBasedConfiguration);
            }
        }
    }

    private Positioning getPositioningIfAvailable(Regatta regatta, Mark mark) {
        return CourseConfigurationBuilder.getPositioningIfAvailable(regatta, mark, positionResolver);
    }

    private Positioning getPositioningIfAvailable(MarkProperties markProperties) {
        return CourseConfigurationBuilder.getPositioningIfAvailable(markProperties, positionResolver);
    }

    private class MarkTemplatesMarkPropertiesAssociater {

        public Map<MarkTemplate, MarkProperties> getSuggestions(Map<Mark, String> roleMappingFromCourseContext,
                Iterable<MarkTemplate> markTemplates, Iterable<MarkProperties> filteredMarkPropertiesCandiates,
                boolean dummy) {
            Map<MarkTemplate, String> transformedRoleMapping = roleMappingFromCourseContext.entrySet().stream()
                    .map(e -> new Pair<>(
                            sharedSailingData.getMarkTemplateById(e.getKey().getOriginatingMarkTemplateIdOrNull()),
                            e.getValue()))
                    .filter(d -> d.getA() == null).collect(Collectors.toMap(d -> d.getA(), d -> d.getB()));
            return getSuggestions(transformedRoleMapping, markTemplates, filteredMarkPropertiesCandiates);
        }

        public Map<MarkTemplate, MarkProperties> getSuggestions(Map<MarkTemplate, String> roleMappingFromCourseContext,
                Iterable<MarkTemplate> markTemplates, Iterable<MarkProperties> filteredMarkPropertiesCandiates) {

            // build mappings for both directions MP->MT and MT->MP
            final Map<MarkProperties, Set<Pair<MarkTemplate, TimePoint>>> usedTemplatesWithTimeByMarkProperties = new HashMap<>();
            final Map<MarkTemplate, Set<Pair<MarkProperties, TimePoint>>> usedPropertiesWithTimeByMarkTemplate = new HashMap<>();

            // go through all mark properties to find the matching mark templates
            for (final MarkProperties properties : filteredMarkPropertiesCandiates) {
                usedTemplatesWithTimeByMarkProperties.put(properties, properties.getLastUsedTemplate().entrySet()
                        .stream()/* .filter(x -> Util.contains(markTemplates, x)) */
                        .map(k -> new Pair<>(k.getKey(), k.getValue())).collect(Collectors.toSet()));

                // find the matching mark template for this properties
                for (final MarkTemplate template : markTemplates) {
                    if (properties.getLastUsedTemplate().containsKey(template)) {
                        // add template use to reverse map
                        Util.addToValueSet(usedPropertiesWithTimeByMarkTemplate, template,
                                new Pair<>(properties, (TimePoint) properties.getLastUsedTemplate().get(template)));
                    }
                }
            }

            final Map<MarkTemplate, MarkProperties> markPropertiesByMarkTemplate = new HashMap<>();

            // find the matching properties for a given template (forward direction)
            for (final MarkTemplate template : usedPropertiesWithTimeByMarkTemplate.keySet()) {
                final Set<Pair<MarkProperties, TimePoint>> currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage = usedPropertiesWithTimeByMarkTemplate
                        .get(template);
                if (currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage.size() == 0) {
                    continue;
                }

                final MarkProperties propertiesResult;
                if (currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage.size() == 1) {
                    // 1:x mapping
                    propertiesResult = currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage.iterator().next()
                            .getA();
                } else {
                    // y:x mapping: reduce to 1:x

                    // A: prioritize by last role use
                    final Set<Pair<MarkProperties, TimePoint>> candidatesAfterRolePriorizationWithLastTemplateUsage = new HashSet<>();
                    String role = roleMappingFromCourseContext.get(template);
                    if (role == null) {
                        candidatesAfterRolePriorizationWithLastTemplateUsage
                                .addAll(currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage);
                    } else {
                        final Set<Pair<MarkProperties, TimePoint>> candidatesForRolePriorizationWithLastRoleUsage = new HashSet<>();
                        for (final Pair<MarkProperties, TimePoint> pair : currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage) {
                            if (pair.getA().getLastUsedRole().containsKey(role)) {
                                candidatesForRolePriorizationWithLastRoleUsage
                                        .add(new Pair<>(pair.getA(), pair.getA().getLastUsedRole().get(role)));
                            }
                        }

                        if (candidatesForRolePriorizationWithLastRoleUsage.size() == 0) {
                            candidatesAfterRolePriorizationWithLastTemplateUsage
                                    .addAll(currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage);
                        } else {
                            candidatesForRolePriorizationWithLastRoleUsage
                                    .forEach(c -> candidatesAfterRolePriorizationWithLastTemplateUsage
                                            .add(new Pair<>(c.getA(), findAssociatedTimepoint(c.getA(),
                                                    currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage))));
                        }
                    }

                    if (candidatesAfterRolePriorizationWithLastTemplateUsage.size() == 0) {
                        // no candidates found: use secondary priority metric on all entries
                        propertiesResult = findMostRecentUse(
                                currentMarkPropertiesSetForThisTemplateWithLastTemplateUsage);
                    } else if (candidatesAfterRolePriorizationWithLastTemplateUsage.size() == 1) {
                        // only one candidate remaining -> use this candidate
                        propertiesResult = candidatesAfterRolePriorizationWithLastTemplateUsage.iterator().next()
                                .getA();
                    } else {
                        // B: prioritize by last use for the already prioritized candidates
                        propertiesResult = findMostRecentUse(candidatesAfterRolePriorizationWithLastTemplateUsage);
                    }
                }

                // check reverse direction (find templates mapped to the found properties)
                final Set<Pair<MarkTemplate, TimePoint>> reverseMappings = usedTemplatesWithTimeByMarkProperties
                        .get(propertiesResult);

                final MarkTemplate mappedTemplate;
                if (reverseMappings.size() == 1) {
                    // 1:1 mapping
                    mappedTemplate = reverseMappings.iterator().next().getA();
                } else {
                    // y:1 mapping
                    // reduce to 1:1 mapping
                    // TODO: role-based scoring?
                    mappedTemplate = findMostRecentUse(reverseMappings);
                }

                markPropertiesByMarkTemplate.put(mappedTemplate, propertiesResult);
            }

            return markPropertiesByMarkTemplate;
        }

        private <T> TimePoint findAssociatedTimepoint(T t, Set<Pair<T, TimePoint>> set) {
            return set.stream().filter(p -> p.getA().equals(t)).collect(Collectors.toSet()).iterator().next().getB();
        }

        /** @return the most recent use */
        private <T> T findMostRecentUse(final Set<Pair<T, TimePoint>> lastUses) {
            TimePoint mostRecent = new MillisecondsTimePoint(0);
            T mostRecentValue = null;
            for (final Pair<T, TimePoint> e : lastUses) {
                if (e.getB().after(mostRecent)) {
                    mostRecent = e.getB();
                    mostRecentValue = e.getA();
                }
            }

            return mostRecentValue;
        }
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
        final Set<Mark> marksInCourse = new HashSet<>();

        public RegattaMarkConfigurations(CourseTemplate courseTemplate, Regatta regatta) {
            this(/* optionalCourse */ null, courseTemplate, regatta);
        }

        public RegattaMarkConfigurations(CourseBase optionalCourse, CourseTemplate courseTemplate, Regatta regatta) {
            if (optionalCourse != null) {
                for (Waypoint waypoint : optionalCourse.getWaypoints()) {
                    Util.addAll(waypoint.getMarks(), marksInCourse);
                }
            }

            for (RaceColumn raceColumn : regatta.getRaceColumns()) {
                for (Mark mark : raceColumn.getAvailableMarks()) {
                    final RegattaMarkConfiguration regattaMarkConfiguration = regattaConfigurationsByMark
                            .computeIfAbsent(mark,
                                    m -> createMarkConfigurationForRegattaMark(courseTemplate, regatta, m));

                    for (Fleet fleet : raceColumn.getFleets()) {
                        final TrackedRace trackedRaceOrNull = raceColumn.getTrackedRace(fleet);
                        if (trackedRaceOrNull != null && Util.contains(raceColumn.getCourseMarks(), mark)) {
                            TimePoint usage = trackedRaceOrNull.getStartOfRace();
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

                        final boolean existingContainedInCourse = marksInCourse.contains(rmc.getMark());
                        final boolean newContainedInCourse = marksInCourse.contains(regattaMarkConfiguration.getMark());

                        if (existingContainedInCourse && !newContainedInCourse) {
                            return rmc;
                        }
                        if (!existingContainedInCourse && newContainedInCourse) {
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
            final RegattaMarkConfiguration regattaMarkConfiguration = new RegattaMarkConfigurationImpl(mark,
                    /* optionalPositioning */ null, getPositioningIfAvailable(regatta, mark), markTemplate);
            return regattaMarkConfiguration;
        }
    }
}
