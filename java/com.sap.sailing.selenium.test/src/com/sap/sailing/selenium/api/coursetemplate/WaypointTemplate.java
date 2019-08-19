package com.sap.sailing.selenium.api.coursetemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class WaypointTemplate extends JsonWrapper {
    private static final String FIELD_CONTROL_POINT_NAME = "controlPointName";
    private static final String FIELD_MARK_TEMPLATE_IDS = "markTemplateIds";
    private static final String FIELD_PASSING_INSTRUCTION = "passingInstruction";

    private final Iterable<MarkTemplate> marks;
    private final PassingInstruction passingInstruction;

    public WaypointTemplate(String name, PassingInstruction passingInstruction, Iterable<MarkTemplate> marks) {
        super(new JSONObject());
        this.passingInstruction = passingInstruction;
        this.marks = marks;
        getJson().put(FIELD_CONTROL_POINT_NAME, name);
        getJson().put(FIELD_PASSING_INSTRUCTION, passingInstruction.name());
        final JSONArray markIds = new JSONArray();
        marks.forEach(mt -> markIds.add(mt.getId().toString()));
        getJson().put(FIELD_MARK_TEMPLATE_IDS, markIds);
    }

    public WaypointTemplate(JSONObject json, Function<UUID, MarkTemplate> markTemplateResolver) {
        super(json);
        final JSONArray markIds = get(FIELD_MARK_TEMPLATE_IDS);
        final List<MarkTemplate> marks = markIds.stream().map(idObject -> UUID.fromString(idObject.toString()))
                .map(markTemplateResolver::apply).collect(Collectors.toList());
        this.marks = marks;
        passingInstruction = PassingInstruction.valueOf(get(FIELD_PASSING_INSTRUCTION));
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

    public Iterable<MarkTemplate> getMarks() {
        return marks;
    }
}