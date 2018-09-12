package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sse.common.TimePoint;

/**
 * Serializes and deserializes {@link TagDTO tags}. <b>Before changing this class, have a look at
 * {@link TagDTO.TagDeSerializer}.</b>
 * 
 * @see TagDTO
 */
public class TagDTODeSerializer extends TagDTO.TagDeSerializer {

    /**
     * Serializes {@link TagDTO tag} as {@link JSONObject}.
     * 
     * @param tag
     *            {@link TagDTO tag} to be serialized
     * @return {@link JSONObject json object}
     */
    private JSONObject serialize(TagDTO tag) {
        JSONObject result = new JSONObject();
        result.put(FIELD_TAG, new JSONString(tag.getTag()));
        result.put(FIELD_COMMENT, new JSONString(tag.getComment()));
        result.put(FIELD_IMAGE_URL, new JSONString(tag.getImageURL()));
        result.put(FIELD_USERNAME, new JSONString(tag.getUsername()));
        result.put(FIELD_VISIBLE_FOR_PUBLIC, JSONBoolean.getInstance(tag.isVisibleForPublic()));
        result.put(FIELD_RACE_TIMEPOINT, new JSONNumber(serializeTimePoint(tag.getRaceTimepoint())));
        result.put(FIELD_CREATED_AT, new JSONNumber(serializeTimePoint(tag.getCreatedAt())));
        result.put(FIELD_REVOKED_AT, new JSONNumber(serializeTimePoint(tag.getRevokedAt())));
        return result;
    }

    /**
     * Deserializes {@link JSONObject jsonObject} to {@link TagDTO}.
     * 
     * @param jsonObject
     *            {@link JSONObject jsonObject} to be deserialized
     * @return {@link TagDTO tag}
     */
    private TagDTO deserialize(JSONObject jsonObject) {
        // if deserializing throws an error, return null
        try {
            JSONString tag = (JSONString) jsonObject.get(FIELD_TAG);
            JSONString comment = (JSONString) jsonObject.get(FIELD_COMMENT);
            JSONString imageURL = (JSONString) jsonObject.get(FIELD_IMAGE_URL);
            JSONString username = (JSONString) jsonObject.get(FIELD_USERNAME);
            boolean visibleForPublic = Boolean.valueOf(jsonObject.get(FIELD_VISIBLE_FOR_PUBLIC).toString());
            // GWT-JSON-Library only supports its own types such as JSONNumber, which is saved as double caused by
            // JavaScript not supporting 64-bit long numbers. TimePoints are saved as long values in Java, therefor
            // conversion from JSONObject -> JSONValue -> JSONNumber -> Double -> Long is needed.
            // TODO: Find better way of type conversion
            TimePoint raceTimePoint = deserilizeTimePoint(
                    Double.valueOf(((JSONNumber) (jsonObject.get(FIELD_RACE_TIMEPOINT))).doubleValue()).longValue());
            TimePoint createdAt = deserilizeTimePoint(
                    Double.valueOf(((JSONNumber) (jsonObject.get(FIELD_CREATED_AT))).doubleValue()).longValue());
            TimePoint revokedAt = deserilizeTimePoint(
                    Double.valueOf(((JSONNumber) (jsonObject.get(FIELD_REVOKED_AT))).doubleValue()).longValue());
            return new TagDTO(tag.stringValue(), comment.stringValue(), imageURL.stringValue(), username.stringValue(),
                    visibleForPublic, raceTimePoint, createdAt, revokedAt);
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public String serializeTag(TagDTO tag) {
        return serialize(tag).toString();
    }

    @Override
    public String serializeTags(List<TagDTO> tags) {
        JSONArray jsonTags = new JSONArray();
        for (int i = 0; i < tags.size(); i++) {
            jsonTags.set(i, serialize(tags.get(i)));
        }
        return jsonTags.toString();
    }

    @Override
    public TagDTO deserializeTag(String jsonObject) {
        JSONObject jsonTag = (JSONObject) JSONParser.parseStrict(jsonObject);
        return deserialize(jsonTag);
    }

    @Override
    public List<TagDTO> deserializeTags(String jsonArray) {
        JSONArray jsonTags = (JSONArray) JSONParser.parseStrict(jsonArray);
        List<TagDTO> result = new ArrayList<TagDTO>();

        for (int i = 0; i < jsonTags.size(); i++) {
            result.add(deserialize((JSONObject) jsonTags.get(i)));
        }
        return result;
    }
}

/*
public JSONObject serialize(TagDTO tag) {
    JSONObject result = new JSONObject();
    result.put(TagDTO.FIELD_TAG, tag.getTag());
    result.put(TagDTO.FIELD_COMMENT, tag.getComment());
    result.put(TagDTO.FIELD_IMAGE_URL, tag.getImageURL());
    result.put(TagDTO.FIELD_USERNAME, tag.getUsername());
    result.put(TagDTO.FIELD_VISIBLE_FOR_PUBLIC, tag.isVisibleForPublic());
    result.put(TagDTO.FIELD_RACE_TIMEPOINT, serializeTimePoint(tag.getRaceTimepoint()));
    result.put(TagDTO.FIELD_CREATED_AT, serializeTimePoint(tag.getCreatedAt()));
    result.put(TagDTO.FIELD_REVOKED_AT, serializeTimePoint(tag.getRevokedAt()));
    return result;
}

public JSONArray serialize(List<TagDTO> tags) {
    JSONArray result = new JSONArray();
    for (TagDTO tag : tags) {
        result.add(serialize(tag));
    }
    return result;
}

public String serializeToString(List<TagDTO> tags) {
    return serialize(tags).toJSONString();
}


public TagDTO deserialize(JSONObject jsonObject) {
    String tag = (String) jsonObject.get(FIELD_TAG);
    String comment = (String) jsonObject.get(FIELD_COMMENT);
    String imageURL = (String) jsonObject.get(FIELD_IMAGE_URL);
    String username = (String) jsonObject.get(FIELD_USERNAME);
    boolean visibleForPublic = Boolean.valueOf((String) jsonObject.get(FIELD_VISIBLE_FOR_PUBLIC));
    TimePoint raceTimepoint = deserilizeTimePoint(jsonObject.get(FIELD_RACE_TIMEPOINT).toString());
    TimePoint createdAt = deserilizeTimePoint(jsonObject.get(FIELD_CREATED_AT).toString());
    TimePoint revokedAt = deserilizeTimePoint(jsonObject.get(FIELD_REVOKED_AT).toString());
    return new TagDTO(tag, comment, imageURL, username, visibleForPublic, raceTimepoint, createdAt, revokedAt);
}


public List<TagDTO> deserialize(JSONArray jsonArray) {
    List<TagDTO> result = new ArrayList<TagDTO>();
    for (Object object : jsonArray) {
        JSONObject jsonObject = (JSONObject) object;
        result.add(deserialize(jsonObject));
    }
    return result;
}

public List<TagDTO> deserializeArray(String array) {
    List<TagDTO> result = new ArrayList<TagDTO>();
    try {
        JSONParser parser = new JSONParser();
        Object value = parser.parse(array);
        result = deserialize((JSONArray) value);
    } catch (ParseException e) {
        // TODO: add error handling
    }
    return result;
}
*/