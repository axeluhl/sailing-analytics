package com.sap.sse.shared.android.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.sap.sse.common.filter.TextOperator;
import com.sap.sse.common.filter.TextOperator.Operators;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.DecimalListSetting;
import com.sap.sse.common.settings.DecimalSetting;
import com.sap.sse.common.settings.EnumListSetting;
import com.sap.sse.common.settings.EnumSetting;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.StringSetting;
import com.sap.sse.common.settings.StringToEnumConverter;

public abstract class AbstractJsonSerializationTest<SOT> {
    
    private static class TestOuterSettings extends AbstractSettings {
        private TestSettings nested = new TestSettings("nested", this);
        public TestOuterSettings() {
        }
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
        public TestEnumListSettings() {
        }
    }
    
    private static class DuplicateFieldSettings extends AbstractSettings {
        @SuppressWarnings("unused")
        private final StringSetting humba = new StringSetting("humba", this);
        @SuppressWarnings("unused")
        private final DecimalSetting bumba = new DecimalSetting("humba", this);
    }
    
    protected abstract <T extends Settings> SOT serialize(T settings) throws Exception;
    
    protected abstract <T extends Settings> T deserialize(SOT serializedObject, T settings) throws Exception;
    
    private <T extends Settings> T serializeAndDeserialize(T objectToSerialize) {
        try {
            SOT serialized = serialize(objectToSerialize);
            
            @SuppressWarnings("unchecked")
            Class<T> settingsType = (Class<T>) objectToSerialize.getClass();
            T deserializedInstance = settingsType.newInstance();
            
            return deserialize(serialized, deserializedInstance);
        } catch (Exception e) {
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
        
        final TestSettings deserializedSettings = serializeAndDeserialize(settings);
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
        
        final TestOuterSettings deserializedSettings = serializeAndDeserialize(outerSettings);
        assertEquals(outerSettings, deserializedSettings);
    }
    
    /**
     * Verifies that it is not possible to have two equally named child settings that would cause conflicts on serialization.
     */
    @Test(expected = RuntimeException.class)
    public void testDuplicateSetting() {
        new DuplicateFieldSettings();
    }

    /**
     * A white-box test that tests property name escaping in case the property name conflicts with how type
     * names (used particularly for enum settings) are represented in JSON.
     */
    @Test
    public void testJsonSerializationWithEnumTypedList() throws ClassNotFoundException {
        final TestEnumListSettings settings = new TestEnumListSettings();
        settings.l.setValues(Arrays.asList(Operators.Contains, Operators.EndsWith));
        
        final TestEnumListSettings deserializedSettings = serializeAndDeserialize(settings);
        assertEquals(settings, deserializedSettings);
    }
}
