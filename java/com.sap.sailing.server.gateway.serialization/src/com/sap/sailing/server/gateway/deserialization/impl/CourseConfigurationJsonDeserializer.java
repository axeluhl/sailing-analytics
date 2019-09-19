package com.sap.sailing.server.gateway.deserialization.impl;

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
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;

public class CourseConfigurationJsonDeserializer implements JsonDeserializer<CourseConfiguration> {

    private final SharedSailingData sharedSailingData;
    private final CommonMarkPropertiesJsonDeserializer commonMarkPropertiesJsonDeserializer;
    private final JsonDeserializer<RepeatablePart> repeatablePartJsonDeserializer;
    private final Regatta regatta;
    private final CourseTemplate courseTemplate;

    public CourseConfigurationJsonDeserializer(final SharedSailingData sharedSailingData, final Regatta regatta,
            final CourseTemplate courseTemplate) {
        this.sharedSailingData = sharedSailingData;
        commonMarkPropertiesJsonDeserializer = new CommonMarkPropertiesJsonDeserializer();
        repeatablePartJsonDeserializer = new RepeatablePartJsonDeserializer();
        this.regatta = regatta;
        this.courseTemplate = courseTemplate;
    }

    @Override
    public CourseConfiguration deserialize(JSONObject json) throws JsonDeserializationException {
        // final String courseConfigurationName = (String) json.get(CourseConfigurationJsonSerializer.FIELD_NAME);

        CourseConfigurationBuilder builder = new CourseConfigurationBuilder(sharedSailingData, regatta, courseTemplate);

        final Map<UUID, MarkConfiguration> markConfigurationsByID = new HashMap<UUID, MarkConfiguration>();
        final JSONArray markConfigurationsJSON = (JSONArray) json
                .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATIONS);
        for (Object markConfigurationObject : markConfigurationsJSON) {
            final JSONObject markConfigurationJSON = (JSONObject) markConfigurationObject;
            final String markTemplateID = (String) markConfigurationJSON
                    .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID);
            final String markPropertiesID = (String) markConfigurationJSON
                    .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID);
            final String markID = (String) markConfigurationJSON
                    .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_ID);
            final CommonMarkProperties commonMarkProperties = commonMarkPropertiesJsonDeserializer.deserialize(json);
            // TODO add optionalPositioning
            final MarkConfiguration markConfiguration = builder.addMarkConfiguration(UUID.fromString(markTemplateID),
                    UUID.fromString(markPropertiesID), UUID.fromString(markID), commonMarkProperties, null);
            String roleName = (String) markConfigurationJSON
                    .get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE);
            if (roleName != null && !roleName.isEmpty()) {
                builder.setRole(markConfiguration, roleName);
            }
            // TODO: wichh ID???
            markConfigurationsByID.put(UUID.randomUUID(), markConfiguration);
        }

        final JSONArray wayPointsJSON = (JSONArray) json.get(CourseConfigurationJsonSerializer.FIELD_WAYPOINTS);
        for (Object waypointObject : wayPointsJSON) {
            final JSONObject waypointJSON = (JSONObject) waypointObject;
            final List<MarkConfiguration> resolvedMarkConfigurations = new ArrayList<>();
            final JSONArray markConfigurationIDs = (JSONArray) waypointJSON
                    .get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_MARK_CONFIGURATION_IDS);
            for (Object markConfigurationIdObject : markConfigurationIDs) {
                final MarkConfiguration resolvedMarkTemplate = markConfigurationsByID
                        .get(UUID.fromString(markConfigurationIdObject.toString()));
                if (resolvedMarkTemplate == null) {
                    throw new JsonDeserializationException("Mark configuration with ID " + markConfigurationIdObject
                            + " was not defined to be part of the course configuration");
                }
                resolvedMarkConfigurations.add(resolvedMarkTemplate);
            }
            final PassingInstruction passingInstruction = PassingInstruction.valueOf(
                    (String) waypointJSON.get(CourseConfigurationJsonSerializer.FIELD_WAYPOINT_PASSING_INSTRUCTION));
            if (resolvedMarkConfigurations.size() == 1) {
                builder.addWaypoint(resolvedMarkConfigurations.get(0), passingInstruction);
            } else if (resolvedMarkConfigurations.size() == 2) {
                // TODO: name?
                builder.addWaypoint(resolvedMarkConfigurations.get(0), resolvedMarkConfigurations.get(1),
                        /* name */ null, passingInstruction);
            } else {
                throw new JsonDeserializationException("Unexpected number of mark configurations found for waypoint");
            }
        }

        final JSONObject repeatablePartJSON = (JSONObject) json
                .get(CourseConfigurationJsonSerializer.FIELD_OPTIONAL_REPEATABLE_PART);
        if (repeatablePartJSON != null) {
            final RepeatablePart optionalRepeatablePart = repeatablePartJsonDeserializer
                    .deserialize(repeatablePartJSON);
            builder.setOptionalRepeatablePart(optionalRepeatablePart);
        }

        final Integer numberOfLaps = (Integer) json.get(CourseConfigurationJsonSerializer.FIELD_NUMBER_OF_LAPS);
        if (numberOfLaps != null) {
            builder.setNumberOfLaps(numberOfLaps);
        }

        final CourseConfiguration courseConfiguration = builder.build();
        return courseConfiguration;
    }

}
