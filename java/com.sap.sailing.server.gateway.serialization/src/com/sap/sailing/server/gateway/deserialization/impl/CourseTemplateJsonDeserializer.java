package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkRolePair.MarkRolePairFactory;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.RepeatablePart;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.impl.CourseTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointTemplateImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseTemplateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkRoleJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkTemplateJsonSerializer;

public class CourseTemplateJsonDeserializer implements JsonDeserializer<CourseTemplate> {

    private final Function<UUID, MarkTemplate> markTemplateResolver;
    private final Function<UUID, MarkRole> markRoleResolver;
    private final JsonDeserializer<RepeatablePart> repeatablePartJsonDeserializer;

    public CourseTemplateJsonDeserializer(Function<UUID, MarkTemplate> markTemplateResolver,
            Function<UUID, MarkRole> markRoleResolver) {
        this.markTemplateResolver = markTemplateResolver;
        this.markRoleResolver = markRoleResolver;
        repeatablePartJsonDeserializer = new RepeatablePartJsonDeserializer();
    }

    @Override
    public CourseTemplate deserialize(JSONObject json) throws JsonDeserializationException {
        final String courseTemplateName = (String) json.get(CourseTemplateJsonSerializer.FIELD_NAME);
        final String courseTemplateShortName = (String) json.get(CourseTemplateJsonSerializer.FIELD_SHORT_NAME);
        final String optionalImageUrlString = (String) json.get(CourseTemplateJsonSerializer.FIELD_OPTIONAL_IMAGE_URL);
        URL optionalImageURL;
        try {
            optionalImageURL = optionalImageUrlString == null ? null : new URL(optionalImageUrlString);
        } catch (MalformedURLException e) {
            throw new JsonDeserializationException("Error while trying to deserialize the given image URL: " + optionalImageUrlString);
        }
        final List<String> tags = new ArrayList<>();
        final JSONArray tagsJSON = (JSONArray) json.get(CourseTemplateJsonSerializer.FIELD_TAGS);
        if (tagsJSON != null) {
            tagsJSON.forEach(t -> tags.add(t.toString()));
        }
        final Map<UUID, MarkRole> allMarkRolesById = new HashMap<UUID, MarkRole>();
        final Map<MarkRole, MarkTemplate> defaultMarkTemplatesForMarkRoles = new HashMap<>();
        final JSONArray allMarkRolesJSON = (JSONArray) json.get(CourseTemplateJsonSerializer.FIELD_ALL_MARK_ROLES);
        for (Object markRoleWithMarkTemplateRefObject : allMarkRolesJSON) {
            final JSONObject markRoleWithMarkTemplateRef = (JSONObject) markRoleWithMarkTemplateRefObject;
            final UUID markRoleUUID = UUID.fromString(
                    (String) markRoleWithMarkTemplateRef.get(MarkRoleJsonSerializer.FIELD_ID));
            final MarkRole resolvedMarkRole = markRoleResolver.apply(markRoleUUID);
            if (resolvedMarkRole == null) {
                throw new JsonDeserializationException("Mark role with ID " + markRoleUUID + " can't be resolved");
            }
            allMarkRolesById.put(markRoleUUID, resolvedMarkRole);
            final String markTemplateIdAsString = (String) markRoleWithMarkTemplateRef.get(CourseTemplateJsonSerializer.FIELD_ASSOCIATED_MARK_TEMPLATE_ID);
            final MarkTemplate resolvedMarkTemplate = markTemplateResolver.apply(UUID.fromString(markTemplateIdAsString));
            defaultMarkTemplatesForMarkRoles.put(resolvedMarkRole, resolvedMarkTemplate);
        }
        final Map<UUID, MarkTemplate> allMarkTemplatesById = new HashMap<UUID, MarkTemplate>();
        final Map<MarkTemplate, MarkRole> defaultMarkRolesForMarkTemplates = new HashMap<>();
        final JSONArray allMarkTemplatesJSON = (JSONArray) json.get(CourseTemplateJsonSerializer.FIELD_ALL_MARK_TEMPLATES);
        for (Object markTemplateWithOptionalRoleNameObject : allMarkTemplatesJSON) {
            final JSONObject markTemplateWithOptionalRoleName = (JSONObject) markTemplateWithOptionalRoleNameObject;
            final UUID markTemplateUUID = UUID.fromString(
                    (String) markTemplateWithOptionalRoleName.get(MarkTemplateJsonSerializer.FIELD_ID));
            final MarkTemplate resolvedMarkTemplate = markTemplateResolver.apply(markTemplateUUID);
            if (resolvedMarkTemplate == null) {
                throw new JsonDeserializationException("Mark template with ID " + markTemplateUUID + " can't be resolved");
            }
            allMarkTemplatesById.put(markTemplateUUID, resolvedMarkTemplate);
            final String markRoleIdAsStringOrNull = (String) markTemplateWithOptionalRoleName.get(CourseTemplateJsonSerializer.FIELD_ASSOCIATED_MARK_ROLE_ID);
            if (markRoleIdAsStringOrNull != null && !markRoleIdAsStringOrNull.isEmpty()) {
                final MarkRole resolvedMarkRole = markRoleResolver.apply(UUID.fromString(markRoleIdAsStringOrNull));
                defaultMarkRolesForMarkTemplates.put(resolvedMarkTemplate, resolvedMarkRole);
            }
        }
        final List<WaypointTemplate> waypoints = new ArrayList<>();
        final MarkRolePairFactory markPairTemplateFactory = new MarkRolePairFactory();
        final JSONArray waypointsJSON = (JSONArray) json.get(CourseTemplateJsonSerializer.FIELD_WAYPOINTS);
        for (Object waypointObject : waypointsJSON) {
            final JSONObject waypointJSON = (JSONObject) waypointObject;
            final JSONArray markRoleIDs = (JSONArray) waypointJSON.get(CourseTemplateJsonSerializer.FIELD_MARK_ROLE_IDS);
            final List<MarkRole> resolvedMarkRoles = new ArrayList<>();
            for (Object markRoleIdObject : markRoleIDs) {
                final MarkRole resolvedMarkRole = allMarkRolesById.get(UUID.fromString(markRoleIdObject.toString()));
                if (resolvedMarkRole == null) {
                    throw new JsonDeserializationException("Mark role with ID " + markRoleIdObject + " was not defined to be part of the course template");
                }
                resolvedMarkRoles.add(resolvedMarkRole);
            }
            final ControlPointTemplate controlPointTemplate;
            if (resolvedMarkRoles.size() == 1) {
                controlPointTemplate = resolvedMarkRoles.get(0);
            } else if (resolvedMarkRoles.size() == 2) {
                controlPointTemplate = markPairTemplateFactory.create(
                        (String) waypointJSON.get(CourseTemplateJsonSerializer.FIELD_CONTROL_POINT_NAME),
                        (String) waypointJSON.get(CourseTemplateJsonSerializer.FIELD_CONTROL_POINT_SHORT_NAME),
                        resolvedMarkRoles.get(0), resolvedMarkRoles.get(1));
            } else {
                throw new JsonDeserializationException("Unexpected number of marks found for waypoint");
            }
            PassingInstruction passingInstruction = PassingInstruction
                    .valueOf((String) waypointJSON.get(CourseTemplateJsonSerializer.FIELD_PASSING_INSTRUCTION));
            waypoints.add(new WaypointTemplateImpl(controlPointTemplate, passingInstruction));
        }
        final JSONObject repeatablePartJSON = (JSONObject) json.get(CourseTemplateJsonSerializer.FIELD_OPTIONAL_REPEATABLE_PART);
        final RepeatablePart optionalRepeatablePart;
        if (repeatablePartJSON == null) {
            optionalRepeatablePart = null;
        } else {
            optionalRepeatablePart = repeatablePartJsonDeserializer.deserialize(repeatablePartJSON);
        }
        final Number defaultNumberOfLapsNumber = (Number) json
                .get(CourseTemplateJsonSerializer.FIELD_DEFAULT_NUMBER_OF_LAPS);
        final Integer defaultNumberOfLaps = defaultNumberOfLapsNumber == null ? null : defaultNumberOfLapsNumber.intValue();
        final CourseTemplateImpl courseTemplate = new CourseTemplateImpl(null, courseTemplateName,
                courseTemplateShortName, allMarkTemplatesById.values(), waypoints, defaultMarkTemplatesForMarkRoles, defaultMarkRolesForMarkTemplates,
                optionalImageURL, optionalRepeatablePart, defaultNumberOfLaps);
        courseTemplate.setTags(tags);
        return courseTemplate;
    }
}
