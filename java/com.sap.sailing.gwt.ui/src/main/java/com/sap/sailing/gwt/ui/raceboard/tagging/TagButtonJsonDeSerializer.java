package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * Serializes and deserializes {@link TagButton tag-buttons} to save them in the {@link com.sap.sse.security.UserStore
 * UserStore}.
 */
public class TagButtonJsonDeSerializer {

    private static final String FIELD_BUTTON_NAME = "buttonName";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_IMAGE_URL = "imageURL";
    private static final String FIELD_VISIBLE_FOR_PUBLIC = "public";

    public JSONArray serialize(List<TagButton> tagButtons) {
        JSONArray result = new JSONArray();
        if (tagButtons != null) {
            int i = 0;
            for (TagButton button : tagButtons) {
                JSONObject tagButtonObject = new JSONObject();
                tagButtonObject.put(FIELD_BUTTON_NAME, new JSONString(button.getText()));
                tagButtonObject.put(FIELD_TAG, new JSONString(button.getTag()));
                if (button.getComment() != null) {
                    tagButtonObject.put(FIELD_COMMENT, new JSONString(button.getComment()));
                }
                if (button.getImageURL() != null) {
                    tagButtonObject.put(FIELD_IMAGE_URL, new JSONString(button.getImageURL()));
                }
                tagButtonObject.put(FIELD_VISIBLE_FOR_PUBLIC, JSONBoolean.getInstance(button.isVisibleForPublic()));
                result.set(i++, tagButtonObject);
            }
        }
        return result;
    }

    public List<TagButton> deserialize(JSONArray rootObject) {
        List<TagButton> result = new ArrayList<TagButton>();
        if (rootObject != null) {
            result = new ArrayList<TagButton>();
            for (int i = 0; i < rootObject.size(); i++) {
                JSONObject tagButtonValue = (JSONObject) rootObject.get(i);
                JSONString tagButtonName = (JSONString) tagButtonValue.get(FIELD_BUTTON_NAME);
                JSONString tagButtonTag = (JSONString) tagButtonValue.get(FIELD_TAG);
                JSONString tagButtonComment = (JSONString) tagButtonValue.get(FIELD_COMMENT);
                JSONString tagButtonImageURL = (JSONString) tagButtonValue.get(FIELD_IMAGE_URL);
                JSONBoolean tagButtonVisibleForPublic = (JSONBoolean) tagButtonValue.get(FIELD_VISIBLE_FOR_PUBLIC);
                result.add(new TagButton(tagButtonName.stringValue(), tagButtonTag.stringValue(),
                        tagButtonImageURL == null ? null : tagButtonImageURL.stringValue(),
                        tagButtonComment == null ? null : tagButtonComment.stringValue(),
                        tagButtonVisibleForPublic.booleanValue()));
            }
        }
        return result;
    }
}