package com.sap.sse.shared.android.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;

public abstract class AbstractSettingsSerializationTestWithSettingsMap<SOT> extends AbstractSettingsSerializationTest<SOT> {

    @Test
    public void testSettingsMapWithOneSerializableSettings() throws Exception {
        final GenericSerializableSettings settings = createTestSettingsWithValues();
        
        SettingsMap settingsMapToSave = new SettingsMapImpl("aaa", settings);

        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithMultipleSerializableSettings() throws Exception {
        
        final GenericSerializableSettings childSettings = createTestSettingsWithValues();
        final GenericSerializableSettings childSettings2 = createTestSettingsWithValues2();
        Map<Serializable, Settings> settings = new HashMap<>();
        settings.put("aaa", childSettings);
        settings.put("bbb", childSettings2);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(settings);

        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithNestedSettingsMapWithOneSerializableSettingsChild() throws Exception {
        final GenericSerializableSettings settings = createTestSettingsWithValues();
        SettingsMap childSettingsMap = new SettingsMapImpl("aaa", settings);
        SettingsMap settingsMapToSave = new SettingsMapImpl("bbb", childSettingsMap);

        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithOneSerializableSettingsAndNestedSettingsMapWithOneSerializableSettings() throws Exception {
        final GenericSerializableSettings nestedMapSettingsChild = createTestSettingsWithValues2();
        SettingsMap nestedSettingsMap = new SettingsMapImpl("aaa", nestedMapSettingsChild);
        
        final GenericSerializableSettings rootSettingsChild = createTestSettingsWithValues();
        Map<Serializable, Settings> rootSettings = new HashMap<>();
        rootSettings.put("aaa", rootSettingsChild);
        rootSettings.put("bbb", nestedSettingsMap);
        final SettingsMap rootSettingsMapToSave = new SettingsMapImpl(rootSettings);
        
        final SettingsMap deserializedSettings = serializeAndDeserialize(rootSettingsMapToSave);
        assertEquals(rootSettingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithOneNonSerializableSettings() throws Exception {
        final Settings settings = createNonSerializableTestSettingsWithChangedValues();
        
        SettingsMap settingsMapToSave = new SettingsMapImpl("aaa", settings);

        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        
        final Settings defaultSettings = createNonSerializableTestSettingsWithDefaultValues();
        settingsMapToSave.getSettingsByKey().put("aaa", defaultSettings);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithOneSerializableAndOneNonSerializableSettings() throws Exception {
        
        final GenericSerializableSettings childSettings = createTestSettingsWithValues();
        final Settings childSettingsNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<Serializable, Settings> settings = new HashMap<>();
        settings.put("aaa", childSettings);
        settings.put("bbb", childSettingsNonSerializable);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(settings);
        
        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        
        final Settings defaultNonSerializableSettings = createNonSerializableTestSettingsWithDefaultValues();
        settingsMapToSave.getSettingsByKey().put("bbb", defaultNonSerializableSettings);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    @Test
    public void testSettingsMapWithNestedSerializableAndNonSerializableSettings() throws Exception {
        
        final GenericSerializableSettings childSettings = createTestSettingsWithValues();
        final Settings childSettingsNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<Serializable, Settings> nestedSettings = new HashMap<>();
        nestedSettings.put("aaa", childSettings);
        nestedSettings.put("bbb", childSettingsNonSerializable);
        
        SettingsMap nestedSettingsMap = new SettingsMapImpl(nestedSettings);
        
        final GenericSerializableSettings settingsSerializable = createTestSettingsWithValues();
        final Settings settingsNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<Serializable, Settings> rootSettings = new HashMap<>();
        rootSettings.put("aaa", nestedSettingsMap);
        rootSettings.put("bbb", settingsNonSerializable);
        rootSettings.put("ccc", settingsSerializable);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(rootSettings);
        
        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsMapToSave);
        
        final Settings defaultNonSerializableSettings = createNonSerializableTestSettingsWithDefaultValues();
        settingsMapToSave.getSettingsByKey().put("bbb", defaultNonSerializableSettings);
        final Settings nestedDefaultNonSerializableSettings = createNonSerializableTestSettingsWithDefaultValues();
        ((SettingsMap) settingsMapToSave.getSettingsByKey().get("aaa")).getSettingsByKey().put("bbb", nestedDefaultNonSerializableSettings);
        assertEquals(settingsMapToSave, deserializedSettings);
    }
    
    private static class SettingsMapImpl implements SettingsMap {
        private final Map<Serializable, Settings> innerSettings;
        
        public SettingsMapImpl(String key, Settings settings) {
            innerSettings = new HashMap<>();
            innerSettings.put(key, settings);
        }
        
        public SettingsMapImpl(Map<Serializable, Settings> settings) {
            innerSettings = new HashMap<>(settings);
        }

        @Override
        public Map<Serializable, Settings> getSettingsByKey() {
            return innerSettings;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((innerSettings == null) ? 0 : innerSettings.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SettingsMapImpl other = (SettingsMapImpl) obj;
            if (innerSettings == null) {
                if (other.innerSettings != null)
                    return false;
            } else if (!innerSettings.equals(other.innerSettings))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "SettingsMapImpl [innerSettings=" + innerSettings + "]";
        }
    }
    
    protected abstract SettingsMap deserialize(SOT serializedObject, SettingsMap settingsMap) throws Exception;
    
    protected abstract SOT serialize(SettingsMap settings) throws Exception;
    
    private SettingsMap serializeAndDeserialize(SettingsMap settingsMap) throws Exception {
        SOT serialized = serialize(settingsMap);
        SettingsMap reconstructedSettingsMap = reconstructSettingsMap(settingsMap);
        return deserialize(serialized, reconstructedSettingsMap);
    }

    private SettingsMap reconstructSettingsMap(SettingsMap settingsMap) throws Exception {
        Map<Serializable, Settings> innerMap = new HashMap<>();
        for(Map.Entry<Serializable, Settings> entry : settingsMap.getSettingsByKey().entrySet()) {
            Settings innerSettings = entry.getValue();
            Settings copiedSettings;
            if(innerSettings instanceof SettingsMap) {
                copiedSettings = reconstructSettingsMap((SettingsMap)innerSettings);
            } else {
                copiedSettings = innerSettings.getClass().newInstance();
            }
            innerMap.put(entry.getKey(), copiedSettings);
        }
        return new SettingsMapImpl(innerMap);
    }
}
