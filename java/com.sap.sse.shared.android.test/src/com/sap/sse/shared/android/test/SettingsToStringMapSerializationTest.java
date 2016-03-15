package com.sap.sse.shared.android.test;

import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.serializer.SettingsToStringMapSerializer;

public class SettingsToStringMapSerializationTest extends AbstractJsonSerializationTest<Map<String,Iterable<String>>> {
    
    private final SettingsToStringMapSerializer serializer = new SettingsToStringMapSerializer();
    
    @Override
    protected <T extends Settings> Map<String,Iterable<String>> serialize(T settings) throws Exception {
        return serializer.serialize(settings);
    }
    
    @Override
    protected <T extends Settings> T deserialize(Map<String,Iterable<String>> serializedObject, T settings) throws Exception {
        return serializer.deserialize(settings, serializedObject);
    }
}
