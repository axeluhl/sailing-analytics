package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Serializes and deserializes TagDTOs.
 */
public class TagDTOJsonDeSerializer {

    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_IMAGE_URL = "image";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_VISIBLE_FOR_PUBLIC = "public";
    private static final String FIELD_RACE_TIMEPOINT = "raceTimepoint";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_REVOKED_AT = "revokedAt";

    public JSONObject serialize(List<TagDTO> tags) {
        JSONObject result = new JSONObject();

        JSONArray tagsArray = new JSONArray();
        result.put(FIELD_TAGS, tagsArray);

        int i = 0;
        for (TagDTO tag : tags) {
            JSONObject tagObject = new JSONObject();
            tagsArray.set(i++, tagObject);
            tagObject.put(FIELD_TAG, new JSONString(tag.getTag()));
            tagObject.put(FIELD_COMMENT, new JSONString(tag.getComment()));
            tagObject.put(FIELD_IMAGE_URL, new JSONString(tag.getImageURL()));
            tagObject.put(FIELD_USERNAME, new JSONString(tag.getUsername()));
            tagObject.put(FIELD_VISIBLE_FOR_PUBLIC, JSONBoolean.getInstance(tag.isVisibleForPublic()));
            tagObject.put(FIELD_RACE_TIMEPOINT, timePointToString(tag.getRaceTimepoint()));
            tagObject.put(FIELD_CREATED_AT, timePointToString(tag.getCreatedAt()));
            tagObject.put(FIELD_REVOKED_AT, timePointToString(tag.getRevokedAt()));
        }
        return result;
    }

    public List<TagDTO> deserialize(JSONObject rootObject) {
        List<TagDTO> result = null;
        if (rootObject != null) {
            result = new ArrayList<TagDTO>();
            JSONArray tagsArray = (JSONArray) rootObject.get(FIELD_TAGS);
            for (int i = 0; i < tagsArray.size(); i++) {
                JSONObject tagValue = (JSONObject) tagsArray.get(i);
                JSONString tag = (JSONString) tagValue.get(FIELD_TAG);
                JSONString comment = (JSONString) tagValue.get(FIELD_COMMENT);
                JSONString imageURL = (JSONString) tagValue.get(FIELD_IMAGE_URL);
                JSONString username = (JSONString) tagValue.get(FIELD_USERNAME);

                boolean visibleForPublic = Boolean.valueOf(tagValue.get(FIELD_VISIBLE_FOR_PUBLIC).toString());

                TimePoint raceTimepoint = deserilizeTimePoint(
                        ((JSONString) (tagValue.get(FIELD_RACE_TIMEPOINT))).stringValue());
                TimePoint createdAt = deserilizeTimePoint(
                        ((JSONString) (tagValue.get(FIELD_CREATED_AT))).stringValue());
                TimePoint revokedAt = deserilizeTimePoint(
                        ((JSONString) (tagValue.get(FIELD_REVOKED_AT))).stringValue());
                result.add(new TagDTO(tag.stringValue(), comment.stringValue(), imageURL.stringValue(),
                        username.stringValue(), visibleForPublic, raceTimepoint, createdAt, revokedAt));
            }
        }
        return result;
    }

    private JSONString timePointToString(TimePoint timepoint) {
        if (timepoint != null) {
            return new JSONString(Long.toString(timepoint.asMillis()));
        } else {
            return new JSONString("");
        }
    }

    private TimePoint deserilizeTimePoint(String timepoint) {
        if (!timepoint.isEmpty()) {
            return new MillisecondsTimePoint(Long.parseLong(timepoint));
        } else {
            return null;
        }
    }

    public String createIdenticalKeyFromThreeStrings(String racecolumn, String fleet, String leaderboard) {
        return "Private tags:" + escape(racecolumn) + "+" + escape(fleet) + "+" + escape(leaderboard);
    }

    private String escape(String string) {
        return string.replaceAll("/", "//").replaceAll("\\+", "/p");
    }
}
