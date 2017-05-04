package com.sap.sse.gwt.client.shared.settings;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

/**
 * Conversion helper which is used by this instance for type conversion/serialization between settings objects and JSON
 * Strings. The class encapsulates all serializers which are required for serialization and deserialization and provides
 * methods for an indirect serialization/deserialization functionality required by {@link SettingsBuildingPipeline} and
 * {@link ComponentContext} implementations.
 * 
 * @author Vladislav Chumak
 * 
 */
public class SettingsSerializationHelper {

    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();
    private final SettingsToJsonSerializerGWT jsonSerializer = new SettingsToJsonSerializerGWT();

    @SuppressWarnings("unchecked")
    public <S extends Settings> S deserializeFromCurrentUrl(S defaultSettings) {
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
    public <S extends Settings> S deserializeFromJson(S defaultSettings, JSONObject jsonToDeserialize) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) jsonSerializer.deserialize((GenericSerializableSettings) defaultSettings,
                    jsonToDeserialize);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) jsonSerializer.deserialize((SettingsMap) defaultSettings, jsonToDeserialize);
        }
        return defaultSettings;
    }

    public JSONObject convertStringToJson(String str) {
        return str == null ? null : jsonSerializer.parseStringToJsonObject(str);
    }

    public String convertJsonToString(JSONObject json) {
        return json == null ? null : jsonSerializer.jsonObjectToString(json);
    }

    public JSONValue serializeFromSettingsObject(Settings newSettings) {
        if (newSettings instanceof GenericSerializableSettings) {
            return jsonSerializer.serialize((GenericSerializableSettings) newSettings);
        }
        throw new IllegalStateException("Requested save of settings that is not Serializable!");
    }

}
