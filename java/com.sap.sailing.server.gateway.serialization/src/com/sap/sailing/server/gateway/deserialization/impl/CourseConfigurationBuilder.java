package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.FixedPositioningImpl;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.RegattaMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.StoredDeviceIdentifierPositioningImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointWithMarkConfigurationImpl;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sse.common.WithID;

/**
 * Builder API for constructing a {@link CourseConfiguration}. Various domain objects like {@link MarkTemplate}s,
 * {@link Mark}s of {@link MarkProperties} need to get resolved when constructing a {@link CourseConfiguration} using
 * the information given by the UI or API. This builder helps to resolve those domain objects and validate the
 * configuration.
 */
public class CourseConfigurationBuilder {

    private final SharedSailingData sharedSailingData;
    private final Regatta optionalRegatta;
    private final CourseTemplate optionalCourseTemplate;
    private final String name;
    // TODO decide if we should combine markConfigurations and roleMapping to one field
    private Set<MarkConfiguration> markConfigurations = new HashSet<>();
    private Map<MarkConfiguration, String> associatedRoles = new HashMap<>();
    private List<WaypointWithMarkConfiguration> waypoints = new ArrayList<>();
    private RepeatablePart optionalRepeatablePart;
    private Integer numberOfLaps;
    private Map<MarkPairWithConfiguration, MarkPairWithConfiguration> markPairCache = new HashMap<>();
    private final Function<DeviceIdentifier, Position> positionResolver;

    public CourseConfigurationBuilder(SharedSailingData sharedSailingData, Regatta optionalRegatta,
            CourseTemplate optionalCourseTemplate, String name, Function<DeviceIdentifier, Position> positionResolver) {
        this.sharedSailingData = sharedSailingData;
        this.optionalRegatta = optionalRegatta;
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.name = name;
        this.positionResolver = positionResolver;
    }

    public MarkConfiguration addMarkConfiguration(UUID optionalMarkTemplateID, UUID optionalMarkPropertiesID,
            UUID optionalMarkID, CommonMarkProperties commonMarkProperties, StorablePositioning optionalPositioning,
            boolean storeToInventory) {
        final MarkConfiguration result;
        if (commonMarkProperties != null) {
            if (optionalMarkID != null) {
                throw new IllegalArgumentException(
                        "Freestyle mark configurations may not reference an existing regatta mark");
            }
            result = addFreestyleMarkConfiguration(optionalMarkTemplateID, optionalMarkPropertiesID,
                    commonMarkProperties, optionalPositioning, storeToInventory);
        } else if (optionalMarkID != null) {
            result = addRegattaMarkConfiguration(optionalMarkID, optionalPositioning, storeToInventory);
        } else if (optionalMarkPropertiesID != null) {
            result = addMarkPropertiesConfiguration(optionalMarkPropertiesID, optionalMarkTemplateID,
                    optionalPositioning, storeToInventory);
        } else if (optionalMarkTemplateID != null) {
            result = addMarkTemplateConfiguration(optionalMarkTemplateID, optionalPositioning, storeToInventory);
        } else {
            throw new IllegalArgumentException(
                    "Mark configuration could not be constructed due to missing specification");
        }
        markConfigurations.add(result);
        return result;
    }

    public MarkTemplateBasedMarkConfiguration addMarkTemplateConfiguration(UUID markTemplateID,
            StorablePositioning optionalPositioning, boolean storeToInventory) {
        final MarkTemplate resolvedMarkTemplate = resolveMarkTemplateByID(markTemplateID);
        if (resolvedMarkTemplate == null) {
            throw new IllegalStateException("Mark template with ID " + markTemplateID + " could not be resolved");
        }
        final MarkTemplateBasedMarkConfiguration result = new MarkTemplateBasedMarkConfigurationImpl(
                resolvedMarkTemplate, optionalPositioning, storeToInventory);
        markConfigurations.add(result);
        return result;
    }

    /**
     * First tries to resolve from the course template and then by ID using sharedSailingData. This allows users having
     * access to a course template to use all mark templates being included even if they don't have explicit read
     * permissions for those.
     */
    private MarkTemplate resolveMarkTemplateByID(UUID markTemplateID) {
        MarkTemplate resolvedMarkTemplate = null;
        if (optionalCourseTemplate != null) {
            for (MarkTemplate markTemplate : optionalCourseTemplate.getMarkTemplates()) {
                if (markTemplate.getId().equals(markTemplateID)) {
                    resolvedMarkTemplate = markTemplate;
                    break;
                }
            }
        }
        if (resolvedMarkTemplate == null) {
            resolvedMarkTemplate = sharedSailingData.getMarkTemplateById(markTemplateID);
        }
        return resolvedMarkTemplate;
    }

    public MarkPropertiesBasedMarkConfiguration addMarkPropertiesConfiguration(UUID markPropertiesID,
            UUID optionalMarkTemplateID, StorablePositioning optionalPositioning, boolean storeToInventory) {
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(markPropertiesID);
        if (resolvedMarkProperties == null) {
            throw new IllegalArgumentException(
                    "Mark properties with ID " + markPropertiesID + " could not be resolved");
        }
        final MarkTemplate resolvedMarkTemplate = optionalMarkTemplateID == null ? null
                : resolveMarkTemplateByID(optionalMarkTemplateID);
        final MarkPropertiesBasedMarkConfiguration result = new MarkPropertiesBasedMarkConfigurationImpl(
                resolvedMarkProperties, resolvedMarkTemplate, optionalPositioning,
                getPositioningIfAvailable(resolvedMarkProperties, positionResolver), storeToInventory);
        markConfigurations.add(result);
        return result;
    }

    public FreestyleMarkConfiguration addFreestyleMarkConfiguration(UUID optionalMarkTemplateID,
            UUID optionalMarkPropertiesID, CommonMarkProperties commonMarkProperties, StorablePositioning optionalPositioning,
            boolean storeToInventory) {
        final MarkTemplate resolvedMarkTemplate = optionalMarkTemplateID == null ? null
                : resolveMarkTemplateByID(optionalMarkTemplateID);
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(optionalMarkPropertiesID);
        // TODO decide if it is fine if we can't resolve a MarkTemplate or MarkProperties here because all appearance
        // properties are available. This vcould potentially cause a lack of tracking information if the MarkProperties
        // isn't available.
        final FreestyleMarkConfiguration result = new FreestyleMarkConfigurationImpl(resolvedMarkTemplate,
                resolvedMarkProperties, commonMarkProperties, optionalPositioning, resolvedMarkProperties == null ? null
                        : getPositioningIfAvailable(resolvedMarkProperties, positionResolver), storeToInventory);
        markConfigurations.add(result);
        return result;
    }

    public RegattaMarkConfiguration addRegattaMarkConfiguration(UUID markID, StorablePositioning optionalPositioning,
            boolean storeToInventory) {
        if (optionalRegatta == null) {
            throw new IllegalStateException();
        }
        for (RaceColumn raceColumn : optionalRegatta.getRaceColumns()) {
            for (Mark mark : raceColumn.getAvailableMarks()) {
                if (mark.getId().equals(markID)) {
                    final UUID markTemplateIdOrNull = mark.getOriginatingMarkTemplateIdOrNull();
                    final MarkTemplate markTemplateOrNull = markTemplateIdOrNull == null ? null
                            : resolveMarkTemplateByID(markTemplateIdOrNull);
                    final UUID markPropertiesIdOrNull = mark.getOriginatingMarkPropertiesIdOrNull();
                    final MarkProperties markPropertiesOrNull = markPropertiesIdOrNull == null ? null
                            : sharedSailingData.getMarkPropertiesById(markPropertiesIdOrNull);
                    final RegattaMarkConfiguration result = new RegattaMarkConfigurationImpl(mark, optionalPositioning,
                            getPositioningIfAvailable(optionalRegatta, mark, positionResolver), markTemplateOrNull,
                            markPropertiesOrNull, storeToInventory);
                    markConfigurations.add(result);
                    return result;
                }
            }
        }
        throw new IllegalArgumentException(
                "Mark " + markID + " could not be found in regatta " + optionalRegatta.getName());
    }

    public void addWaypoint(MarkConfiguration markConfiguration, PassingInstruction passingInstruction) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        waypoints.add(new WaypointWithMarkConfigurationImpl(markConfiguration, passingInstruction));
    }

    public void addWaypoint(MarkConfiguration leftMark, MarkConfiguration rightMark, String name,
            PassingInstruction passingInstruction, String shortNameForMarkPair) {
        if (!markConfigurations.contains(leftMark) || !markConfigurations.contains(rightMark)) {
            throw new IllegalArgumentException();
        }
        final MarkPairWithConfiguration markPair = markPairCache
                .computeIfAbsent(new MarkPairWithConfigurationImpl(name, leftMark, rightMark, shortNameForMarkPair), mp -> mp);
        waypoints.add(new WaypointWithMarkConfigurationImpl(markPair, passingInstruction));
    }

    public void setRole(MarkConfiguration markConfiguration, String roleName) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        associatedRoles.forEach((mc, existingRole) -> {
            if (roleName.equals(existingRole) && !mc.equals(markConfiguration)) {
                throw new IllegalArgumentException(
                        "Role name '" + roleName + "' is already used for another mark configuration");
            }
        });
        associatedRoles.put(markConfiguration, roleName);
    }

    public void setOptionalRepeatablePart(RepeatablePart optionalRepeatablePart) {
        this.optionalRepeatablePart = optionalRepeatablePart;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public CourseConfiguration build() {
        return new CourseConfigurationImpl(optionalCourseTemplate, markConfigurations, associatedRoles, waypoints,
                optionalRepeatablePart, numberOfLaps, name);
    }
    
    public static Positioning getPositioningIfAvailable(Regatta regatta, Mark mark, Function<DeviceIdentifier, Position> positionResolver) {
        final DeviceIdentifier identifier = findDeviceForMostRecentOrOngoingMapping(regatta, mark);

        final Positioning result;
        if (identifier != null) {
            final Position lastPositionOrNull = positionResolver.apply(identifier);
            if (PingDeviceIdentifier.TYPE.equals(identifier.getIdentifierType())) {
                result = new FixedPositioningImpl(lastPositionOrNull);
            } else {
                result = new StoredDeviceIdentifierPositioningImpl(identifier, lastPositionOrNull);
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * @return the device mapping from the set of {@code foundMappings} which is either ongoing or has the most recent
     *         end time point
     */
    private static DeviceIdentifier findDeviceForMostRecentOrOngoingMapping(
            final Regatta regatta, final Mark mark) {
        final DeviceMappingWithRegattaLogEvent<Mark> mostRecentOrOngoingMapping = findMostRecentOrOngoingMapping(regatta, mark);
        return mostRecentOrOngoingMapping == null ? null : mostRecentOrOngoingMapping.getDevice();
    }
    
    /**
     * @return the device mapping from the set of {@code foundMappings} which is either ongoing or has the most recent
     *         end time point
     */
    @SuppressWarnings("unchecked")
    public static DeviceMappingWithRegattaLogEvent<Mark> findMostRecentOrOngoingMapping(
            final Regatta regatta, final Mark mark) {
        final Map<WithID, List<DeviceMappingWithRegattaLogEvent<WithID>>> deviceMappings = new RegattaLogDeviceMappingFinder<>(
                regatta.getRegattaLog()).analyze();

        final List<DeviceMappingWithRegattaLogEvent<WithID>> foundMappings = deviceMappings.get(mark);
        DeviceMappingWithRegattaLogEvent<Mark> bestMatchingDeviceMapping = null;
        if (foundMappings != null) {
            for (DeviceMappingWithRegattaLogEvent<?> event : foundMappings) {
                if (bestMatchingDeviceMapping == null
                        || (event.getTimeRange().hasOpenEnd() && !bestMatchingDeviceMapping.getTimeRange().hasOpenEnd())
                        || (event.getTimeRange().hasOpenEnd() && bestMatchingDeviceMapping.getTimeRange().hasOpenEnd()
                                && event.getTimeRange().startsAfter(bestMatchingDeviceMapping.getTimeRange()))
                        || (!event.getTimeRange().hasOpenEnd() && !bestMatchingDeviceMapping.getTimeRange().hasOpenEnd()
                                && event.getTimeRange().endsAfter(bestMatchingDeviceMapping.getTimeRange()))) {
                    bestMatchingDeviceMapping = (DeviceMappingWithRegattaLogEvent<Mark>)event;
                }
            }
        }
        return bestMatchingDeviceMapping;
    }

    public static Positioning getPositioningIfAvailable(MarkProperties markProperties, Function<DeviceIdentifier, Position> positionResolver) {
        final Position fixedPosition = markProperties.getFixedPosition();
        if (fixedPosition != null) {
            return new FixedPositioningImpl(fixedPosition);
        }
        final DeviceIdentifier trackingDeviceIdentifier = markProperties.getTrackingDeviceIdentifier();
        if (trackingDeviceIdentifier != null) {
            final Position lastPositionOrNull = positionResolver.apply(trackingDeviceIdentifier);
            return new StoredDeviceIdentifierPositioningImpl(trackingDeviceIdentifier, lastPositionOrNull);
        }
        return null;
    }
}
