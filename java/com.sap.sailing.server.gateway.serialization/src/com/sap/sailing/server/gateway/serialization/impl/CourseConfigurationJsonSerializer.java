package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
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

    public CourseConfigurationJsonSerializer() {
        repeatablePartJsonSerializer = new RepeatablePartJsonSerializer();
    }

    @Override
    public JSONObject serialize(CourseConfiguration courseConfiguration) {
        final JSONObject result = new JSONObject();
        //TODO: name? //result.put(FIELD_NAME, "");
        
        for (MarkConfiguration markConfiguration : courseConfiguration.getAllMarks()) {
            
        }
        
        if (courseConfiguration.hasRepeatablePart()) {
            result.put(FIELD_OPTIONAL_REPEATABLE_PART,
                    repeatablePartJsonSerializer.serialize(courseConfiguration.getRepeatablePart()));
        }
        result.put(FIELD_NUMBER_OF_LAPS, courseConfiguration.getNumberOfLaps());
        return result;
    }

}
