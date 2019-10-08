package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class CourseConfiguration extends JsonWrapper {

    public static final String FIELD_NAME = "name";
    private static final String FIELD_OPTIONAL_COURSE_TEMPLATE_UUID = "courseTemplateId";
    private static final String FIELD_MARKCONFIGURATIONS = "markConfigurations";
    private static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_NUMBER_OF_LAPS = "numberOfLaps";
    public static final String FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";

    public CourseConfiguration(JSONObject json) {
        super(json);
    }

    public CourseConfiguration(String name, Iterable<MarkConfiguration> markConfigurations,
            Iterable<WaypointWithMarkConfiguration> waypoints) {
        super(new JSONObject());
        getJson().put(FIELD_NAME, name);
        final JSONArray markConfigurationsJson = new JSONArray();
        markConfigurations.forEach(mc -> markConfigurationsJson.add(mc.getJson()));
        getJson().put(FIELD_MARKCONFIGURATIONS, markConfigurationsJson);
        getJson().put(FIELD_WAYPOINTS, waypoints);
    }

    public Iterable<MarkConfiguration> getMarkConfigurations() {
        JSONArray markConfigurationsJson = (JSONArray) get(FIELD_MARKCONFIGURATIONS);
        return markConfigurationsJson.stream().map(m -> (JSONObject) m).map(MarkConfiguration::new)
                .collect(Collectors.toList());
    }

    public Iterable<WaypointWithMarkConfiguration> getWaypoints() {
        JSONArray waypointsJson = (JSONArray) get(FIELD_WAYPOINTS);
        return waypointsJson.stream().map(w -> (JSONObject) w).map(WaypointWithMarkConfiguration::new)
                .collect(Collectors.toList());
    }

    public int getNumberOfLaps() {
        return ((Long) get(FIELD_NUMBER_OF_LAPS)).intValue();
    }

    public void setNumberOfLaps(Integer numberOfLaps) {
        getJson().put(FIELD_NUMBER_OF_LAPS, numberOfLaps);
    }

    public UUID getOptionalCourseTemplateId() {
        Object optionalCourseTemplateId = get(FIELD_OPTIONAL_COURSE_TEMPLATE_UUID);
        return optionalCourseTemplateId != null ? UUID.fromString((String) optionalCourseTemplateId) : null;
    }

    public RepeatablePart getRepeatablePart() {
        JSONObject repeatablePartJson = get(FIELD_OPTIONAL_REPEATABLE_PART);
        return repeatablePartJson != null ? new RepeatablePart(repeatablePartJson) : null;
    }
}
