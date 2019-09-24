package com.sap.sailing.server.gateway.serialization.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseConfigurationJsonSerializer implements JsonSerializer<CourseConfiguration> {

    public static final String FIELD_NAME = "NAME";
    public static final String FIELD_MARK_CONFIGURATIONS = "markConfigurations";
    public static final String FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID = "markTemplateId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID = "markPropertiesId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_ID = "markId";
    public static final String FIELD_MARK_CONFIGURATION_COMMON_MARK_PROPERTIES = "commonMarkProperties";
    public static final Object FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE = "associatedRole";
    public static final Object FIELD_WAYPOINTS = "waypoints";
    public static final Object FIELD_WAYPOINT_CONTROL_POINT_NAME = "controlPointName";
    public static final Object FIELD_WAYPOINT_CONTROL_POINT_SHORT_NAME = "controlPointShortName";
    public static final Object FIELD_WAYPOINT_PASSING_INSTRUCTION = "passingInstruction";
    public static final Object FIELD_WAYPOINT_MARK_CONFIGURATION_IDS = "markConfigurationIds";
    public static final Object FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";
    public static final Object FIELD_NUMBER_OF_LAPS = "numberOfLaps";

    private final JsonSerializer<RepeatablePart> repeatablePartJsonSerializer;
    private final JsonSerializer<CommonMarkProperties> commonMarkPropertiesJsonSerializer;

    public CourseConfigurationJsonSerializer() {
        repeatablePartJsonSerializer = new RepeatablePartJsonSerializer();
        commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
    }

    @Override
    public JSONObject serialize(CourseConfiguration courseConfiguration) {
        final JSONObject result = new JSONObject();
        // TODO: name? //result.put(FIELD_NAME, "");

        final Map<MarkConfiguration, UUID> markConfigurationsToTempIdMap = new HashMap<>();
        final JSONArray markConfigurationsJSON = new JSONArray();
        for (final MarkConfiguration markConfiguration : courseConfiguration.getAllMarks()) {
            JSONObject markConfigurationsEntry = new JSONObject();
            if (markConfiguration.getOptionalMarkTemplate() != null) {
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID,
                        markConfiguration.getOptionalMarkTemplate().getId());
            }
            if (markConfiguration instanceof FreestyleMarkConfiguration) {
                final FreestyleMarkConfiguration freeStyleMarkConfiguration = (FreestyleMarkConfiguration) markConfiguration;
                if (freeStyleMarkConfiguration.getOptionalMarkProperties() != null) {
                    markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID,
                            freeStyleMarkConfiguration.getOptionalMarkProperties());
                }
                markConfigurationsEntry.putAll(commonMarkPropertiesJsonSerializer
                        .serialize(freeStyleMarkConfiguration.getFreestyleProperties()));
            } else if (markConfiguration instanceof MarkPropertiesBasedMarkConfiguration) {
                final MarkPropertiesBasedMarkConfiguration markPropertiesBasedMarkConfiguration = (MarkPropertiesBasedMarkConfiguration) markConfiguration;
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID,
                        markPropertiesBasedMarkConfiguration.getMarkProperties().getId().toString());
                if (markPropertiesBasedMarkConfiguration.getOptionalMarkTemplate() != null) {
                    markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID,
                            markPropertiesBasedMarkConfiguration.getOptionalMarkTemplate().getId().toString());
                }
            } else if (markConfiguration instanceof RegattaMarkConfiguration) {
                final RegattaMarkConfiguration regattaMarkConfiguration = (RegattaMarkConfiguration) markConfiguration;
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_ID,
                        regattaMarkConfiguration.getMark().getId().toString());
            }
            markConfigurationsEntry
                    .putAll(commonMarkPropertiesJsonSerializer.serialize(markConfiguration.getEffectiveProperties()));

            // TODO: associated role? markConfiguration.
            // TODO add optionalPositioning
            markConfigurationsToTempIdMap.put(markConfiguration, UUID.randomUUID());
            markConfigurationsJSON.add(markConfigurationsEntry);

        }
        result.put(FIELD_MARK_CONFIGURATIONS, markConfigurationsJSON);

        final JSONArray waypoints = new JSONArray();
        for (final WaypointWithMarkConfiguration waypoint : courseConfiguration.getNumberOfLaps() != null
                ? courseConfiguration.getWaypoints(courseConfiguration.getNumberOfLaps())
                : courseConfiguration.getWaypoints()) {
            final JSONObject waypointEntry = new JSONObject();
            waypointEntry.put(FIELD_WAYPOINT_PASSING_INSTRUCTION, waypoint.getPassingInstruction().name());
            final JSONArray markConfigurationIDs = new JSONArray();
            // TODO: markConfigurationIds
            waypoint.getControlPoint().getMarkConfigurations()
                    .forEach(mc -> markConfigurationsToTempIdMap.get(mc).toString());
            waypointEntry.put(FIELD_WAYPOINT_MARK_CONFIGURATION_IDS, markConfigurationIDs);
            waypointEntry.put(FIELD_WAYPOINT_CONTROL_POINT_NAME, waypoint.getControlPoint().getName());
            //waypointEntry.put(FIELD_WAYPOINT_CONTROL_POINT_SHORT_NAME, waypoint.getControlPoint().getShortName());
            waypoints.add(waypointEntry);
        }
        result.put(FIELD_WAYPOINTS, waypoints);

        if (courseConfiguration.hasRepeatablePart()) {
            result.put(FIELD_OPTIONAL_REPEATABLE_PART,
                    repeatablePartJsonSerializer.serialize(courseConfiguration.getRepeatablePart()));
        }
        result.put(FIELD_NUMBER_OF_LAPS, courseConfiguration.getNumberOfLaps());
        return result;
    }

}
