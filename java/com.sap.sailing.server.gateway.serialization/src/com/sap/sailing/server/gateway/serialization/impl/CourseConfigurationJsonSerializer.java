package com.sap.sailing.server.gateway.serialization.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.FreestyleMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPairWithConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkPropertiesBasedMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.RegattaMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseConfigurationJsonSerializer implements JsonSerializer<CourseConfiguration> {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_OPTIONAL_COURSE_TEMPLATE_UUID = "courseTemplateId";
    public static final String FIELD_MARK_CONFIGURATIONS = "markConfigurations";
    public static final String FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID = "markTemplateId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID = "markPropertiesId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_ID = "markId";
    public static final String FIELD_MARK_CONFIGURATION_EFFECTIVE_PROPERTIES = "effectiveProperties";
    public static final String FIELD_MARK_CONFIGURATION_FREESTYLE_PROPERTIES = "freestyleProperties";
    public static final String FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE = "associatedRole";
    public static final String FIELD_MARK_CONFIGURATION_POSITIONING = "positioning";
    public static final String FIELD_MARK_CONFIGURATION_EFFECTIVE_POSITIONING = "effectivePositioning";
    public static final String FIELD_MARK_CONFIGURATION_STORE_TO_INVENTORY = "storeToInventory";
    public static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_WAYPOINT_CONTROL_POINT_NAME = "controlPointName";
    public static final String FIELD_WAYPOINT_CONTROL_POINT_SHORT_NAME = "controlPointShortName";
    public static final String FIELD_WAYPOINT_PASSING_INSTRUCTION = "passingInstruction";
    public static final String FIELD_WAYPOINT_MARK_CONFIGURATION_IDS = "markConfigurationIds";
    public static final String FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";
    public static final String FIELD_NUMBER_OF_LAPS = "numberOfLaps";
    public static final String FIELD_MARK_CONFIGURATION_ID = "id";

    private final JsonSerializer<RepeatablePart> repeatablePartJsonSerializer;
    private final JsonSerializer<CommonMarkProperties> commonMarkPropertiesJsonSerializer;
    private final JsonSerializer<Positioning> positioningJsonSerializer;
    private final JsonSerializer<StorablePositioning> storablePositioningJsonSerializer;

    public CourseConfigurationJsonSerializer() {
        repeatablePartJsonSerializer = new RepeatablePartJsonSerializer();
        commonMarkPropertiesJsonSerializer = new CommonMarkPropertiesJsonSerializer();
        positioningJsonSerializer = new PositioningJsonSerializer();
        storablePositioningJsonSerializer = new StorablePositioningJsonSerializer();
    }

    @Override
    public JSONObject serialize(CourseConfiguration courseConfiguration) {
        final JSONObject result = new JSONObject();
        result.put(FIELD_NAME, courseConfiguration.getName());
        if (courseConfiguration.getOptionalCourseTemplate() != null) {
            result.put(FIELD_OPTIONAL_COURSE_TEMPLATE_UUID,
                    courseConfiguration.getOptionalCourseTemplate().getId().toString());
        }
        final Map<MarkConfiguration, UUID> markConfigurationsToTempIdMap = new HashMap<>();
        final JSONArray markConfigurationsJSON = new JSONArray();
        for (final MarkConfiguration markConfiguration : courseConfiguration.getAllMarks()) {
            JSONObject markConfigurationsEntry = new JSONObject();
            final UUID markConfigurationId = UUID.randomUUID();
            markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_ID, markConfigurationId.toString());
            if (markConfiguration.getOptionalMarkTemplate() != null) {
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID,
                        markConfiguration.getOptionalMarkTemplate().getId().toString());
            }
            final String associatedRole = courseConfiguration.getAssociatedRoles().get(markConfiguration);
            if (associatedRole != null) {
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_ASSOCIATED_ROLE, associatedRole);
            }
            if (markConfiguration instanceof FreestyleMarkConfiguration) {
                final FreestyleMarkConfiguration freeStyleMarkConfiguration = (FreestyleMarkConfiguration) markConfiguration;
                if (freeStyleMarkConfiguration.getOptionalMarkProperties() != null) {
                    markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID,
                            freeStyleMarkConfiguration.getOptionalMarkProperties().getId().toString());
                }
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_FREESTYLE_PROPERTIES,
                        commonMarkPropertiesJsonSerializer
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
            markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_EFFECTIVE_PROPERTIES,
                    commonMarkPropertiesJsonSerializer.serialize(markConfiguration.getEffectiveProperties()));

            if (markConfiguration.getEffectivePositioning() != null) {
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_EFFECTIVE_POSITIONING,
                        positioningJsonSerializer.serialize(markConfiguration.getEffectivePositioning()));
            }
            if (markConfiguration.getOptionalPositioning() != null) {
                markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_POSITIONING,
                        storablePositioningJsonSerializer.serialize(markConfiguration.getOptionalPositioning()));
            }
            markConfigurationsEntry.put(FIELD_MARK_CONFIGURATION_STORE_TO_INVENTORY,
                    markConfiguration.isStoreToInventory());

            markConfigurationsToTempIdMap.put(markConfiguration, markConfigurationId);
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
            final ControlPointWithMarkConfiguration controlPoint = waypoint.getControlPoint();
            controlPoint.getMarkConfigurations()
                    .forEach(mc -> markConfigurationIDs.add(markConfigurationsToTempIdMap.get(mc).toString()));
            waypointEntry.put(FIELD_WAYPOINT_MARK_CONFIGURATION_IDS, markConfigurationIDs);
            if (controlPoint instanceof MarkPairWithConfiguration) {
                final MarkPairWithConfiguration markPairWithConfiguration = (MarkPairWithConfiguration) controlPoint;
                waypointEntry.put(FIELD_WAYPOINT_CONTROL_POINT_NAME, markPairWithConfiguration.getName());
                waypointEntry.put(FIELD_WAYPOINT_CONTROL_POINT_SHORT_NAME, markPairWithConfiguration.getShortName());
            }
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
