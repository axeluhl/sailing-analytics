package com.sap.sse.shared.android.test;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.shared.settings.SettingsToJsonSerializer;

public class SettingsJsonSerializationTest extends AbstractJsonSerializationTest<String> {
    
    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
    
    @Override
    protected <T extends Settings> String serialize(T settings) throws Exception {
        // Do not change this to return a JSONOBject instance.
        // JSONOBject is just a HashMap and you only know if it is really JSON serializable if you do it!
        return serializer.serialize(settings).toJSONString();
    }
    
    @Override
    protected <T extends Settings> T deserialize(String serializedObject, T settings) throws Exception {
        return serializer.deserialize(settings, (JSONObject) new JSONParser().parse(serializedObject));
    }
}
