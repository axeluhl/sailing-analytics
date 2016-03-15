package com.sap.sse.settings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.settings.EnumSetting;
import com.sap.sse.common.settings.ListSetting;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.serializer.AbstractSettingsToJsonSerializer;

/**
 * Serializes a {@link Settings} object to a {@link JSONObject}. All setting types are supported as top-level entities.
 * Nesting of {@link Settings} is generally supported. For example, a {@link Settings} object can be contains as one
 * setting within another {@link Settings} object. However, when nesting {@link EnumSetting} objects within a
 * {@link ListSetting} then it is mandatory to put this {@link ListSetting} object into a {@link Settings} object's
 * {@link Settings#getNonDefaultSettings()}, such as into a {@link MapSettings} object. The technical reason for this
 * limitation is that the {@link Class} object for the enumeration type must be stored with the serialized format in
 * order to re-construct the enumeration literals of the correct type. For readability of the produced JSON output,
 * this implementation chooses to store the enumeration class's name in a special property that is sibling of the
 * enumeration property. A {@link ListSetting} object does not contain named properties and therefore does not allow
 * us to add such a property easily.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SettingsToJsonSerializer extends AbstractSettingsToJsonSerializer<JSONObject, JSONArray> {

    @Override
    protected JSONObject newOBJECT() {
        return new JSONObject();
    }

    @Override
    protected void set(JSONObject jsonObject, String property, Object value) {
        jsonObject.put(property, value);
    }

    @Override
    protected Object get(JSONObject jsonObject, String property) {
        return jsonObject.get(property);
    }
    
    @Override
    protected boolean hasProperty(JSONObject jsonObject, String property) {
        return jsonObject.containsKey(property);
    }
    
    @Override
    protected JSONArray ToJsonArray(Iterable<Object> values) {
        JSONArray jsonArray = new JSONArray();
        for(Object value : values) {
            jsonArray.add(value);
        }
        return jsonArray;
    }

    @Override
    protected Iterable<Object> fromJsonArray(JSONArray jsonArray) {
        return jsonArray;
    }
}
