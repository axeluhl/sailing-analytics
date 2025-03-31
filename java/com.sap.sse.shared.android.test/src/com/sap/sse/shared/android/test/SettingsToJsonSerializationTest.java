package com.sap.sse.shared.android.test;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.shared.settings.SettingsToJsonSerializer;

public class SettingsToJsonSerializationTest extends AbstractSettingsSerializationTestWithSettingsMap<String> {
    
    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
    
    @Override
    protected <T extends GenericSerializableSettings> String serialize(T settings) throws Exception {
        // Do not change this to return a JSONOBject instance.
        // JSONOBject is just a HashMap and you only know if it is really JSON serializable if you do it!
        return serializer.serializeToString(settings);
    }
    
    @Override
    protected <T extends GenericSerializableSettings> T deserialize(String serializedObject, T settings) throws Exception {
        return serializer.deserialize(settings, serializedObject);
    }

    @Override
    protected SettingsMap deserialize(String serializedObject, SettingsMap settingsMap) throws Exception {
        return serializer.deserialize(settingsMap, serializedObject);
    }

    @Override
    protected String serialize(SettingsMap settings) throws Exception {
        // Do not change this to return a JSONOBject instance.
        // JSONOBject is just a HashMap and you only know if it is really JSON serializable if you do it!
        return serializer.serializeToString(settings);
    }
}
