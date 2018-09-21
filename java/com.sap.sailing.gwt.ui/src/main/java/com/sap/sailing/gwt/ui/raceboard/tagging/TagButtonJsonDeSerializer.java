package com.sap.sailing.gwt.ui.raceboard.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.sap.sailing.gwt.ui.client.GwtJsonDeSerializer;

/**
 * Serializes and deserializes {@link TagButton tag-buttons} to save them in the {@link com.sap.sse.security.UserStore
 * UserStore}.
 */
// TODO: Don't use key for tag-buttons, save array directly instead.
public class TagButtonJsonDeSerializer implements GwtJsonDeSerializer<List<TagButton>> {

    private static final String FIELD_TAG_BUTTONS = "tagButtons";
    private static final String FIELD_BUTTON_NAME = "buttonName";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_IMAGE_URL = "imageURL";
    private static final String FIELD_VISIBLE_FOR_PUBLIC = "public";

    @Override
    public JSONObject serialize(List<TagButton> tagButtons) {
        JSONObject result = new JSONObject();

        JSONArray tagButtonsArray = new JSONArray();
        result.put(FIELD_TAG_BUTTONS, tagButtonsArray);

        int i = 0;
        for (TagButton button : tagButtons) {
            JSONObject tagButtonObject = new JSONObject();
            tagButtonsArray.set(i++, tagButtonObject);
            tagButtonObject.put(FIELD_BUTTON_NAME, new JSONString(button.getText()));
            tagButtonObject.put(FIELD_TAG, new JSONString(button.getTag()));
            tagButtonObject.put(FIELD_COMMENT, new JSONString(button.getComment()));
            tagButtonObject.put(FIELD_IMAGE_URL, new JSONString(button.getImageURL()));
            tagButtonObject.put(FIELD_VISIBLE_FOR_PUBLIC, JSONBoolean.getInstance(button.isVisibleForPublic()));
        }
        return result;
    }

    @Override
    public List<TagButton> deserialize(JSONObject rootObject) {
        List<TagButton> result = null;
        if (rootObject != null) {
            result = new ArrayList<TagButton>();
            JSONArray tagButtonsArray = (JSONArray) rootObject.get(FIELD_TAG_BUTTONS);
            for (int i = 0; i < tagButtonsArray.size(); i++) {
                JSONObject tagButtonValue = (JSONObject) tagButtonsArray.get(i);
                JSONString tagButtonName = (JSONString) tagButtonValue.get(FIELD_BUTTON_NAME);
                JSONString tagButtonTag = (JSONString) tagButtonValue.get(FIELD_TAG);
                JSONString tagButtonComment = (JSONString) tagButtonValue.get(FIELD_COMMENT);
                JSONString tagButtonImageURL = (JSONString) tagButtonValue.get(FIELD_IMAGE_URL);
                JSONBoolean tagButtonVisibleForPublic = (JSONBoolean) tagButtonValue.get(FIELD_VISIBLE_FOR_PUBLIC);
                result.add(new TagButton(tagButtonName.stringValue(), tagButtonTag.stringValue(),
                        tagButtonImageURL.stringValue(), tagButtonComment.stringValue(),
                        tagButtonVisibleForPublic.booleanValue()));
            }
        }
        return result;
    }

}