package com.sap.sailing.server.tagging;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.TimePoint;

/**
 * Serializes and deserializes {@link TagDTO tags}. <b>Before changing this class, have a look at
 * {@link TagDTO.TagDeSerializer}.</b>
 * 
 * @see TagDTO.TagDeSerializer
 */
public class TagDTODeSerializer extends TagDTO.TagDeSerializer {

    private final JSONParser parser = new JSONParser();

    /**
     * Serializes {@link TagDTO tag} as {@link JSONObject}.
     * 
     * @param tag
     *            {@link TagDTO tag} to be serialized
     * @return {@link JSONObject json object}
     */
    public JSONObject serialize(TagDTO tag) {
        JSONObject result = new JSONObject();
        result.put(FIELD_TAG, tag.getTag());
        result.put(FIELD_COMMENT, tag.getComment());
        result.put(FIELD_IMAGE_URL, tag.getImageURL());
        result.put(FIELD_USERNAME, tag.getUsername());
        result.put(FIELD_VISIBLE_FOR_PUBLIC, tag.isVisibleForPublic());
        result.put(FIELD_RACE_TIMEPOINT, serializeTimePoint(tag.getRaceTimepoint()));
        result.put(FIELD_CREATED_AT, serializeTimePoint(tag.getCreatedAt()));
        result.put(FIELD_REVOKED_AT, serializeTimePoint(tag.getRevokedAt()));
        return result;
    }

    /**
     * Serializes list of {@link TagDTO tags} as {@link JSONArray}.
     * 
     * @param tags
     *            list of {@link TagDTO tags} to be serialized
     * @return {@link JSONArray json array}
     */
    public JSONArray serialize(List<TagDTO> tags) {
        JSONArray result = new JSONArray();
        for (TagDTO tag : tags) {
            result.add(serialize(tag));
        }
        return result;
    }

    /**
     * Deserializes {@link JSONObject jsonObject} to {@link TagDTO}.
     * 
     * @param jsonObject
     *            {@link JSONObject jsonObject} to be deserialized
     * @return <code>null</code> if <code>jsonObject</code> is no valid json for a tag, otherwise {@link TagDTO tag}
     */
    public TagDTO deserialize(JSONObject jsonObject) {
        // if deserializing throws an error, return null
        try {
            String tag = (String) jsonObject.get(FIELD_TAG);
            String comment = (String) jsonObject.get(FIELD_COMMENT);
            String imageURL = (String) jsonObject.get(FIELD_IMAGE_URL);
            String username = (String) jsonObject.get(FIELD_USERNAME);
            boolean visibleForPublic = (Boolean) jsonObject.get(FIELD_VISIBLE_FOR_PUBLIC);
            TimePoint raceTimePoint = deserilizeTimePoint((Long) (jsonObject.get(FIELD_RACE_TIMEPOINT)));
            TimePoint createdAt = deserilizeTimePoint((Long) (jsonObject.get(FIELD_CREATED_AT)));
            TimePoint revokedAt = deserilizeTimePoint((Long) (jsonObject.get(FIELD_REVOKED_AT)));
            return new TagDTO(tag, comment, imageURL, username, visibleForPublic, raceTimePoint, createdAt, revokedAt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deserializes {@link JSONArray jsonArray} to list of {@link TagDTO tags}.
     * 
     * @param jsonArray
     *            {@link JSONArray jsonArray} to be deserialized
     * @return list of {@link TagDTO tags}
     */
    public List<TagDTO> deserialize(JSONArray jsonArray) {
        return deserializeTags(jsonArray.toJSONString());
    }

    @Override
    public String serializeTag(TagDTO tag) {
        return serialize(tag).toString();
    }

    @Override
    public String serializeTags(List<TagDTO> tags) {
        JSONArray jsonTags = new JSONArray();
        for (TagDTO tag : tags) {
            jsonTags.add(serialize(tag));
        }
        return jsonTags.toString();
    }

    @Override
    public TagDTO deserializeTag(String jsonObject) {
        TagDTO result = null;
        if (jsonObject != null && !jsonObject.isEmpty()) {
            try {
                JSONObject jsonTag = (JSONObject) parser.parse(jsonObject);
                result = deserialize(jsonTag);
            } catch (Exception e) {
                // do nothing => return null
            }
        }
        return result;

    }

    @Override
    public List<TagDTO> deserializeTags(String jsonArray) {
        JSONArray jsonTags;
        List<TagDTO> result = new ArrayList<TagDTO>();
        if (jsonArray != null && !jsonArray.isEmpty()) {
            try {
                jsonTags = (JSONArray) parser.parse(jsonArray);
                for (int i = 0; i < jsonTags.size(); i++) {
                    result.add(deserialize((JSONObject) jsonTags.get(i)));
                }
            } catch (Exception e) {
                // return all already parsed tags in case an error occurs => do nothing
            }
        }
        return result;
    }
}
