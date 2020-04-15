package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationRequestAnnotation;
import com.sap.sailing.domain.coursetemplate.MarkConfigurationRequestAnnotation.MarkRoleCreationRequest;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.impl.MarkConfigurationRequestAnnotationImpl.MarkRoleCreationRequestImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;
import com.sap.sailing.shared.server.SharedSailingData;

public class CourseConfigurationJsonDeserializer implements JsonDeserializer<CourseConfiguration<MarkConfigurationRequestAnnotation>> {

    private final SharedSailingData sharedSailingData;
    private final CommonMarkPropertiesJsonDeserializer commonMarkPropertiesJsonDeserializer;
    private final JsonDeserializer<RepeatablePart> repeatablePartJsonDeserializer;
    private final Regatta regatta;
    private final PositioningJsonDeserializer positioningJsonDeserializer;

    public CourseConfigurationJsonDeserializer(final SharedSailingData sharedSailingData,
            DeviceIdentifierJsonDeserializer deviceIdentifierDeserializer, final Regatta regatta) {
        this.sharedSailingData = sharedSailingData;
        commonMarkPropertiesJsonDeserializer = new CommonMarkPropertiesJsonDeserializer();
        repeatablePartJsonDeserializer = new RepeatablePartJsonDeserializer();
        positioningJsonDeserializer = new PositioningJsonDeserializer(deviceIdentifierDeserializer);
        this.regatta = regatta;
    }

    @Override
    public CourseConfiguration<MarkConfigurationRequestAnnotation> deserialize(JSONObject json) throws JsonDeserializationException {
        final String name = (String) json.get(CourseConfigurationJsonSerializer.FIELD_NAME);
        final String shortName = (String) json.get(CourseConfigurationJsonSerializer.FIELD_SHORT_NAME);
        final String optionalImageURLAsString = (String) json.get(CourseConfigurationJsonSerializer.FIELD_OPTIONAL_IMAGE_URL);
        URL optionalImageURL;
        try {
            optionalImageURL = optionalImageURLAsString == null ? null : new URL(optionalImageURLAsString);
        } catch (MalformedURLException e) {
            throw new JsonDeserializationException(e);
        }
        final String courseTemplateIdString = (String) json.get(CourseConfigurationJsonSerializer.FIELD_OPTIONAL_COURSE_TEMPLATE_UUID);
        CourseTemplate optionalCourseTemplate = null;
        if (courseTemplateIdString != null && !courseTemplateIdString.isEmpty()) {
            optionalCourseTemplate = sharedSailingData.getCourseTemplateById(UUID.fromString(courseTemplateIdString));
        }
        CourseConfigurationBuilder builder = new CourseConfigurationBuilder(sharedSailingData, regatta, optionalCourseTemplate,
                name, shortName, optionalImageURL);
        final Map<UUID, MarkConfiguration<MarkConfigurationRequestAnnotation>> markConfigurationsByID = new HashMap<>();
        final JSONArray markConfigurationsJSON = (JSONArray) json
                .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATIONS);
        if (markConfigurationsJSON != null) {
            for (Object markConfigurationObject : markConfigurationsJSON) {
                final JSONObject markConfigurationJSON = (JSONObject) markConfigurationObject;
                final String markTemplateID = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID);
                final String markPropertiesID = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID);
                final String markID = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_ID);
                final JSONObject freestylePropertiesObject = (JSONObject) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_FREESTYLE_PROPERTIES);
                final CommonMarkProperties optionalFreestyleProperties = freestylePropertiesObject == null ? null
                        : commonMarkPropertiesJsonDeserializer.deserialize(freestylePropertiesObject);
                final Object positioningObject = markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_POSITIONING);
                final Positioning positioning = positioningObject instanceof JSONObject
                        ? positioningJsonDeserializer.deserialize((JSONObject) positioningObject)
                        : null;
                final boolean storeToInventory = Boolean.TRUE.equals(markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_STORE_TO_INVENTORY));
                String markRoleNameOrNull = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE_NAME);
                String markRoleShortNameOrNull = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE_SHORT_NAME);
                final MarkRoleCreationRequest markRoleCreationRequestOrNull;
                if (markRoleNameOrNull != null) {
                    markRoleCreationRequestOrNull = new MarkRoleCreationRequestImpl(markRoleNameOrNull, markRoleShortNameOrNull);
                } else {
                    markRoleCreationRequestOrNull = null;
                }
                final MarkConfiguration<MarkConfigurationRequestAnnotation> markConfiguration = builder.addMarkConfiguration(
                        markTemplateID != null ? UUID.fromString(markTemplateID) : null,
                        markPropertiesID != null ? UUID.fromString(markPropertiesID) : null,
                        markID != null ? UUID.fromString(markID) : null, optionalFreestyleProperties,
                                positioning, storeToInventory, markRoleCreationRequestOrNull);
                final String markRoleIdOrNullAsString = (String) markConfigurationJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE_ID);
                final UUID markRoleIdOrNull = markRoleIdOrNullAsString == null ? null : UUID.fromString(markRoleIdOrNullAsString);
                if (markRoleIdOrNull != null) {
                    builder.setRole(markConfiguration, markRoleIdOrNull);
                }
                markConfigurationsByID.put(
                        UUID.fromString((String) markConfigurationJSON
                                .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_ID)),
                        markConfiguration);
            }
        }
        final JSONArray wayPointsJSON = (JSONArray) json.get(CourseConfigurationJsonSerializer.FIELD_WAYPOINTS);
        if (wayPointsJSON != null) {
            for (Object waypointObject : wayPointsJSON) {
                final JSONObject waypointJSON = (JSONObject) waypointObject;
                final List<MarkConfiguration<MarkConfigurationRequestAnnotation>> resolvedMarkConfigurations = new ArrayList<>();
                final JSONArray markConfigurationIDs = (JSONArray) waypointJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_MARK_CONFIGURATION_IDS);
                for (Object markConfigurationIdObject : markConfigurationIDs) {
                    final MarkConfiguration<MarkConfigurationRequestAnnotation> resolvedMarkTemplate = markConfigurationsByID
                            .get(UUID.fromString(markConfigurationIdObject.toString()));
                    if (resolvedMarkTemplate == null) {
                        throw new JsonDeserializationException("Mark configuration with ID " + markConfigurationIdObject
                                + " was not defined to be part of the course configuration");
                    }
                    resolvedMarkConfigurations.add(resolvedMarkTemplate);
                }
                final PassingInstruction passingInstruction = PassingInstruction.valueOf((String) waypointJSON
                        .get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_PASSING_INSTRUCTION));
                if (resolvedMarkConfigurations.size() == 1) {
                    builder.addWaypoint(resolvedMarkConfigurations.get(0), passingInstruction);
                } else if (resolvedMarkConfigurations.size() == 2) {
                    String controlPointName = (String) waypointJSON
                            .get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_CONTROL_POINT_NAME);
                    String controlPointShortName = (String) waypointJSON
                            .get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_CONTROL_POINT_SHORT_NAME);
                    builder.addWaypoint(resolvedMarkConfigurations.get(0), resolvedMarkConfigurations.get(1),
                            controlPointName, passingInstruction, controlPointShortName);
                } else {
                    throw new JsonDeserializationException(
                            "Unexpected number of mark configurations found for waypoint");
                }
            }
        }
        final JSONObject repeatablePartJSON = (JSONObject) json
                .get(CourseConfigurationJsonSerializer.FIELD_OPTIONAL_REPEATABLE_PART);
        if (repeatablePartJSON != null) {
            final RepeatablePart optionalRepeatablePart = repeatablePartJsonDeserializer
                    .deserialize(repeatablePartJSON);
            builder.setOptionalRepeatablePart(optionalRepeatablePart);
        }
        final Number numberOfLapsNumber = (Number) json.get(CourseConfigurationJsonSerializer.FIELD_NUMBER_OF_LAPS);
        final Integer numberOfLaps = numberOfLapsNumber == null ? null : numberOfLapsNumber.intValue();
        if (numberOfLaps != null) {
            builder.setNumberOfLaps(numberOfLaps);
        }
        final CourseConfiguration<MarkConfigurationRequestAnnotation> courseConfiguration = builder.build();
        return courseConfiguration;
    }
}
