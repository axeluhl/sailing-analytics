package com.sap.sse.gwt.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.http.client.URL;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

public class SettingsToStringSerializer {
    private final SettingsToStringMapSerializer settingsToStringMapSerializer = new SettingsToStringMapSerializer();

    public String fromSettings(GenericSerializableSettings settings) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        return writeParameterMapToString(serializedValues);
    }

    public String fromSettings(SettingsMap settings) {
        Map<String, Iterable<String>> serializedValues = settingsToStringMapSerializer.serialize(settings);
        return writeParameterMapToString(serializedValues);
    }

    private String writeParameterMapToString(Map<String, Iterable<String>> serializedValues) {
        String settingsString = "";
        for (Entry<String, Iterable<String>> val : serializedValues.entrySet()) {
            String ekey = URL.encode(val.getKey());
            for (String vval : val.getValue()) {
                String evval = URL.encode(vval);
                if (!settingsString.isEmpty()) {
                    settingsString += "&";
                }
                settingsString += ekey + "=" + evval;
            }
        }
        return settingsString;
    }

    public final <T extends GenericSerializableSettings> T fromString(String serializedSettings, T settings) {
        Map<String, Iterable<String>> values = deserializeStringToMap(serializedSettings);
        return settingsToStringMapSerializer.deserialize(settings, values);
    }

    public final <T extends SettingsMap> T fromString(String serializedSettings, T settings) {
        Map<String, Iterable<String>> values = deserializeStringToMap(serializedSettings);
        return settingsToStringMapSerializer.deserializeSettingsMap(settings, values);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Iterable<String>> deserializeStringToMap(String serializedSettings) {
        Map<String, Iterable<String>> values = new HashMap<>();
        String[] singleSettings = serializedSettings.split("&");
        for (String entry : singleSettings) {
            String[] entryParts = entry.split("=");
            // TODO determine what kind of error handling could be done here, if the url was wrongly edited manually
            String key = URL.decode(entryParts[0]);
            String value = URL.decode(entryParts[1]);
            Iterable<String> listOrNull = values.get(key);
            if(listOrNull == null){
                listOrNull = new ArrayList<>();
                values.put(key, listOrNull);
            }
            // can only be a arraylist at this point
            ((ArrayList)listOrNull).add(value);
            
        }

        return values;
    }

}
