package com.sap.sse.shared.android.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sse.common.filter.TextOperator;
import com.sap.sse.common.filter.TextOperator.Operators;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.DecimalListSetting;
import com.sap.sse.common.settings.DecimalSetting;
import com.sap.sse.common.settings.EnumListSetting;
import com.sap.sse.common.settings.EnumSetting;
import com.sap.sse.common.settings.StringSetting;
import com.sap.sse.common.settings.StringToEnumConverter;
import com.sap.sse.shared.settings.SettingsToJsonSerializer;

public class SettingsJsonSerializationTest {
    
    private static class TestOuterSettings extends AbstractSettings {
        private TestSettings nested = new TestSettings("nested", this);
        
    }
    
    private static class TestSettings extends AbstractSettings {
        private final StringSetting humba = new StringSetting("humba", this);
        // TODO user lambda: TextOperator.Operators::valueOf
        private final EnumSetting<TextOperator.Operators> trala = new EnumSetting<>("trala", this, new StringToEnumConverter<TextOperator.Operators>() {
            @Override
            public Operators fromString(String stringValue) {
                return TextOperator.Operators.valueOf(stringValue);
            }
        });
        private final DecimalSetting num = new DecimalSetting("num", this);
        private final DecimalListSetting l = new DecimalListSetting("l", this);
        
        public TestSettings() {
        }
        public TestSettings(String name, AbstractSettings settings) {
            super(name, settings);
        }
    }
    
    private static class TestEnumListSettings extends AbstractSettings {
        private final EnumListSetting<TextOperator.Operators> l = new EnumListSetting<>("l", this, new StringToEnumConverter<TextOperator.Operators>() {
            @Override
            public Operators fromString(String stringValue) {
                return TextOperator.Operators.valueOf(stringValue);
            }
        });
    }
    
    private static class DuplicateFieldSettings extends AbstractSettings {
        @SuppressWarnings("unused")
        private final StringSetting humba = new StringSetting("humba", this);
        @SuppressWarnings("unused")
        private final DecimalSetting bumba = new DecimalSetting("humba", this);
    }
    
    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
    
    private JSONObject serializeAndDeserialize(JSONObject jsonObject) {
        String serializedJson = jsonObject.toJSONString();
        try {
            return (JSONObject) new JSONParser().parse(serializedJson);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testFlatJsonSerialization() {
        final TestSettings settings = new TestSettings();
        settings.humba.setValue("trala");
        settings.trala.setValue(TextOperator.Operators.Contains);
        settings.num.setValue(BigDecimal.TEN);
        settings.l.setValues(Arrays.asList(BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)));
        
        final TestSettings deserializedSettings = serializer.deserialize(new TestSettings(), serializeAndDeserialize(serializer.serialize(settings)));
        assertEquals(settings, deserializedSettings);
    }

    @Test
    public void testNestedJsonSerialization() {
        final TestOuterSettings outerSettings = new TestOuterSettings();
        final TestSettings settings = outerSettings.nested;
        settings.humba.setValue("trala");
        settings.trala.setValue(TextOperator.Operators.Contains);
        settings.num.setValue(BigDecimal.TEN);
        settings.l.setValues(Arrays.asList(BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)));
        
        final TestOuterSettings deserializedSettings = serializer.deserialize(new TestOuterSettings(), serializeAndDeserialize(serializer.serialize(outerSettings)));
        assertEquals(outerSettings, deserializedSettings);
    }
    
    /**
     * Verifies that it is not possible to have two equally named child settings that would cause conflicts on serialization.
     */
    @Test(expected = RuntimeException.class)
    public void testDuplicateSetting() {
        new DuplicateFieldSettings();
    }

//    /**
//     * A white-box test that tests property name escaping in case the property name conflicts with how type
//     * names (used particularly for enum settings) are represented in JSON.
//     */
//    @Test
//    public void testJsonSerializationWithFunnyPropertyName() throws ClassNotFoundException {
//        final Map<String, Setting> settingsMap = new HashMap<>();
//        settingsMap.put("humba___TYPE", new StringSetting("trala"));
//        settingsMap.put("trala___TYPE___TYPE", new EnumSetting<>(TextOperator.Operators.Contains));
//        Settings settings = new MapSettings(settingsMap);
//        SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
//        JSONObject json = serializer.serialize(settings);
//        Settings deserializedSettings = serializer.deserialize(json);
//        assertEquals(settingsMap, deserializedSettings.getNonDefaultSettings());
//    }

    /**
     * A white-box test that tests property name escaping in case the property name conflicts with how type
     * names (used particularly for enum settings) are represented in JSON.
     */
    @Test
    public void testJsonSerializationWithEnumTypedList() throws ClassNotFoundException {
        final TestEnumListSettings settings = new TestEnumListSettings();
        settings.l.setValues(Arrays.asList(Operators.Contains, Operators.EndsWith));
        
        final TestEnumListSettings deserializedSettings = serializer.deserialize(new TestEnumListSettings(), serializeAndDeserialize(serializer.serialize(settings)));
        assertEquals(settings, deserializedSettings);
    }
    
//    /**
//     * A white-box test that tests property name escaping in case the property name conflicts with how type
//     * names (used particularly for enum settings) are represented in JSON.
//     */
//    @Test
//    public void testJsonSerializationWithStringifiedEnum() throws ClassNotFoundException {
//        final Map<String, Setting> settingsMap = new HashMap<>();
//        final List<StringSetting> list = new ArrayList<>();
//        list.add(new StringSetting(Operators.Contains));
//        list.add(new StringSetting(Operators.EndsWith));
//        settingsMap.put("l", new ListSetting<StringSetting>(list));
//        Settings settings = new MapSettings(settingsMap);
//        SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
//        JSONObject json = serializer.serialize(settings);
//        Settings deserializedSettings = serializer.deserialize(json);
//        ListSetting<?> l = (ListSetting<?>) deserializedSettings.getNonDefaultSettings().get("l");
//        assertEquals(Arrays.asList(Operators.Contains, Operators.EndsWith), l.getEnumList(Operators.class));
//    }

}
