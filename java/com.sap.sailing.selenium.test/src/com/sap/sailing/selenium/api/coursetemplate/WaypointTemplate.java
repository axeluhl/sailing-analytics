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
    private static final String FIELD_PASSING_INSTRUCTION = "passingInstruction";
    private static final String FIELD_MARK_ROLE_IDS = "markRoleIds";

    private final Iterable<MarkRole> markRoles;
    private final PassingInstruction passingInstruction;

    public WaypointTemplate(String name, PassingInstruction passingInstruction, Iterable<MarkRole> markRoles) {
        super(new JSONObject());
        this.passingInstruction = passingInstruction;
        this.markRoles = markRoles;
        getJson().put(FIELD_CONTROL_POINT_NAME, name);
        getJson().put(FIELD_PASSING_INSTRUCTION, passingInstruction.name());
        final JSONArray roleIds = new JSONArray();
        markRoles.forEach(mr -> roleIds.add(mr.getId().toString()));
        getJson().put(FIELD_MARK_ROLE_IDS, roleIds);
    }

    public WaypointTemplate(JSONObject json, Function<UUID, MarkRole> markRoleResolver) {
        super(json);
        final JSONArray markRoleIds = get(FIELD_MARK_ROLE_IDS);
        final List<MarkRole> markRoles = markRoleIds.stream().map(idObject -> UUID.fromString(idObject.toString()))
                .map(markRoleResolver::apply).collect(Collectors.toList());
        this.markRoles = markRoles;
        passingInstruction = PassingInstruction.valueOf(get(FIELD_PASSING_INSTRUCTION));
    }

    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

    public Iterable<MarkRole> getMarkRoles() {
        return markRoles;
    }
}