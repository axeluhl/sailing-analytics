package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.FixedPositioning;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationRequestAnnotation;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationResponseAnnotation;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkProperties;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplateBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.PositioningVisitor;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.TrackingDeviceBasedPositioning;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.impl.CourseConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.FreestyleMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkConfigurationRequestAnnotationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkConfigurationResponseAnnotationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairWithConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPropertiesBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateBasedMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.RegattaMarkConfigurationImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointWithMarkConfigurationImpl;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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
    private final String shortName;
    private final URL optionalImageUrl;
    private Set<MarkConfiguration<MarkConfigurationRequestAnnotation>> markConfigurations = new HashSet<>();
    private Map<MarkConfiguration<MarkConfigurationRequestAnnotation>, MarkRole> associatedRoles = new HashMap<>();
    private List<WaypointWithMarkConfiguration<MarkConfigurationRequestAnnotation>> waypoints = new ArrayList<>();
    private RepeatablePart optionalRepeatablePart;
    private Integer numberOfLaps;
    private Map<MarkPairWithConfiguration<MarkConfigurationRequestAnnotation>, MarkPairWithConfiguration<MarkConfigurationRequestAnnotation>> markPairCache = new HashMap<>();

    public CourseConfigurationBuilder(SharedSailingData sharedSailingData, Regatta optionalRegatta,
            CourseTemplate optionalCourseTemplate, String name, String shortName, URL optionalImageUrl) {
        this.sharedSailingData = sharedSailingData;
        this.optionalRegatta = optionalRegatta;
        this.optionalCourseTemplate = optionalCourseTemplate;
        this.name = name;
        this.shortName = shortName;
        this.optionalImageUrl = optionalImageUrl;
    }

    public MarkConfiguration<MarkConfigurationRequestAnnotation> addMarkConfiguration(UUID optionalMarkTemplateID, UUID optionalMarkPropertiesID,
            UUID optionalMarkID, CommonMarkProperties commonMarkProperties, Positioning optionalPositioning,
            boolean storeToInventory) {
        final MarkConfiguration<MarkConfigurationRequestAnnotation> result;
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

    public MarkTemplateBasedMarkConfiguration<MarkConfigurationRequestAnnotation> addMarkTemplateConfiguration(UUID markTemplateID,
            Positioning optionalPositioning, boolean storeToInventory) {
        final MarkTemplate resolvedMarkTemplate = resolveMarkTemplateByID(markTemplateID);
        if (resolvedMarkTemplate == null) {
            throw new IllegalStateException("Mark template with ID " + markTemplateID + " could not be resolved");
        }
        final MarkTemplateBasedMarkConfiguration<MarkConfigurationRequestAnnotation> result = new MarkTemplateBasedMarkConfigurationImpl<>(
                resolvedMarkTemplate, new MarkConfigurationRequestAnnotationImpl(storeToInventory, optionalPositioning));
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
            resolvedMarkTemplate = optionalCourseTemplate
                    .getMarkTemplateByIdIfContainedInCourseTemplate(markTemplateID);
        }
        if (resolvedMarkTemplate == null) {
            resolvedMarkTemplate = sharedSailingData.getMarkTemplateById(markTemplateID);
        }
        return resolvedMarkTemplate;
    }
    
    private MarkRole resolveMarkRoleByID(UUID markRoleID) {
        MarkRole resolvedMarkRole = null;
        if (optionalCourseTemplate != null) {
            resolvedMarkRole = optionalCourseTemplate.getMarkRoleByIdIfContainedInCourseTemplate(markRoleID);
        }
        if (resolvedMarkRole == null) {
            resolvedMarkRole = sharedSailingData.getMarkRoleById(markRoleID);
        }
        return resolvedMarkRole;
    }

    public MarkPropertiesBasedMarkConfiguration<MarkConfigurationRequestAnnotation> addMarkPropertiesConfiguration(UUID markPropertiesID,
            UUID optionalMarkTemplateID, Positioning optionalPositioning, boolean storeToInventory) {
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(markPropertiesID);
        if (resolvedMarkProperties == null) {
            throw new IllegalArgumentException(
                    "Mark properties with ID " + markPropertiesID + " could not be resolved");
        }
        final MarkTemplate resolvedMarkTemplate = optionalMarkTemplateID == null ? null
                : resolveMarkTemplateByID(optionalMarkTemplateID);
        final MarkPropertiesBasedMarkConfiguration<MarkConfigurationRequestAnnotation> result =
                new MarkPropertiesBasedMarkConfigurationImpl<>(
                resolvedMarkProperties, resolvedMarkTemplate,
                new MarkConfigurationRequestAnnotationImpl(storeToInventory, optionalPositioning));
        markConfigurations.add(result);
        return result;
    }

    public FreestyleMarkConfiguration<MarkConfigurationRequestAnnotation> addFreestyleMarkConfiguration(UUID optionalMarkTemplateID,
            UUID optionalMarkPropertiesID, CommonMarkProperties commonMarkProperties, Positioning optionalPositioning,
            boolean storeToInventory) {
        final MarkTemplate resolvedMarkTemplate = optionalMarkTemplateID == null ? null
                : resolveMarkTemplateByID(optionalMarkTemplateID);
        final MarkProperties resolvedMarkProperties = sharedSailingData.getMarkPropertiesById(optionalMarkPropertiesID);
        // TODO decide if it is fine if we can't resolve a MarkTemplate or MarkProperties here because all appearance
        // properties are available. This could potentially cause a lack of tracking information if the MarkProperties
        // isn't available.
        final FreestyleMarkConfiguration<MarkConfigurationRequestAnnotation> result = new FreestyleMarkConfigurationImpl<>(resolvedMarkTemplate,
                resolvedMarkProperties, commonMarkProperties, new MarkConfigurationRequestAnnotationImpl(storeToInventory, optionalPositioning));
        markConfigurations.add(result);
        return result;
    }

    public MarkConfiguration<MarkConfigurationRequestAnnotation> addRegattaMarkConfiguration(UUID markID, Positioning optionalPositioning,
            boolean storeToInventory) {
        if (optionalRegatta == null) {
            throw new IllegalStateException("Require a valid regatta in CourseConfigurationBuilder to add regatta mark with ID "+markID);
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
                    final RegattaMarkConfiguration<MarkConfigurationRequestAnnotation> result = new RegattaMarkConfigurationImpl<>(
                            mark, new MarkConfigurationRequestAnnotationImpl(storeToInventory, optionalPositioning), markTemplateOrNull,
                            markPropertiesOrNull);
                    markConfigurations.add(result);
                    return result;
                }
            }
        }
        throw new IllegalArgumentException(
                "Mark " + markID + " could not be found in regatta " + optionalRegatta.getName());
    }

    public void addWaypoint(MarkConfiguration<MarkConfigurationRequestAnnotation> markConfiguration, PassingInstruction passingInstruction) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        waypoints.add(new WaypointWithMarkConfigurationImpl<>(markConfiguration, passingInstruction));
    }

    public void addWaypoint(MarkConfiguration<MarkConfigurationRequestAnnotation> leftMark,
            MarkConfiguration<MarkConfigurationRequestAnnotation> rightMark, String name,
            PassingInstruction passingInstruction, String shortNameForMarkPair) {
        if (!markConfigurations.contains(leftMark) || !markConfigurations.contains(rightMark)) {
            throw new IllegalArgumentException();
        }
        final MarkPairWithConfiguration<MarkConfigurationRequestAnnotation> markPair = markPairCache
                .computeIfAbsent(new MarkPairWithConfigurationImpl<MarkConfigurationRequestAnnotation>(name, leftMark,
                        rightMark, shortNameForMarkPair), mp -> mp);
        waypoints.add(new WaypointWithMarkConfigurationImpl<>(markPair, passingInstruction));
    }

    public void setRole(MarkConfiguration<MarkConfigurationRequestAnnotation> markConfiguration, UUID markRoleId, String roleName, String roleShortName) {
        if (!markConfigurations.contains(markConfiguration)) {
            throw new IllegalArgumentException();
        }
        final MarkRole candidate;
        if (markRoleId != null) {
            candidate = resolveMarkRoleByID(markRoleId);
        } else {
            candidate = new MarkRoleNameImpl(roleName, roleShortName); // FIXME annotate the MarkConfiguration with the role name/shortName
        }
        associatedRoles.forEach((mc, existingRole) -> {
            if (candidate.equals(existingRole) && !mc.equals(markConfiguration)) {
                throw new IllegalArgumentException(
                        "Role name '" + roleName + "' is already used for another mark configuration");
            }
        });
        associatedRoles.put(markConfiguration, candidate);
    }

    public void setOptionalRepeatablePart(RepeatablePart optionalRepeatablePart) {
        this.optionalRepeatablePart = optionalRepeatablePart;
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    public CourseConfiguration<MarkConfigurationRequestAnnotation> build() {
        return new CourseConfigurationImpl<MarkConfigurationRequestAnnotation>(optionalCourseTemplate, markConfigurations,
                associatedRoles, waypoints, optionalRepeatablePart, numberOfLaps, name, shortName, optionalImageUrl);
    }
    
    public static MarkConfigurationResponseAnnotation getPositioningIfAvailable(Regatta regatta,
            TrackedRace optionalRace, Mark mark, Function<DeviceIdentifier, Position> positionResolver) {
        return createMarkConfigurationResponseAnnotation(optionalRace, mark, positionResolver,
                findAllDevicesAndMappedRangesForMark(regatta, mark));
    }
    
    private static MarkConfigurationResponseAnnotation createMarkConfigurationResponseAnnotation(
            Function<DeviceIdentifier, Position> positionResolver, final DeviceIdentifier identifier) {
        final Position position;
        if (identifier != null) {
            position = positionResolver.apply(identifier);
        } else {
            position = null;
        }
        return new MarkConfigurationResponseAnnotationImpl(position, identifier);
    }

    private static MarkConfigurationResponseAnnotation createMarkConfigurationResponseAnnotation(
            TrackedRace optionalRace, Mark mark,
            Function<DeviceIdentifier, Position> positionResolver,
            Iterable<Pair<DeviceIdentifier, TimeRange>> deviceMappings) {
        Position position = null;
        if (optionalRace != null) {
            final GPSFixTrack<Mark, GPSFix> trackForMark = optionalRace.getTrack(mark);
            if (trackForMark != null) {
                final GPSFix lastFixOrNull = trackForMark.getLastFixAtOrBefore(MillisecondsTimePoint.now());
                if (lastFixOrNull != null) {
                    position = lastFixOrNull.getPosition();
                }
            }
        }
        if (position == null) {
            final Pair<DeviceIdentifier,TimeRange> mostRecentOrOngoingMapping = findMostRecentOrOngoingMapping(deviceMappings);
            if (mostRecentOrOngoingMapping != null) {
                position = positionResolver.apply(mostRecentOrOngoingMapping.getA());
            }
        }
        return new MarkConfigurationResponseAnnotationImpl(position, deviceMappings);
    }
    
    public static Pair<DeviceIdentifier, TimeRange> findMostRecentOrOngoingMapping(
            Iterable<Pair<DeviceIdentifier, TimeRange>> deviceMappings) {
        Pair<DeviceIdentifier, TimeRange> bestMatchingDeviceMapping = null;
        for (Pair<DeviceIdentifier, TimeRange> deviceMapping : deviceMappings) {
            if (bestMatchingDeviceMapping == null
                    || (deviceMapping.getB().hasOpenEnd() && !bestMatchingDeviceMapping.getB().hasOpenEnd())
                    || (deviceMapping.getB().hasOpenEnd() && bestMatchingDeviceMapping.getB().hasOpenEnd()
                            && deviceMapping.getB().startsAfter(bestMatchingDeviceMapping.getB()))
                    || (!deviceMapping.getB().hasOpenEnd() && !bestMatchingDeviceMapping.getB().hasOpenEnd()
                            && deviceMapping.getB().endsAfter(bestMatchingDeviceMapping.getB()))) {
                bestMatchingDeviceMapping = deviceMapping;
            }
        }
        return bestMatchingDeviceMapping;
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
    
    public static Iterable<Pair<DeviceIdentifier, TimeRange>> findAllDevicesAndMappedRangesForMark(
            final Regatta regatta, Mark mark) {
        final Map<WithID, List<DeviceMappingWithRegattaLogEvent<WithID>>> deviceMappings = new RegattaLogDeviceMappingFinder<>(
                regatta.getRegattaLog()).analyze();
        final List<DeviceMappingWithRegattaLogEvent<WithID>> foundMappings = deviceMappings.get(mark);
        final Iterable<Pair<DeviceIdentifier, TimeRange>> result;
        if (foundMappings == null) {
            result = Collections.emptySet();
        } else {
            result = foundMappings.stream().map(dm -> new Pair<>(dm.getDevice(), dm.getTimeRange())).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * For a {@link MarkProperties} object finds its {@link MarkProperties#getPositioningInformation()} and if it is {@code null}, returns {@code null};
     * otherwise, for a fixed position specification that position will be returned as the "last known position" and no device ID will be returned in this
     * case, although practically, the fixed position at some point may be stored in a regatta context using a "PING" device mapping.<p>
     * 
     * For device identifier-based positioning requests the device identifier will be returned and a query for the last known position of that device will
     * be launched using the {@code positionResolver}. If a last known position is found that way, it is included in the return value of this method.
     */
    public static MarkConfigurationResponseAnnotation getPositioningIfAvailable(Positioning positioning, Function<DeviceIdentifier, Position> positionResolver) {
        final MarkConfigurationResponseAnnotation result;
        if (positioning != null) {
            result = positioning.accept(new PositioningVisitor<MarkConfigurationResponseAnnotation>() {
                @Override
                public MarkConfigurationResponseAnnotation visit(FixedPositioning fixedPositioning) {
                    return new MarkConfigurationResponseAnnotationImpl(fixedPositioning.getFixedPosition());
                }

                @Override
                public MarkConfigurationResponseAnnotation visit(
                        TrackingDeviceBasedPositioning trackingDeviceBasedPositioning) {
                    return createMarkConfigurationResponseAnnotation(positionResolver, trackingDeviceBasedPositioning.getDeviceIdentifier());
                }
            });
        } else {
            result = null;
        }
        return result;
    }
}
