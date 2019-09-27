package com.sap.sailing.selenium.api.coursetemplate;

import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class CourseConfiguration extends JsonWrapper {

    private static final String FIELD_MARKCONFIGURATIONS = "markConfigurations";
    private static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_NUMBER_OF_LAPS = "numberOfLaps";

    public CourseConfiguration(JSONObject json) {
        super(json);
    }

    public CourseConfiguration(Iterable<MarkConfiguration> markConfigurations,
            Iterable<WaypointWithMarkConfiguration> waypoints) {
        super(new JSONObject());
        getJson().put(FIELD_MARKCONFIGURATIONS, markConfigurations);
        getJson().put(FIELD_WAYPOINTS, waypoints);
    }

    public Iterable<MarkConfiguration> getMarkConfigurations() {
        JSONArray markConfigurationsJson = (JSONArray) getJson().get(FIELD_MARKCONFIGURATIONS);
        return markConfigurationsJson.stream().map(m -> (JSONObject) m).map(MarkConfiguration::new)
                .collect(Collectors.toList());
    }

    public Iterable<WaypointWithMarkConfiguration> getWaypoints() {
        JSONArray waypointsJson = (JSONArray) getJson().get(FIELD_WAYPOINTS);
        return waypointsJson.stream().map(w -> (JSONObject) w).map(WaypointWithMarkConfiguration::new)
                .collect(Collectors.toList());
    }
    
    public void setNumberOfLaps(Integer numberOfLaps) {
        getJson().put(FIELD_NUMBER_OF_LAPS, numberOfLaps);
    }
}
