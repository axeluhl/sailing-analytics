package com.sap.sailing.server.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseConfigurationWithMarkRoles;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.FixedPositioning;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.IsMarkRole;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairTemplate;
import com.sap.sailing.domain.coursetemplate.MarkPairTemplate.MarkPairTemplateFactory;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.SmartphoneUUIDPositioning;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationWithMarkRolesImpl;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkRoleNameImpl;
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
import com.sap.sse.security.shared.impl.UserGroup;

public class CourseAndMarkConfigurationFactoryImpl implements CourseAndMarkConfigurationFactory {

    private static final Logger log = Logger.getLogger(CourseAndMarkConfigurationFactoryImpl.class.getName());
    
    private final ServiceTracker<SharedSailingData, SharedSailingData> sharedSailingDataTracker;
    private final SensorFixStore sensorFixStore;
    private final Function<DeviceIdentifier, Position> positionResolver;
    private final RaceLogResolver raceLogResolver;
    private final DomainFactory domainFactory;

    public CourseAndMarkConfigurationFactoryImpl(
            ServiceTracker<SharedSailingData, SharedSailingData> sharedSailingDataTracker,
            SensorFixStore sensorFixStore, RaceLogResolver raceLogResolver, DomainFactory domainFactory) {
        this.sharedSailingDataTracker = sharedSailingDataTracker;
        this.domainFactory = domainFactory;
        this.sensorFixStore = sensorFixStore;
        this.raceLogResolver = raceLogResolver;
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
    
    private SharedSailingData getSharedSailingData() {
        return sharedSailingDataTracker.getService();
    }

    private CourseTemplate resolveCourseTemplateSafe(CourseBase course) {
        CourseTemplate courseTemplateOrNull;
        try {
            courseTemplateOrNull = resolveCourseTemplate(course);
        } catch (org.apache.shiro.authz.AuthorizationException e) {
            // The call may fail due to required permissions.
            // In this case we just handle it as there is no CourseTemplate.
            courseTemplateOrNull = null;
        }
        return courseTemplateOrNull;
    }
    
    @Override
    public CourseTemplate resolveCourseTemplate(CourseBase course) {
        final CourseTemplate result;
        if (course.getOriginatingCourseTemplateIdOrNull() == null) {
            result = null;
        } else {
            result = getSharedSailingData().getCourseTemplateById(course.getOriginatingCourseTemplateIdOrNull());
        }
        return result;
    }
    
    private CourseConfigurationWithMarkRoles handleSaveToInventory(CourseConfiguration courseConfiguration, Optional<UserGroup> optionalNonDefaultGroupOwnership) {
        final Map<MarkConfiguration, MarkConfiguration> effectiveConfigurations = new HashMap<>();
        for (MarkConfiguration markConfiguration : courseConfiguration.getAllMarks()) {
            if (markConfiguration.isStoreToInventory()) {
                MarkProperties markPropertiesOrNull = markConfiguration.getOptionalMarkProperties();
                if (markPropertiesOrNull == null) {
                    // If no mark properties exist yet, a new one is created
                    markPropertiesOrNull = getSharedSailingData().createMarkProperties(markConfiguration.getEffectiveProperties(), /* tags */ Collections.emptySet(),
                            optionalNonDefaultGroupOwnership);
                } else {
                    // in the case of a MarkPropertiesBasedMarkConfiguration, the following call is expected to notice
                    // the identity between the mark properties object to update and the mark properties that constitute
                    // the effective properties and then skip the update.
                    getSharedSailingData().updateMarkProperties(markPropertiesOrNull.getId(), markConfiguration.getEffectiveProperties(), null, null, Collections.emptySet());
                }
                Positioning positioningOrNull = markConfiguration.getOptionalPositioning();
                if (positioningOrNull == null && markConfiguration instanceof RegattaMarkConfiguration) {
                    // Only for RegattaMarkConfiguration we can obtain the positioning directly from the Mark if available.
                    // In all other cases positioning is either already available for and existing MarkProperties or explicitly given.
                    positioningOrNull = markConfiguration.getEffectivePositioning();
                }
                if (positioningOrNull != null) {
                    final DeviceIdentifier deviceIdentifier = positioningOrNull.getDeviceIdentifier();
                    final Position fixedPosition = positioningOrNull.getPosition();
                    if (deviceIdentifier != null) {
                        getSharedSailingData().setTrackingDeviceIdentifierForMarkProperties(markPropertiesOrNull, deviceIdentifier);
                    } else {
                        getSharedSailingData().setFixedPositionForMarkProperties(markPropertiesOrNull, fixedPosition);
                    }
                }
                final MarkConfiguration effectiveMarkConfiguration;
                if (markConfiguration instanceof RegattaMarkConfiguration) {
                    final RegattaMarkConfiguration regattaMarkConfiguration = (RegattaMarkConfiguration) markConfiguration;
                    effectiveMarkConfiguration = new RegattaMarkConfigurationImpl(regattaMarkConfiguration.getMark(),
                            markConfiguration.getOptionalPositioning(), regattaMarkConfiguration.getEffectivePositioning(),
                            regattaMarkConfiguration.getOptionalMarkTemplate(), markPropertiesOrNull,
                            false);
                } else {
                    effectiveMarkConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(markPropertiesOrNull,
                            markConfiguration.getOptionalMarkTemplate(), markConfiguration.getOptionalPositioning(),
                            getPositioningIfAvailable(markPropertiesOrNull),
                            false);
                }
                effectiveConfigurations.put(markConfiguration, effectiveMarkConfiguration);
            } else {
                effectiveConfigurations.put(markConfiguration, markConfiguration);
            }
        }
        final CourseConfigurationToCourseConfigurationMapper waypointConfigurationMapper = new CourseConfigurationToCourseConfigurationMapper(
                courseConfiguration.getWaypoints(), courseConfiguration.getAssociatedRoles(),
                effectiveConfigurations);
        return new CourseConfigurationWithMarkRolesImpl(courseConfiguration.getOptionalCourseTemplate(),
                new HashSet<>(effectiveConfigurations.values()),
                ensureMarkRoles(waypointConfigurationMapper.explicitAssociatedRoles),
                waypointConfigurationMapper.effectiveWaypoints, courseConfiguration.getRepeatablePart(),
                courseConfiguration.getNumberOfLaps(), courseConfiguration.getName(),
                courseConfiguration.getOptionalImageURL());
    }

    private <C> Map<C, MarkRole> ensureMarkRoles(Map<C, IsMarkRole> explicitAssociatedRoles) {
        final Map<C, MarkRole> result = new HashMap<>();
        explicitAssociatedRoles.forEach((c, r) -> result.put(c, ensureMarkRole(r)));
        return result;
    }
    
    private MarkRole ensureMarkRole(IsMarkRole r) {
        final MarkRole mr;
        if (r instanceof MarkRole) {
            mr = (MarkRole) r;
        } else {
            mr = getSharedSailingData().createMarkRole(r.getName());
        }
        return mr;
    }
    
    private MarkRole resolveMarkRole(UUID markRoleId, CourseTemplate optionalCourseTemplate) {
        if (markRoleId == null) {
            return null;
        }
        MarkRole markRole = null;
        if (optionalCourseTemplate != null) {
            markRole = optionalCourseTemplate.getMarkRoleByIdIfContainedInCourseTemplate(markRoleId);
        }
        if (markRole == null) {
            markRole = getSharedSailingData().getMarkRoleById(markRoleId);
        }
        return markRole;
    }

    @Override
    public CourseConfiguration createCourseTemplateAndUpdatedConfiguration(
            final CourseConfiguration courseConfiguration, Iterable<String> tags, Optional<UserGroup> optionalNonDefaultGroupOwnership) {
        final CourseConfigurationWithMarkRoles courseConfigurationAfterInventory = handleSaveToInventory(courseConfiguration, optionalNonDefaultGroupOwnership);
        final Map<MarkConfiguration, MarkTemplate> markTemplatesByMarkConfigurations = new HashMap<>();
        final Map<MarkConfiguration, MarkConfiguration> marksConfigurationsMapping = new HashMap<>();
        for (MarkConfiguration markConfiguration : courseConfigurationAfterInventory.getAllMarks()) {
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
                    effectiveMarkTemplate = getSharedSailingData()
                            .createMarkTemplate(markConfiguration.getEffectiveProperties());
                }
                if (markConfiguration instanceof RegattaMarkConfiguration) {
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
                            effectiveMarkTemplate, markConfiguration.getOptionalPositioning(), getPositioningIfAvailable(markProperties),
                            /* storeToInventory */ false);
                } else if (markConfiguration instanceof FreestyleMarkConfiguration) {
                    final MarkProperties markPropertiesOrNull = ((FreestyleMarkConfiguration) markConfiguration)
                            .getOptionalMarkProperties();
                    if (markPropertiesOrNull != null) {
                        if (markPropertiesOrNull.hasEqualAppeareanceWith(effectiveMarkTemplate)) {
                            effectiveConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(markPropertiesOrNull,
                                    effectiveMarkTemplate, markConfiguration.getOptionalPositioning(), getPositioningIfAvailable(markPropertiesOrNull),
                                    /* storeToInventory */ false);
                        } else {
                            effectiveConfiguration = new FreestyleMarkConfigurationImpl(effectiveMarkTemplate,
                                    markPropertiesOrNull, effectiveMarkTemplate, markConfiguration.getOptionalPositioning(),
                                    getPositioningIfAvailable(markPropertiesOrNull),
                                    /* storeToInventory */ false);
                        }
                    } else {
                        effectiveConfiguration = new MarkTemplateBasedMarkConfigurationImpl(effectiveMarkTemplate,
                                markConfiguration.getOptionalPositioning(),
                                /* storeToInventory */ false);
                    }
                } else {
                    // Should never happen but could in case a new MarkConfiguration type is defined
                    throw new IllegalStateException("Unknown mark configuration type found");
                }
            }
            markTemplatesByMarkConfigurations.put(markConfiguration, effectiveMarkTemplate);
            marksConfigurationsMapping.put(markConfiguration, effectiveConfiguration);
        }

        final MarkPairTemplateFactory markPairTemplateFactory = new MarkPairTemplateFactory();
        
        final CourseConfigurationToCourseConfigurationMapper waypointConfigurationMapper = new CourseConfigurationToCourseConfigurationMapper(
                courseConfigurationAfterInventory.getWaypoints(), courseConfigurationAfterInventory.getAssociatedRoles(),
                marksConfigurationsMapping);
        recordUsagesForMarkProperties(waypointConfigurationMapper.effectiveWaypoints, waypointConfigurationMapper.allAssociatedRoles);

        final CourseSequenceReplacementMapper<ControlPointTemplate, MarkTemplate, WaypointTemplate> waypointTemplateMapper = new CourseSequenceReplacementMapper<ControlPointTemplate, MarkTemplate, WaypointTemplate>(
                courseConfigurationAfterInventory.getWaypoints(), courseConfigurationAfterInventory.getAssociatedRoles(),
                markTemplatesByMarkConfigurations) {
            @Override
            protected ControlPointTemplate createMarkPair(MarkTemplate left, MarkTemplate right, String name,
                    String shortName) {
                return markPairTemplateFactory.create(name, shortName, Arrays.asList(left, right));
            }

            @Override
            protected WaypointTemplate getOrCreateWaypoint(ControlPointTemplate controlPoint,
                    PassingInstruction passingInstruction) {
                return new WaypointTemplateImpl(controlPoint, passingInstruction);
            }
        };
        
        final CourseTemplate newCourseTemplate = getSharedSailingData().createCourseTemplate(courseConfigurationAfterInventory.getName(), new HashSet<>(markTemplatesByMarkConfigurations.values()),
                waypointTemplateMapper.effectiveWaypoints, ensureMarkRoles(waypointTemplateMapper.allAssociatedRoles), courseConfigurationAfterInventory.getRepeatablePart(),
                tags, courseConfigurationAfterInventory.getOptionalImageURL(),
                courseConfigurationAfterInventory.getNumberOfLaps());
        return new CourseConfigurationImpl(newCourseTemplate,
                new HashSet<>(marksConfigurationsMapping.values()),
                waypointConfigurationMapper.allAssociatedRoles, waypointConfigurationMapper.effectiveWaypoints, courseConfigurationAfterInventory.getRepeatablePart(),
                courseConfigurationAfterInventory.getNumberOfLaps(),
                courseConfigurationAfterInventory.getName(), courseConfigurationAfterInventory.getOptionalImageURL());
    }
    
    private void recordUsagesForMarkProperties(Iterable<WaypointWithMarkConfiguration> waypoints,
            Map<MarkConfiguration, ? extends IsMarkRole> associatedRoles) {
        for (MarkConfiguration markConfiguration : getAllMarkConfigurations(waypoints)) {
            final MarkProperties markProperties = getAssociatedMarkPropertiesIfAvailable(markConfiguration);
            if (markProperties != null) {
                final IsMarkRole role = getExplicitOrImplicitMarkRole(associatedRoles, markConfiguration);
                try {
                    getSharedSailingData().recordUsage(markProperties, ensureMarkRole(role));
                } catch (Exception e) {
                    log.log(Level.WARNING,
                            "Could not record usage for mark properties " + markProperties + " and role " + role,
                            e);
                }
                final MarkTemplate markTemplateOrNull = markConfiguration.getOptionalMarkTemplate();
                if (markTemplateOrNull != null) {
                    try {
                        getSharedSailingData().recordUsage(markTemplateOrNull, markProperties);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Could not record usage for mark properties " + markProperties
                                + " and mark template " + markTemplateOrNull, e);
                    }
                }
            }
        }
    }

    private IsMarkRole getExplicitOrImplicitMarkRole(Map<MarkConfiguration, ? extends IsMarkRole> associatedRoles, MarkConfiguration markConfiguration) {
        IsMarkRole effectiveRole = associatedRoles.get(markConfiguration);
        if (effectiveRole == null) {
            effectiveRole = new MarkRoleNameImpl(markConfiguration.getEffectiveProperties().getName());
        }
        return effectiveRole;
    }
    
    private Iterable<MarkConfiguration> getAllMarkConfigurations(Iterable<WaypointWithMarkConfiguration> waypoints) {
        final Set<MarkConfiguration> result = new HashSet<>();
        for (WaypointWithMarkConfiguration waypoint : waypoints) {
            Util.addAll(waypoint.getControlPoint().getMarkConfigurations(), result);
        }
        return result;
    }
    
    private MarkProperties getAssociatedMarkPropertiesIfAvailable(MarkConfiguration markConfiguration) {
        if (markConfiguration instanceof MarkPropertiesBasedMarkConfiguration) {
            return ((MarkPropertiesBasedMarkConfiguration) markConfiguration).getMarkProperties();
        } else if (markConfiguration instanceof RegattaMarkConfiguration) {
            return ((RegattaMarkConfiguration) markConfiguration).getOptionalMarkProperties();
        } else if (markConfiguration instanceof FreestyleMarkConfiguration) {
            return ((FreestyleMarkConfiguration) markConfiguration).getOptionalMarkProperties();
        }
        return null;
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
            boolean terminateOpenEndedDeviceMapping = false;
            if (deviceIdentifier != null) {
                if (existingDeviceMapping == null || !(deviceIdentifier.equals(existingDeviceMapping.getDevice()) && existingDeviceMapping.getTimeRange().hasOpenEnd())) {
                    regatta.getRegattaLog()
                            .add(new RegattaLogDeviceMarkMappingEventImpl(
                                    timePointForDefinitionOfMarksAndDeviceMappings, author, mark, deviceIdentifier,
                                    timePointForDefinitionOfMarksAndDeviceMappings, null));
                    terminateOpenEndedDeviceMapping = true;
                }
            } else if (position != null) {
                final boolean update;
                if (existingDeviceMapping != null) {
                    if (!PingDeviceIdentifier.TYPE.equals(existingDeviceMapping.getDevice().getIdentifierType())) {
                        update = true;
                        terminateOpenEndedDeviceMapping = true;
                    } else {
                        final Position lastPingedPositionOrNull = positionResolver.apply(existingDeviceMapping.getDevice());
                        update = lastPingedPositionOrNull == null || !lastPingedPositionOrNull.equals(position);
                    }
                } else {
                    update = true;
                }
                if (update) {
                    // TODO check if we can use com.sap.sailing.domain.racelogtracking.impl.RaceLogTrackingAdapterImpl.pingMark(RegattaLog, Mark, GPSFix, RacingEventService)
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
            if (terminateOpenEndedDeviceMapping && existingDeviceMapping != null && existingDeviceMapping.getTimeRange().hasOpenEnd()) {
                regatta.getRegattaLog()
                        .add(new RegattaLogCloseOpenEndedDeviceMappingEventImpl(
                                timePointForDefinitionOfMarksAndDeviceMappings, author,
                                existingDeviceMapping.getRegattaLogEvent().getId(),
                                timePointForDefinitionOfMarksAndDeviceMappings.minus(1)));
            }
        }
    }

    @Override
    public CourseBase createCourseFromConfigurationAndDefineMarksAsNeeded(Regatta regatta,
            final CourseConfiguration courseConfiguration, TimePoint timePointForDefinitionOfMarksAndDeviceMappings,
            AbstractLogEventAuthor author, Optional<UserGroup> optionalNonDefaultGroupOwnership) {
        final CourseConfigurationWithMarkRoles courseConfigurationAfterInventory = handleSaveToInventory(courseConfiguration, optionalNonDefaultGroupOwnership);
        recordUsagesForMarkProperties(courseConfiguration.getWaypoints(), courseConfiguration.getAssociatedRoles());
        final Map<MarkConfiguration, Mark> marksByMarkConfigurations = new HashMap<>();
        final RegattaLog regattaLog = regatta.getRegattaLog();
        for (MarkConfiguration markConfiguration : courseConfigurationAfterInventory.getAllMarks()) {
            if (markConfiguration instanceof RegattaMarkConfiguration) {
                final Mark mark = ((RegattaMarkConfiguration) markConfiguration).getMark();
                marksByMarkConfigurations.put(markConfiguration, mark);
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
                final Mark markToCreate = domainFactory.getOrCreateMark(UUID.randomUUID(),
                        effectiveProperties.getName(), effectiveProperties.getShortName(),
                        effectiveProperties.getType(), effectiveProperties.getColor(), effectiveProperties.getShape(),
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
        final CourseDataImpl course = new CourseDataImpl(courseConfigurationAfterInventory.getName(),
                courseConfigurationAfterInventory.getOptionalCourseTemplate() == null ? null
                        : courseConfigurationAfterInventory.getOptionalCourseTemplate().getId());
        final Iterable<WaypointWithMarkConfiguration> waypoints;
        if (courseConfigurationAfterInventory.hasRepeatablePart()) {
            if (courseConfigurationAfterInventory.getNumberOfLaps() == null) {
                throw new IllegalStateException("A course with repeatable part requires a lap count");
            }
            waypoints = courseConfigurationAfterInventory.getWaypoints(courseConfigurationAfterInventory.getNumberOfLaps());
        } else {
            waypoints = courseConfigurationAfterInventory.getWaypoints();
        }
        final CourseSequenceReplacementMapper<ControlPoint, Mark, Waypoint> courseSequenceMapper = new CourseSequenceReplacementMapper<ControlPoint, Mark, Waypoint>(
                waypoints, courseConfigurationAfterInventory.getAssociatedRoles(), marksByMarkConfigurations) {
            @Override
            protected ControlPointWithTwoMarks createMarkPair(Mark left, Mark right, String name, String shortName) {
                return new ControlPointWithTwoMarksImpl(UUID.randomUUID(), left, right, name, shortName);
            }

            @Override
            protected Waypoint getOrCreateWaypoint(ControlPoint controlPoint, PassingInstruction passingInstruction) {
                return new WaypointImpl(controlPoint, passingInstruction);
            }
        };
        courseSequenceMapper.effectiveWaypoints.forEach(wp -> course.addWaypoint(Util.size(course.getWaypoints()), wp));
        ensureMarkRoles(courseSequenceMapper.explicitAssociatedRoles)
                .forEach((m, mr) -> course.addRoleMapping(m, mr.getId()));
        return course;
    }

    @Override
    public CourseConfiguration createCourseConfigurationFromTemplate(CourseTemplate courseTemplate,
            Regatta optionalRegatta, Iterable<String> tagsToFilterMarkProperties) {
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();
        final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations = new HashMap<>();
        if (optionalRegatta != null) {
            // If we have a regatta context, we first try to get all existing marks and their association to
            // MarkTemplates from the regatta
            final Map<MarkTemplate, RegattaMarkConfiguration> markConfigurationsByMarkTemplate = new HashMap<>();
            final Map<Mark, RegattaMarkConfiguration> markConfigurationsByMark = new HashMap<>();
            final Map<RegattaMarkConfiguration, TimePoint> lastUsages = new HashMap<>();
            for (RaceColumn raceColumn : optionalRegatta.getRaceColumns()) {
                for (Mark mark : raceColumn.getAvailableMarks()) {
                    markConfigurationsByMark
                            .computeIfAbsent(mark,
                                    m -> createMarkConfigurationForRegattaMark(courseTemplate, optionalRegatta, m));
                }
            }
            final LastUsageBasedAssociater<RegattaMarkConfiguration, IsMarkRole> usagesForRole = new LastUsageBasedAssociater<>(
                    new HashSet<IsMarkRole>(courseTemplate.getAssociatedRoles().values()));
            for (RaceColumn raceColumn : optionalRegatta.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    final TrackedRace trackedRaceOrNull = raceColumn.getTrackedRace(fleet);
                    TimePoint usage = null;
                    if (trackedRaceOrNull != null) {
                        usage = trackedRaceOrNull.getStartOfRace();
                        if (usage == null) {
                            usage = trackedRaceOrNull.getStartOfTracking();
                        }
                    }
                    CourseBase courseOrNull = null;
                    final RaceDefinition raceDefinition = raceColumn.getRaceDefinition(fleet);
                    if (raceDefinition != null) {
                        courseOrNull = raceDefinition.getCourse();
                    }
                    if (courseOrNull == null || usage == null) {
                        final ReadonlyRaceState raceState = ReadonlyRaceStateImpl.getOrCreate(raceLogResolver, raceColumn.getRaceLog(fleet));
                        if (courseOrNull == null) {
                            courseOrNull = raceState.getCourseDesign();
                        }
                        if (usage == null) {
                            usage = raceState.getStartTime();
                        }
                    }
                    if (usage == null) {
                        usage = TimePoint.BeginningOfTime;
                    }
                    if (courseOrNull != null) {
                        final TimePoint effectiveUsageTP = usage;
                        for (Waypoint waypoint : courseOrNull.getWaypoints()) {
                            for (Mark mark : waypoint.getMarks()) {
                                final RegattaMarkConfiguration regattaMarkConfiguration = markConfigurationsByMark
                                        .computeIfAbsent(mark,
                                                m -> createMarkConfigurationForRegattaMark(courseTemplate, optionalRegatta, m));
                                lastUsages.compute(regattaMarkConfiguration,
                                        (mc, existingTP) -> (existingTP == null || existingTP.before(effectiveUsageTP))
                                        ? effectiveUsageTP
                                                : existingTP);
                                IsMarkRole roleName = resolveMarkRole(courseOrNull.getAssociatedRoles().get(mark), courseTemplate);
                                if (roleName == null) {
                                    roleName = new MarkRoleNameImpl(mark.getName());
                                }
                                usagesForRole.addUsage(regattaMarkConfiguration, roleName, effectiveUsageTP);
                            }
                        }
                    }
                }
            }
            Set<MarkTemplate> markTemplatesToAssociate = new HashSet<>();
            Util.addAll(courseTemplate.getMarkTemplates(), markTemplatesToAssociate);
            // Primary matching is based on the associated role.
            for (Iterator<MarkTemplate> iterator = markTemplatesToAssociate.iterator(); iterator.hasNext();) {
                MarkTemplate mt = iterator.next();
                MarkRole roleOrNull = courseTemplate.getAssociatedRoles().get(mt);
                if (roleOrNull != null) {
                    RegattaMarkConfiguration bestMatchForRole = usagesForRole.getBestMatchForT2(roleOrNull);
                    if (bestMatchForRole != null) {
                        iterator.remove();
                        markConfigurationsByMarkTemplate.put(mt, bestMatchForRole);
                        usagesForRole.removeT1(bestMatchForRole);
                        usagesForRole.removeT2(roleOrNull);
                    }
                }
            }
            // Marks that couldn't be matched by a role could be directly matched by an originating MarkTemplate and last usage
            for (RegattaMarkConfiguration regattaMarkConfiguration : usagesForRole.usagesByT1.keySet()) {
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
            allMarkConfigurations.addAll(markConfigurationsByMark.values());
            markTemplatesToMarkConfigurations.putAll(markConfigurationsByMarkTemplate);
        }
        for (MarkTemplate markTemplate : courseTemplate.getMarkTemplates()) {
            // For any MarkTemplate that wasn't resolved from the regatta, an explicit entry needs to get created
            markTemplatesToMarkConfigurations.computeIfAbsent(markTemplate, mt -> {
                final MarkConfiguration markConfiguration = new MarkTemplateBasedMarkConfigurationImpl(markTemplate,
                        null, /* storeToInventory */ false);
                allMarkConfigurations.add(markConfiguration);
                return markConfiguration;
            });
        }
        replaceTemplateBasedConfigurationCandidatesBySuggestedProperties(markTemplatesToMarkConfigurations, allMarkConfigurations, tagsToFilterMarkProperties,
                courseTemplate.getAssociatedRoles());
        final Map<MarkConfiguration, IsMarkRole> resultingRoleMapping = createRoleMappingWithMarkTemplateMapping(
                courseTemplate, markTemplatesToMarkConfigurations);
        final List<WaypointWithMarkConfiguration> resultingWaypoints = createWaypointConfigurationsWithMarkTemplateMapping(
                courseTemplate, markTemplatesToMarkConfigurations);
        return new CourseConfigurationImpl(courseTemplate, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, courseTemplate.getRepeatablePart(), courseTemplate.getDefaultNumberOfLaps(),
                courseTemplate.getName(), courseTemplate.getOptionalImageURL());
    }

    private Map<MarkConfiguration, IsMarkRole> createRoleMappingWithMarkTemplateMapping(CourseTemplate courseTemplate,
            final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations) {
        final Map<MarkConfiguration, IsMarkRole> resultingRoleMapping = new HashMap<>();
        for (Entry<MarkTemplate, MarkRole> markTemplateWithRole : courseTemplate.getAssociatedRoles().entrySet()) {
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
                resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), left, right,
                        markPairTemplate.getShortName());
            }
            resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                    waypointTemplate.getPassingInstruction()));
        }
        return resultingWaypoints;
    }

    @Override
    public CourseConfiguration createCourseConfigurationFromRegatta(CourseBase course, Regatta regatta,
            Iterable<String> tagsToFilterMarkProperties) {
        assert regatta != null;
        final Set<MarkConfiguration> allMarkConfigurations = new HashSet<>();
        final CourseTemplate courseTemplateOrNull = course == null ? null : resolveCourseTemplateSafe(course);
        final Map<Mark, RegattaMarkConfiguration> markConfigurationsByMark = createMarkConfigurationsForRegatta(regatta, courseTemplateOrNull);
        allMarkConfigurations.addAll(markConfigurationsByMark.values());
        final Map<MarkConfiguration, IsMarkRole> resultingRoleMapping = new HashMap<>();
        if (course != null) {
            for (Entry<Mark, UUID> entry : course.getAssociatedRoles().entrySet()) {
                resultingRoleMapping.put(markConfigurationsByMark.get(entry.getKey()),
                        resolveMarkRole(entry.getValue(), courseTemplateOrNull));
            }
        }
        String name = null;
        URL optionalImageURL = null;
        final List<WaypointWithMarkConfiguration> resultingWaypoints = new ArrayList<>();
        RepeatablePart optionalRepeatablePart = null;
        Integer numberOfLaps = null;
        boolean handledByTemplate = false;
        if (courseTemplateOrNull != null && course != null) {
            final CourseToCourseTemplateMapper mapper = new CourseToCourseTemplateMapper(course, courseTemplateOrNull, markConfigurationsByMark);
            if (mapper.validCourseTemplateUsage) {
                name = courseTemplateOrNull.getName();
                optionalImageURL = courseTemplateOrNull.getOptionalImageURL();
                final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations = new HashMap<>();
                markTemplatesToMarkConfigurations.putAll(mapper.markTemplatesToMarkConfigurations);
                if (!mapper.markTemplatesNotIncluded.isEmpty()) {
                    for (MarkTemplate markTemplate : mapper.markTemplatesNotIncluded) {
                        final MarkConfiguration markConfiguration = new MarkTemplateBasedMarkConfigurationImpl(
                                markTemplate, null, /* storeToInventory */ false);
                        markTemplatesToMarkConfigurations.put(markTemplate, markConfiguration);
                        resultingRoleMapping.put(markConfiguration,
                                courseTemplateOrNull.getOptionalAssociatedRole(markTemplate));
                    }
                    replaceTemplateBasedConfigurationCandidatesBySuggestedProperties(
                            markTemplatesToMarkConfigurations, allMarkConfigurations, tagsToFilterMarkProperties,
                            courseTemplateOrNull.getAssociatedRoles());
                }
                resultingWaypoints.addAll(createWaypointConfigurationsWithMarkTemplateMapping(courseTemplateOrNull,
                        markTemplatesToMarkConfigurations));
                numberOfLaps = mapper.numberOfLaps;
                optionalRepeatablePart = courseTemplateOrNull.getRepeatablePart();
                handledByTemplate = true;
            }
        }
        if (!handledByTemplate) {
            if (course != null) {
                for (Entry<Mark, UUID> markWithRole : course.getAssociatedRoles().entrySet()) {
                    resultingRoleMapping.put(
                            markConfigurationsByMark.get(markWithRole.getKey()),
                            resolveMarkRole(markWithRole.getValue(), courseTemplateOrNull));
                }
                for (Waypoint waypoint : course.getWaypoints()) {
                    final ControlPoint controlPoint = waypoint.getControlPoint();
                    final ControlPointWithMarkConfiguration resultingControlPoint;
                    if (controlPoint instanceof Mark) {
                        final Mark mark = (Mark) controlPoint;
                        resultingControlPoint = markConfigurationsByMark.get(mark);
                    } else {
                        final ControlPointWithTwoMarks markPairTemplate = (ControlPointWithTwoMarks) controlPoint;
                        final MarkConfiguration left = markConfigurationsByMark
                                .get(markPairTemplate.getLeft());
                        final MarkConfiguration right = markConfigurationsByMark
                                .get(markPairTemplate.getRight());
                        resultingControlPoint = new MarkPairWithConfigurationImpl(markPairTemplate.getName(), left, right,
                                markPairTemplate.getShortName());
                    }
                    resultingWaypoints.add(new WaypointWithMarkConfigurationImpl(resultingControlPoint,
                            waypoint.getPassingInstructions()));
                }
            }
        }
        return new CourseConfigurationImpl(courseTemplateOrNull, allMarkConfigurations, resultingRoleMapping,
                resultingWaypoints, optionalRepeatablePart, numberOfLaps, name, optionalImageURL);
    }
    
    private Map<Mark, RegattaMarkConfiguration> createMarkConfigurationsForRegatta(Regatta regatta, CourseTemplate courseTemplate) {
        final Map<Mark, RegattaMarkConfiguration> result = new HashMap<>();
        for (RaceColumn raceColumn : regatta.getRaceColumns()) {
            for (Mark mark : raceColumn.getAvailableMarks()) {
                result
                .computeIfAbsent(mark,
                        m -> createMarkConfigurationForRegattaMark(courseTemplate, regatta, m));
            }
        }
        return result;
    }
    
    private class CourseToCourseTemplateMapper {
        boolean validCourseTemplateUsage;
        Integer numberOfLaps;
        final Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurations = new HashMap<>();
        final Set<MarkTemplate> markTemplatesNotIncluded = new HashSet<>();
        
        public CourseToCourseTemplateMapper(CourseBase course, CourseTemplate courseTemplate, Map<Mark, RegattaMarkConfiguration> markConfigurationsByMark) {
            validCourseTemplateUsage = false;
            RepeatablePart optionalRepeatablePart = null;
            numberOfLaps = null;
            if (courseTemplate != null) {
                validCourseTemplateUsage = true;
                final Iterable<WaypointTemplate> effectiveCourseSequence;
                if (courseTemplate.hasRepeatablePart()) {
                    optionalRepeatablePart = courseTemplate.getRepeatablePart();
                    final int numberOfWaypointsInTemplate = Util.size(courseTemplate.getWaypointTemplates());
                    final int numberOfWaypointsInCourse = Util.size(course.getWaypoints());
                    final int lengthOfRepeatablePart = optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartEnd()
                            - optionalRepeatablePart.getZeroBasedIndexOfRepeatablePartStart();
                    final int lengthOfNonRepeatablePart = numberOfWaypointsInTemplate - lengthOfRepeatablePart;
                    final int lengthOfRepetitions = numberOfWaypointsInCourse - lengthOfNonRepeatablePart;
                    if (lengthOfRepetitions % lengthOfRepeatablePart == 0) {
                        numberOfLaps = lengthOfRepetitions / lengthOfRepeatablePart + 1;
                        effectiveCourseSequence = optionalRepeatablePart.createSequence(numberOfLaps,
                                courseTemplate.getWaypointTemplates());
                    } else {
                        validCourseTemplateUsage = false;
                        effectiveCourseSequence = courseTemplate.getWaypointTemplates();
                    }
                } else {
                    effectiveCourseSequence = courseTemplate.getWaypointTemplates();
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
                            final MarkRole roleForMarkTempalte = courseTemplate.getOptionalAssociatedRole(markTemplate);
                            if (roleForMarkTempalte == null) {
                                validCourseTemplateUsage = false;
                                break;
                            }
                            final MarkRole roleForMarkOrNull = resolveMarkRole(course.getAssociatedRoles().get(mark), courseTemplate);
                            if (!Util.equalsWithNull(roleForMarkTempalte, roleForMarkOrNull)) {
                                validCourseTemplateUsage = false;
                                break;
                            }
                            final RegattaMarkConfiguration regattaMarkConfiguration = markConfigurationsByMark
                                    .get(mark);
                            markTemplatesToMarkConfigurations.putIfAbsent(markTemplate, regattaMarkConfiguration);
                        }
                    }
                }
            }
            Util.addAll(courseTemplate.getMarkTemplatesInWaypoints(), markTemplatesNotIncluded);
            markTemplatesNotIncluded.removeAll(markTemplatesToMarkConfigurations.keySet());
        }
    }

    private void replaceTemplateBasedConfigurationCandidatesBySuggestedProperties(Map<MarkTemplate, MarkConfiguration> markTemplatesToMarkConfigurationsToReplace,
            Set<MarkConfiguration> markConfigurationsToEdit, Iterable<String> tagsToFilterMarkProperties,
            Map<MarkTemplate, MarkRole> associatedRoles) {
        // find candidates for replacement of mark configuration
        final Map<MarkTemplate, MarkTemplateBasedMarkConfiguration> replacementCandidates = markTemplatesToMarkConfigurationsToReplace.entrySet()
                .stream().filter(e -> e.getValue() instanceof MarkTemplateBasedMarkConfiguration)
                .collect(Collectors.toMap(s -> s.getKey(), s -> (MarkTemplateBasedMarkConfiguration)s.getValue()));
        
        final Set<MarkProperties> markPropertiesCandidates = new HashSet<>();
        Util.addAll(getSharedSailingData().getAllMarkProperties(tagsToFilterMarkProperties), markPropertiesCandidates);
        // Already included mark properties may not get associated again
        markPropertiesCandidates.removeAll(markConfigurationsToEdit.stream()
                .map(this::getAssociatedMarkPropertiesIfAvailable).filter(v -> v != null).collect(Collectors.toSet()));
        
        final LastUsageBasedAssociater<MarkProperties, MarkRole> roleBasedAssociater = new LastUsageBasedAssociater<>(
                replacementCandidates.keySet().stream().map(associatedRoles::get).filter(v -> v != null)
                        .collect(Collectors.toSet()));
        
        for (MarkProperties mp : markPropertiesCandidates) {
            roleBasedAssociater.addUsages(mp, mp.getLastUsedRole());
        }
        
        final Map<MarkTemplate, MarkProperties> suggestedMappings = new HashMap<>();
        
        for (Iterator<Entry<MarkTemplate, MarkTemplateBasedMarkConfiguration>> iterator = replacementCandidates.entrySet().iterator(); iterator
                .hasNext();) {
            Entry<MarkTemplate, MarkTemplateBasedMarkConfiguration> entry = iterator.next();
            final MarkRole roleName = associatedRoles.get(entry.getKey());
            if (roleName != null) {
                final MarkProperties bestMatchOrNull = roleBasedAssociater.getBestMatchForT2(roleName);
                if (bestMatchOrNull != null) {
                    suggestedMappings.put(entry.getKey(), bestMatchOrNull);
                    iterator.remove();
                    markPropertiesCandidates.remove(bestMatchOrNull);
                }
            }
        }
        
        // Trying to map the left over candidates by direct usages of the MarkTemplate with the MarkProperties
        final LastUsageBasedAssociater<MarkProperties, MarkTemplate> templateBasedAssociater = new LastUsageBasedAssociater<>(
                new HashSet<>(replacementCandidates.keySet()));
        
        for (MarkProperties mp : markPropertiesCandidates) {
            roleBasedAssociater.addUsages(mp, mp.getLastUsedRole());
        }
        
        for (Entry<MarkTemplate, MarkTemplateBasedMarkConfiguration> entry : replacementCandidates.entrySet()) {
            final MarkProperties bestMatchOrNull = templateBasedAssociater.getBestMatchForT2(entry.getKey());
            if (bestMatchOrNull != null) {
                suggestedMappings.put(entry.getKey(), bestMatchOrNull);
            }
        }

        // replace candidates if possible
        for (Map.Entry<MarkTemplate, MarkTemplateBasedMarkConfiguration> entr : replacementCandidates.entrySet()) {
            final MarkTemplate keyTemplate = entr.getKey();
            if (suggestedMappings.containsKey(keyTemplate)) {
                final MarkProperties suggestedPropertiesMapping = suggestedMappings.get(keyTemplate);
                final MarkPropertiesBasedMarkConfigurationImpl newMarkPropertiesBasedConfiguration = new MarkPropertiesBasedMarkConfigurationImpl(
                        suggestedPropertiesMapping, keyTemplate, /* optionalPositioning */ null, getPositioningIfAvailable(suggestedPropertiesMapping),
                        /* storeToInventory */ false);

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
            resolvedMarkTemplate = courseTemplate.getMarkTemplateByIdIfContainedInCourseTemplate(markTemplateID);
        }
        if (resolvedMarkTemplate == null) {
            try {
                resolvedMarkTemplate = getSharedSailingData().getMarkTemplateById(markTemplateID);
            } catch(Exception e) {
                // This call may fail due to missing permissions but should not prevent the user from creating a course for a regatta.
                // This is just the case, a regatta Mark is based on a MarkTemplate that is not part of the associated CourseTemplate.
                // In this case we don't require the MarkTemplate reference to reconstruct a CourseTemplate.
            }
        }
        return resolvedMarkTemplate;
    }

    private RegattaMarkConfiguration createMarkConfigurationForRegattaMark(CourseTemplate courseTemplate,
            Regatta regatta, Mark mark) {
        final UUID markTemplateIdOrNull = mark.getOriginatingMarkTemplateIdOrNull();
        final MarkTemplate markTemplateOrNull = markTemplateIdOrNull == null ? null : resolveMarkTemplateByID(courseTemplate, markTemplateIdOrNull);
        final UUID markPropertiesIdOrNull = mark.getOriginatingMarkPropertiesIdOrNull();
        final MarkProperties markPropertiesOrNull = markPropertiesIdOrNull == null ? null
                : getSharedSailingData().getMarkPropertiesById(markPropertiesIdOrNull);
        final RegattaMarkConfiguration regattaMarkConfiguration = new RegattaMarkConfigurationImpl(mark,
                /* optionalPositioning */ null, getPositioningIfAvailable(regatta, mark), markTemplateOrNull,
                markPropertiesOrNull, /* storeToInventory */ false);
        return regattaMarkConfiguration;
    }

    private abstract class CourseSequenceMapper<C, M extends C, W> {
        final Map<M, IsMarkRole> explicitAssociatedRoles = new HashMap<>();
        final Map<M, IsMarkRole> allAssociatedRoles = new HashMap<>();
        final List<W> effectiveWaypoints = new ArrayList<>();
        private final Iterable<WaypointWithMarkConfiguration> waypoints;
        private final Map<MarkConfiguration, ? extends IsMarkRole> existingRoleMapping;
        
        public CourseSequenceMapper(Iterable<WaypointWithMarkConfiguration> waypoints,
                Map<MarkConfiguration, ? extends IsMarkRole> existingRoleMapping) {
            this.waypoints = waypoints;
            this.existingRoleMapping = existingRoleMapping;
        }
        public void calculateEffectiveWaypoints() {
            // all preexisting explicit roles are added to the result
            for (Entry<MarkConfiguration, ? extends IsMarkRole> entry : existingRoleMapping.entrySet()) {
                explicitAssociatedRoles.put(mapMarkConfiguration(entry.getKey()), entry.getValue());
            }
            allAssociatedRoles.putAll(explicitAssociatedRoles);
            // Cache to allow reusing ControlPointWithTwoMarks objects that are based on the same MarkPairWithConfiguration
            final Map<MarkPairWithConfiguration, C> markPairCache = new HashMap<>();
            
            for (WaypointWithMarkConfiguration waypointWithMarkConfiguration : waypoints) {
                final ControlPointWithMarkConfiguration controlPointWithMarkConfiguration = waypointWithMarkConfiguration
                        .getControlPoint();

                if (controlPointWithMarkConfiguration instanceof MarkConfiguration) {
                    final MarkConfiguration markConfiguration = (MarkConfiguration) controlPointWithMarkConfiguration;
                    effectiveWaypoints.add(
                            getOrCreateWaypoint(mapMarkConfiguration(markConfiguration), waypointWithMarkConfiguration.getPassingInstruction()));
                } else {
                    final C controlPoint = markPairCache
                            .computeIfAbsent((MarkPairWithConfiguration) controlPointWithMarkConfiguration, mpwc -> {
                                final M left = mapMarkConfiguration(mpwc.getLeft());
                                final M right = mapMarkConfiguration(mpwc.getRight());
                                return createMarkPair(left, right, mpwc.getName(), mpwc.getShortName());
                            });
                    effectiveWaypoints.add(
                            getOrCreateWaypoint(controlPoint, waypointWithMarkConfiguration.getPassingInstruction()));
                }
            }
        }
        
        private M mapMarkConfiguration(MarkConfiguration markConfiguration) {
            final M mark = getOrCreateMarkReplacement(markConfiguration);
            if (mark == null) {
                throw new IllegalStateException("Non declared mark found in waypoint sequence");
            }
            // If an explicit role mapping isn't given -> default to the mark's name
            allAssociatedRoles.computeIfAbsent(mark, m -> getExplicitOrImplicitMarkRole(existingRoleMapping, markConfiguration));
            return mark;
        }
        
        protected abstract M getOrCreateMarkReplacement(MarkConfiguration markConfiguration);
        
        protected abstract C createMarkPair(M left, M right, String name, String shortName);
        
        protected abstract W getOrCreateWaypoint(C controlPoint, PassingInstruction passingInstruction);
    }
    
    private abstract class CourseSequenceReplacementMapper<C, M extends C, W> extends CourseSequenceMapper<C, M, W> {
        
        private final Map<MarkConfiguration, M> existingMapping;

        public CourseSequenceReplacementMapper(Iterable<WaypointWithMarkConfiguration> waypoints,
                Map<MarkConfiguration, ? extends IsMarkRole> existingRoleMapping, Map<MarkConfiguration, M> existingMapping) {
            super(waypoints, existingRoleMapping);
            this.existingMapping = existingMapping;
            calculateEffectiveWaypoints();
        }

        @Override
        protected M getOrCreateMarkReplacement(MarkConfiguration markConfiguration) {
            return existingMapping.get(markConfiguration);
        }
    }
    
    private class CourseConfigurationToCourseConfigurationMapper extends
            CourseSequenceReplacementMapper<ControlPointWithMarkConfiguration, MarkConfiguration, WaypointWithMarkConfiguration> {
        public CourseConfigurationToCourseConfigurationMapper(Iterable<WaypointWithMarkConfiguration> waypoints,
                Map<MarkConfiguration, ? extends IsMarkRole> existingRoleMapping,
                Map<MarkConfiguration, MarkConfiguration> existingMapping) {
            super(waypoints, existingRoleMapping, existingMapping);
        }

        @Override
        protected MarkPairWithConfiguration createMarkPair(MarkConfiguration left, MarkConfiguration right, String name,
                String shortName) {
            return new MarkPairWithConfigurationImpl(name, left, right, shortName);
        }

        @Override
        protected WaypointWithMarkConfiguration getOrCreateWaypoint(ControlPointWithMarkConfiguration controlPoint,
                PassingInstruction passingInstruction) {
            return new WaypointWithMarkConfigurationImpl(controlPoint, passingInstruction);
        }
    };
    
    private class LastUsageBasedAssociater<T1, T2> {
        
        private final Map<T1, Map<T2, TimePoint>> usagesByT1 = new HashMap<>();
        private final Map<T2, Map<T1, TimePoint>> usagesByT2 = new HashMap<>();
        private final Predicate<T2> t2Filter;
        
        public LastUsageBasedAssociater(Iterable<T2> t2Whitelist) {
            t2Filter = t2 -> Util.contains(t2Whitelist, t2);
        }
        
        public void addUsage(T1 t1, T2 t2, TimePoint lastUsage) {
            if (t2Filter.test(t2)) {
                insertOrUpdateUsage(usagesByT1, t1, t2, lastUsage);
                insertOrUpdateUsage(usagesByT2, t2, t1, lastUsage);
            }
        }
        
        public void addUsages(T1 t1, Map<T2, TimePoint> lastUsages) {
            for (Entry<T2, TimePoint> entry : lastUsages.entrySet()) {
                addUsage(t1, entry.getKey(), entry.getValue());
            }
        }
        
        private <K, V> void insertOrUpdateUsage(Map<K, Map<V, TimePoint>> usages, K key, V value, TimePoint timePoint) {
            Map<V, TimePoint> usagesForKey = usages.computeIfAbsent(key, k -> new HashMap<>());
            usagesForKey.compute(value,
                    (k, currentValue) -> (currentValue == null || timePoint.after(currentValue)) ? timePoint
                            : currentValue);
        }
        
        private <K, V> V getBestMatch(Map<K, Map<V, TimePoint>> forwardUsages, Map<V, Map<K, TimePoint>> backwardUsages, K keyToSearch) {
            V bestMatch = getBestMatchCandidate(forwardUsages, keyToSearch);
            if (bestMatch != null && ! keyToSearch.equals(getBestMatchCandidate(backwardUsages, bestMatch))) {
                // match would be better suited for another key
                bestMatch = null;
            }
            return bestMatch;
        }
        
        private <K, V> V getBestMatchCandidate(Map<K, Map<V, TimePoint>> usages, K keyToSearch) {
            final V result;
            final Map<V, TimePoint> usagesForT1 = usages.get(keyToSearch);
            if (usagesForT1 == null) {
                // No match at all
                result = null;
            } else {
                TimePoint bestMatchTP = null;
                V bestMatch = null;
                for (Map.Entry<V, TimePoint> entry : usagesForT1.entrySet()) {
                    if (bestMatchTP == null || bestMatchTP.after(entry.getValue())) {
                        bestMatchTP = entry.getValue();
                        bestMatch = entry.getKey();
                    } else if (bestMatchTP.compareTo(entry.getValue()) == 0) {
                        // ambiguous match
                        bestMatch = null;
                    }
                }
                result = bestMatch;
            }
            return result;
        }
        
        public T1 getBestMatchForT2(T2 t2) {
            return getBestMatch(usagesByT2, usagesByT1, t2);
        }
        
        private <K, V> void removeT1(T1 t1) {
            remove(usagesByT1, usagesByT2, t1);
        }
        
        private <K, V> void removeT2(T2 t2) {
            remove(usagesByT2, usagesByT1, t2);
        }
        
        private <K, V> void remove(Map<K, Map<V, TimePoint>> forwardUsages, Map<V, Map<K, TimePoint>> backwardUsages, K keyToRemove) {
            final Map<V, TimePoint> associatedUses = forwardUsages.remove(keyToRemove);
            if (associatedUses != null) {
                for (V associatedValue : associatedUses.keySet()) {
                    final Map<K, TimePoint> usesForValue = backwardUsages.get(associatedValue);
                    if (usesForValue != null) {
                        usesForValue.remove(keyToRemove);
                        if (usesForValue.isEmpty()) {
                            backwardUsages.remove(associatedValue);
                        }
                    }
                }
            }
        }
    }
}
