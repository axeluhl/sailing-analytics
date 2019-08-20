package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseTemplateJsonSerializer implements JsonSerializer<CourseTemplate> {

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_OPTIONAL_IMAGE_URL = "optionalImageURL";
    public static final String FIELD_ALL_MARK_TEMPLATES = "allMarkTemplates";
    public static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_PASSING_INSTRUCTION = "passingInstruction";
    public static final String FIELD_CONTROL_POINT_NAME = "controlPointName";
    public static final String FIELD_MARK_TEMPLATE_IDS = "markTemplateIds";
    public static final String FIELD_ASSOCIATED_ROLE = "associatedRole";
    public static final String FIELD_OPTIONAL_REPEATABLE_PART = "optionalRepeatablePart";
    
    private final JsonSerializer<RepeatablePart> repeatablePartJsonSerializer;
    private final JsonSerializer<MarkTemplate> markTemplateJsonSerializer;
    
    public CourseTemplateJsonSerializer() {
        repeatablePartJsonSerializer = new RepeatablePartJsonSerializer();
        markTemplateJsonSerializer = new MarkTemplateJsonSerializer();
    }

    @Override
    public JSONObject serialize(CourseTemplate courseTemplate) {
        final JSONObject result = new JSONObject();
        result.put(FIELD_ID, courseTemplate.getId().toString());
        result.put(FIELD_NAME, courseTemplate.getName());
        result.put(FIELD_OPTIONAL_IMAGE_URL, courseTemplate.getOptionalImageURL());
        
        final JSONArray tags = new JSONArray();
        courseTemplate.getTags().forEach(tags::add);
        result.put(FIELD_TAGS, tags);
        
        final JSONArray allMarkTemplates = new JSONArray();
        courseTemplate.getMarkTemplates().forEach(mt -> {
            final JSONObject markTemplateEntry = markTemplateJsonSerializer.serialize(mt);
            final String associatedRoleOrNull = courseTemplate.getAssociatedRoles().get(mt);
            if (associatedRoleOrNull != null) {
                markTemplateEntry.put(FIELD_ASSOCIATED_ROLE, associatedRoleOrNull);
            }
            allMarkTemplates.add(markTemplateEntry);
        });
        result.put(FIELD_ALL_MARK_TEMPLATES, allMarkTemplates);
        
        final JSONArray waypoints = new JSONArray();
        courseTemplate.getWaypointTemplates(1).forEach(wp -> {
            final JSONObject waypointEntry = new JSONObject();
            
            waypointEntry.put(FIELD_PASSING_INSTRUCTION, wp.getPassingInstruction().name());
            final JSONArray markTemplateIDs = new JSONArray();
            wp.getControlPointTemplate().getMarks().forEach(mt -> markTemplateIDs.add(mt.getId().toString()));
            waypointEntry.put(FIELD_MARK_TEMPLATE_IDS, markTemplateIDs);
            waypointEntry.put(FIELD_CONTROL_POINT_NAME, wp.getControlPointTemplate().getName());
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
