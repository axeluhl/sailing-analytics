package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.DeviceConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DeviceConfigurationJsonSerializer implements JsonSerializer<DeviceConfiguration> {

    public static final String FIELD_COURSE_AREA_NAMES = "courseAreaNames";
    public static final String FIELD_MIN_ROUNDS = "minRounds";
    public static final String FIELD_MAX_ROUNDS = "maxRounds";
    public static final String FIELD_RESULTS_RECIPIENT = "resultsRecipient";

    @Override
    public JSONObject serialize(DeviceConfiguration object) {
        JSONObject result = new JSONObject();

        if (object.getAllowedCourseAreaNames() != null) {
            JSONArray courseAreaNames = new JSONArray();
            for (String name : object.getAllowedCourseAreaNames()) {
                courseAreaNames.add(name);
            }
            result.put(FIELD_COURSE_AREA_NAMES, courseAreaNames);
        }
        if (object.getMinimumRoundsForCourse() != null) {
            result.put(FIELD_MIN_ROUNDS, object.getMinimumRoundsForCourse());
        }
        if (object.getMaximumRoundsForCourse() != null) {
            result.put(FIELD_MAX_ROUNDS, object.getMaximumRoundsForCourse());
        }
        if (object.getResultsMailRecipient() != null) {
            result.put(FIELD_RESULTS_RECIPIENT, object.getResultsMailRecipient());
        }

        return result;
    }

}
