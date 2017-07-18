package com.sap.sse.shared.android.test;

import java.util.Map;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

public class SettingsToStringMapSerializationTest extends AbstractSettingsSerializationTestWithSettingsMap<Map<String,Iterable<String>>> {
    
    private final SettingsToStringMapSerializer serializer = new SettingsToStringMapSerializer();
    
    @Override
    protected <T extends GenericSerializableSettings> Map<String,Iterable<String>> serialize(T settings) throws Exception {
        return serializer.serialize(settings);
    }
    
    @Override
    protected <T extends GenericSerializableSettings> T deserialize(Map<String,Iterable<String>> serializedObject, T settings) throws Exception {
        return serializer.deserialize(settings, serializedObject);
    }

    @Override
    protected SettingsMap deserialize(Map<String, Iterable<String>> serializedObject, SettingsMap settingsMap)
            throws Exception {
        return serializer.deserializeSettingsMap(settingsMap, serializedObject);
    }

    @Override
    protected Map<String, Iterable<String>> serialize(SettingsMap settings) throws Exception {
        return serializer.serialize(settings);
    }
}
