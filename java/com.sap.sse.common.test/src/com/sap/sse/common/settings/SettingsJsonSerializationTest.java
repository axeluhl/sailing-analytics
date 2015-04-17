package com.sap.sse.common.settings;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sse.common.filter.TextOperator;

public class SettingsJsonSerializationTest {
    @Test
    public void testFlatJsonSerialization() throws ClassNotFoundException {
        final Map<String, Setting> settingsMap = new HashMap<>();
        settingsMap.put("humba", new StringSetting("trala"));
        settingsMap.put("trala", new EnumSetting<>(TextOperator.Operators.Contains));
        settingsMap.put("num", new NumberSetting(BigDecimal.TEN));
        settingsMap.put("l", new ListSetting<NumberSetting>(Arrays.asList(new NumberSetting(1), new NumberSetting(2), new NumberSetting(3))));
        Settings settings = new MapSettings(settingsMap);
        SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
        JSONObject json = serializer.serialize(settings);
        Settings deserializedSettings = serializer.deserialize(json);
        assertEquals(settingsMap, deserializedSettings.getNonDefaultSettings());
    }

    @Test
    public void testNestedJsonSerialization() throws ClassNotFoundException {
        final Map<String, Setting> settingsMap = new HashMap<>();
        final Map<String, Setting> innerMap = new HashMap<>();
        settingsMap.put("humba", new StringSetting("trala"));
        innerMap.put("num", new NumberSetting(123.4));
        settingsMap.put("map", new MapSettings(innerMap));
        Settings settings = new MapSettings(settingsMap);
        SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
        JSONObject json = serializer.serialize(settings);
        Settings deserializedSettings = serializer.deserialize(json);
        assertEquals(settingsMap, deserializedSettings.getNonDefaultSettings());
    }

    /**
     * A white-box test that tests property name escaping in case the property name conflicts with how type
     * names (used particularly for enum settings) are represented in JSON.
     */
    @Test
    public void testJsonSerializationWithFunnyPropertyName() throws ClassNotFoundException {
        final Map<String, Setting> settingsMap = new HashMap<>();
        settingsMap.put("humba___TYPE", new StringSetting("trala"));
        settingsMap.put("trala___TYPE___TYPE", new EnumSetting<>(TextOperator.Operators.Contains));
        Settings settings = new MapSettings(settingsMap);
        SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
        JSONObject json = serializer.serialize(settings);
        Settings deserializedSettings = serializer.deserialize(json);
        assertEquals(settingsMap, deserializedSettings.getNonDefaultSettings());
    }
}
