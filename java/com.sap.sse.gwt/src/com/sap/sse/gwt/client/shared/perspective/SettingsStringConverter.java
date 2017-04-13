package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

public class SettingsStringConverter {

    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();
    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();
    
    @SuppressWarnings("unchecked")
    public<S extends Settings> S deserializeFromCurrentUrl(S defaultSettings) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) urlSerializer
                    .deserializeFromCurrentLocation((GenericSerializableSettings) defaultSettings);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) urlSerializer
                    .deserializeSettingsMapFromCurrentLocation((SettingsMap) defaultSettings);
        }
        return defaultSettings;
    }
    
    @SuppressWarnings("unchecked")
    public<S extends Settings> S deserializeFromJson(S defaultSettings, JSONObject jsonToDeserialize) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) defaultSettings,
                    jsonToDeserialize);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) jsonSerializer.deserialize((SettingsMap) defaultSettings, jsonToDeserialize);
        }
        return defaultSettings;
    }

    public SettingsJsons convertToSettingsJson(SettingsStrings settingsStrings) {
        JSONObject globalSettingsJson = convertStringToJson(settingsStrings.getGlobalSettingsString());
        JSONObject contextSpecificSettingsJson = convertStringToJson(settingsStrings.getContextSpecificSettingsString());
        return new SettingsJsons(globalSettingsJson, contextSpecificSettingsJson);
    }
    
    public JSONObject convertStringToJson(String str) {
        return str == null ? null : jsonSerializer.parseStringToJsonObject(str);
    }
    
    public String convertJsonToString(JSONObject json) {
        return json == null ? null : jsonSerializer.jsonObjectToString(json);
    }
    
    public SettingsStrings convertToSettingsStrings(SettingsJsons settingsJsons) {
        String globalSettingsString = convertJsonToString(settingsJsons.getGlobalSettingsJson());
        String contextSpecificSettingsString = convertJsonToString(settingsJsons.getContextSpecificSettingsJson());
        return new SettingsStrings(globalSettingsString, contextSpecificSettingsString);
    }
    
    public JSONValue serializeFromSettingsObject(Settings newSettings) {
        if (newSettings instanceof GenericSerializableSettings) {
            return jsonSerializer.serialize((GenericSerializableSettings) newSettings);
        }
        throw new IllegalStateException("Requested save of settings that is not Serializable!");
    }
    
}
