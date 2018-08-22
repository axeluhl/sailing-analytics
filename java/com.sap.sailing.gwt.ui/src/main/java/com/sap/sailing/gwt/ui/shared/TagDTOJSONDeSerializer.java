package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TagDTOJSONDeSerializer{

    private static final String FIELD_TAGS = "tags";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_IMAGE_URL = "image";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_VISIBLE_FOR_PUBLIC = "public";
    private static final String FIELD_RACE_TIMEPOINT = "raceTimepoint";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_REVOKED_AT = "revokedAt";

    public TagDTOJSONDeSerializer() {
    }


    public JSONObject serialize(List<TagDTO> tags) {
        JSONObject result = new JSONObject();

        JSONArray tagsArray = new JSONArray();
        result.put(FIELD_TAGS, tagsArray);

        for (TagDTO tag : tags) {
            JSONObject tagObject = new JSONObject();
            tagsArray.add(tagObject);
            tagObject.put(FIELD_TAG, tag.getTag());
            tagObject.put(FIELD_COMMENT, tag.getComment());
            tagObject.put(FIELD_IMAGE_URL, tag.getImageURL());
            tagObject.put(FIELD_USERNAME, tag.getUsername());
            tagObject.put(FIELD_VISIBLE_FOR_PUBLIC, Boolean.toString(tag.isVisibleForPublic()));
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
                String tag = tagValue.get(FIELD_TAG).toString();
                String comment = tagValue.get(FIELD_COMMENT).toString();
                String imageURL = tagValue.get(FIELD_IMAGE_URL).toString();
                String username = tagValue.get(FIELD_USERNAME).toString();
                boolean visibleForPublic = Boolean.valueOf(tagValue.get(FIELD_VISIBLE_FOR_PUBLIC).toString());
                TimePoint raceTimepoint = deserilizeTimePoint(tagValue.get(FIELD_RACE_TIMEPOINT).toString());
                TimePoint createdAt = deserilizeTimePoint(tagValue.get(FIELD_CREATED_AT).toString());
                TimePoint revokedAt = deserilizeTimePoint(tagValue.get(FIELD_REVOKED_AT).toString());
                result.add(new TagDTO(tag, comment, imageURL, username, visibleForPublic, raceTimepoint, createdAt,
                        revokedAt));
            }
        }
        return result;
    }

    private String timePointToString(TimePoint timepoint) {
        if (timepoint != null) {
            return Long.toString(timepoint.asMillis());
        } else {
            return "";
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
        return escape(racecolumn) + "+" + escape(fleet) + "+" +  escape(leaderboard);
    }
    
    private String escape(String string) {
        return string.replaceAll("/", "//").replaceAll("+", "/p");
    }
}