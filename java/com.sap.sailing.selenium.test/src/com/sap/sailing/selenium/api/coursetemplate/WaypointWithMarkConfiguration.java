package com.sap.sailing.selenium.api.coursetemplate;

import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class WaypointWithMarkConfiguration extends JsonWrapper {
    private static final String FIELD_CONTROL_POINT_NAME = "controlPointName";
    private static final String FIELD_MARK_CONFIGURATION_IDS = "markConfigurationIds";
    private static final String FIELD_PASSING_INSTRUCTION = "passingInstruction";

    private final Iterable<String> markConfigurationIds;
    private final PassingInstruction passingInstruction;

    public WaypointWithMarkConfiguration(final JSONObject json) {
        super(json);
        JSONArray markConfigurationIdsJson = get(FIELD_MARK_CONFIGURATION_IDS);
        markConfigurationIds = markConfigurationIdsJson.stream().map(m -> ((JSONObject) m).toString())
                .collect(Collectors.toList());
        passingInstruction = get(FIELD_PASSING_INSTRUCTION);
    }

    public WaypointWithMarkConfiguration(String name, PassingInstruction passingInstruction,
            Iterable<String> markConfigurationIds) {
        super(new JSONObject());
        this.passingInstruction = passingInstruction;
        this.markConfigurationIds = markConfigurationIds;
        getJson().put(FIELD_CONTROL_POINT_NAME, name);
        getJson().put(FIELD_PASSING_INSTRUCTION, passingInstruction.name());
        final JSONArray markIds = new JSONArray();
        markConfigurationIds.forEach(mt -> markIds.add(mt.toString()));
        getJson().put(FIELD_MARK_CONFIGURATION_IDS, markIds);
    }

    public Iterable<String> getMarkConfigurationIds() {
        return markConfigurationIds;
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }
}
