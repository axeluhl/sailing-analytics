package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseTemplateJsonSerializer implements JsonSerializer<CourseTemplate> {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SHORT_NAME = "shortName";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_OPTIONAL_IMAGE_URL = "optionalImageURL";
    public static final String FIELD_ALL_MARK_TEMPLATES = "allMarkTemplates";
    public static final String FIELD_ALL_MARK_ROLES = "allMarkRoles";
    public static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_PASSING_INSTRUCTION = "passingInstruction";
    public static final String FIELD_CONTROL_POINT_NAME = "controlPointName";
    public static final String FIELD_CONTROL_POINT_SHORT_NAME = "controlPointShortName";
    public static final String FIELD_MARK_TEMPLATE_IDS = "markTemplateIds";
    public static final String FIELD_MARK_TEMPLATE_ID = "markTemplateId";
    public static final String FIELD_ASSOCIATED_MARK_ROLE_ID = "associatedRoleId";
    public static final String FIELD_MARK_ROLE_IDS = "markRoleIds";
    public static final String FIELD_ASSOCIATED_MARK_TEMPLATE_ID = "associatedMarkTemplateId";
    public static final String FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";
    public static final String FIELD_DEFAULT_NUMBER_OF_LAPS = "defaultNumberOfLaps";
    
    private final JsonSerializer<RepeatablePart> repeatablePartJsonSerializer;
    private final MarkRoleJsonSerializer markRoleJsonSerializer;
    private final MarkTemplateJsonSerializer markTemplateJsonSerializer;
    
    public CourseTemplateJsonSerializer() {
        repeatablePartJsonSerializer = new RepeatablePartJsonSerializer();
        markRoleJsonSerializer = new MarkRoleJsonSerializer();
        markTemplateJsonSerializer = new MarkTemplateJsonSerializer();
    }

    @Override
    public JSONObject serialize(CourseTemplate courseTemplate) {
        final JSONObject result = new JSONObject();
        result.put(FIELD_ID, courseTemplate.getId().toString());
        result.put(FIELD_NAME, courseTemplate.getName());
        result.put(FIELD_SHORT_NAME, courseTemplate.getShortName());
        result.put(FIELD_OPTIONAL_IMAGE_URL, courseTemplate.getOptionalImageURL());
        result.put(FIELD_DEFAULT_NUMBER_OF_LAPS, courseTemplate.getDefaultNumberOfLaps());
        final JSONArray tags = new JSONArray();
        courseTemplate.getTags().forEach(tags::add);
        result.put(FIELD_TAGS, tags);
        final JSONArray allMarkRoles = new JSONArray();
        courseTemplate.getMarkRoles().forEach(markRole -> {
            final JSONObject markRoleEntry = markRoleJsonSerializer.serialize(markRole);
            markRoleEntry.put(FIELD_ASSOCIATED_MARK_TEMPLATE_ID, courseTemplate.getDefaultMarkTemplateForRole(markRole).getId().toString());
            allMarkRoles.add(markRoleEntry);
        });
        result.put(FIELD_ALL_MARK_ROLES, allMarkRoles);
        final JSONArray allMarkTemplates = new JSONArray();
        courseTemplate.getMarkTemplates().forEach(mt -> {
            final JSONObject markTemplateEntry = markTemplateJsonSerializer.serialize(mt);
            final MarkRole associatedRoleOrNull = courseTemplate.getOptionalAssociatedRole(mt);
            if (associatedRoleOrNull != null) {
                markTemplateEntry.put(FIELD_ASSOCIATED_MARK_ROLE_ID, associatedRoleOrNull.getId().toString());
            }
            allMarkTemplates.add(markTemplateEntry);
        });
        result.put(FIELD_ALL_MARK_TEMPLATES, allMarkTemplates);
        final JSONArray waypoints = new JSONArray();
        courseTemplate.getWaypointTemplates().forEach(wp -> {
            final JSONObject waypointEntry = new JSONObject();
            waypointEntry.put(FIELD_PASSING_INSTRUCTION, wp.getPassingInstruction().name());
            final JSONArray markRoleIDs = new JSONArray();
            wp.getControlPointTemplate().getMarkRoles().forEach(mr -> markRoleIDs.add(mr.getId().toString()));
            waypointEntry.put(FIELD_MARK_ROLE_IDS, markRoleIDs);
            waypointEntry.put(FIELD_CONTROL_POINT_NAME, wp.getControlPointTemplate().getName());
            waypointEntry.put(FIELD_CONTROL_POINT_SHORT_NAME, wp.getControlPointTemplate().getShortName());
            waypoints.add(waypointEntry);
        });
        result.put(FIELD_WAYPOINTS, waypoints);
        if (courseTemplate.hasRepeatablePart()) {
            result.put(FIELD_OPTIONAL_REPEATABLE_PART,
                    repeatablePartJsonSerializer.serialize(courseTemplate.getRepeatablePart()));
        }
        return result;
    }
}
