package com.sap.sse.shared.android.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;

public abstract class AbstractSettingsSerializationTestWithSettingsMap<SOT> extends AbstractSettingsSerializationTest<SOT> {

    @Test
    public void testSettingsMapWithOneSerializableSettings() throws Exception {
        final GenericSerializableSettings settingsValues = createTestSettingsWithValues();
        
        SettingsMap settingsMapToSave = new SettingsMapImpl("aaa", settingsValues);

        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        Settings deserializedSettingsValues = deserializedSettingsMap.getSettingsByKey().get("aaa");
        assertEquals(deserializedSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 1);
    }
    
    @Test
    public void testSettingsMapWithMultipleSerializableSettings() throws Exception {
        
        final GenericSerializableSettings settingsValues1 = createTestSettingsWithValues();
        final GenericSerializableSettings settingsValues2 = createTestSettingsWithValues2();
        Map<String, Settings> settings = new HashMap<>();
        settings.put("aaa", settingsValues1);
        settings.put("bbb", settingsValues2);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(settings);

        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        Settings deserializedSettingsValues1 = deserializedSettingsMap.getSettingsByKey().get("aaa");
        Settings deserializedSettingsValues2 = deserializedSettingsMap.getSettingsByKey().get("bbb");
        assertEquals(deserializedSettingsValues1, createTestSettingsWithValues());
        assertEquals(deserializedSettingsValues2, createTestSettingsWithValues2());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 2);
    }
    
    @Test
    public void testSettingsMapWithNestedSettingsMapWithOneSerializableSettingsChild() throws Exception {
        final GenericSerializableSettings childSettingsValues = createTestSettingsWithValues();
        SettingsMap childSettingsMap = new SettingsMapImpl("aaa", childSettingsValues);
        SettingsMap settingsMapToSave = new SettingsMapImpl("bbb", childSettingsMap);

        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        SettingsMap deserializedChildSettingsMap = (SettingsMap) deserializedSettingsMap.getSettingsByKey().get("bbb");
        Settings deserializedChildSettingsValues = deserializedChildSettingsMap.getSettingsByKey().get("aaa");
        assertEquals(deserializedChildSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 1);
        assertEquals(deserializedChildSettingsMap.getSettingsByKey().size(), 1);
    }
    
    @Test
    public void testSettingsMapWithOneSerializableSettingsAndNestedSettingsMapWithOneSerializableSettings() throws Exception {
        final GenericSerializableSettings childSettingsValues = createTestSettingsWithValues2();
        SettingsMap childSettingsMap = new SettingsMapImpl("aaa", childSettingsValues);
        
        final GenericSerializableSettings settingsValues = createTestSettingsWithValues();
        Map<String, Settings> settings = new HashMap<>();
        settings.put("aaa", settingsValues);
        settings.put("bbb", childSettingsMap);
        final SettingsMap settingsMapToSave = new SettingsMapImpl(settings);
        
        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        SettingsMap deserializedChildSettingsMap = (SettingsMap) deserializedSettingsMap.getSettingsByKey().get("bbb");
        Settings deserializedSettingsValues = deserializedSettingsMap.getSettingsByKey().get("aaa");
        Settings deserializedChildSettingsValues = deserializedChildSettingsMap.getSettingsByKey().get("aaa");
        
        assertEquals(deserializedSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedChildSettingsValues, createTestSettingsWithValues2());
        
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 2);
        assertEquals(deserializedChildSettingsMap.getSettingsByKey().size(), 1);
    }
    
    @Test
    public void testSettingsMapWithOneNonSerializableSettings() throws Exception {
        final Settings settingsValuesNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        
        SettingsMap settingsMapToSave = new SettingsMapImpl("aaa", settingsValuesNonSerializable);

        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        
        Settings deserializedSettingsValues = deserializedSettingsMap.getSettingsByKey().get("aaa");
        assertEquals(deserializedSettingsValues, createNonSerializableTestSettingsWithDefaultValues());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 1);
    }
    
    @Test
    public void testSettingsMapWithOneSerializableAndOneNonSerializableSettings() throws Exception {
        
        final GenericSerializableSettings childSettingsValues = createTestSettingsWithValues();
        final Settings childSettingsValuesNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<String, Settings> settings = new HashMap<>();
        settings.put("aaa", childSettingsValues);
        settings.put("bbb", childSettingsValuesNonSerializable);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(settings);
        
        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        Settings deserializedSettingsValues = deserializedSettingsMap.getSettingsByKey().get("aaa");
        Settings deserializedSettingsValuesNonSerializable = deserializedSettingsMap.getSettingsByKey().get("bbb");
        assertEquals(deserializedSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedSettingsValuesNonSerializable, createNonSerializableTestSettingsWithDefaultValues());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 2);
    }
    
    @Test
    public void testSettingsMapWithNestedSerializableAndNonSerializableSettings() throws Exception {
        
        final GenericSerializableSettings childSettingsValues = createTestSettingsWithValues();
        final Settings childSettingsNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<String, Settings> childSettings = new HashMap<>();
        childSettings.put("aaa", childSettingsValues);
        childSettings.put("bbb", childSettingsNonSerializable);
        
        SettingsMap childSettingsMap = new SettingsMapImpl(childSettings);
        
        final GenericSerializableSettings settingsValues = createTestSettingsWithValues();
        final Settings settingsValuesNonSerializable = createNonSerializableTestSettingsWithChangedValues();
        Map<String, Settings> settings = new HashMap<>();
        settings.put("aaa", childSettingsMap);
        settings.put("bbb", settingsValuesNonSerializable);
        settings.put("ccc", settingsValues);
        
        SettingsMap settingsMapToSave = new SettingsMapImpl(settings);
        
        final SettingsMap deserializedSettingsMap = serializeAndDeserialize(settingsMapToSave);
        SettingsMap deserializedChildSettingsMap = (SettingsMap) deserializedSettingsMap.getSettingsByKey().get("aaa");
        Settings deserializedSettingsValuesNonSerializable = deserializedSettingsMap.getSettingsByKey().get("bbb");
        Settings deserializedSettingsValues = deserializedSettingsMap.getSettingsByKey().get("ccc");
        
        assertEquals(deserializedSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedSettingsValuesNonSerializable, createNonSerializableTestSettingsWithDefaultValues());
        assertEquals(deserializedSettingsMap.getSettingsByKey().size(), 3);
        
        Settings deserializedChildSettingsValues = deserializedChildSettingsMap.getSettingsByKey().get("aaa");
        Settings deserializedChildSettingsValuesNonSerializable = deserializedChildSettingsMap.getSettingsByKey().get("bbb");
        
        assertEquals(deserializedChildSettingsValues, createTestSettingsWithValues());
        assertEquals(deserializedChildSettingsValuesNonSerializable, createNonSerializableTestSettingsWithDefaultValues());
        assertEquals(deserializedChildSettingsMap.getSettingsByKey().size(), 2);
    }
    
    private static class SettingsMapImpl implements SettingsMap {
        private final Map<String, Settings> innerSettings;
        
        public SettingsMapImpl(String key, Settings settings) {
            innerSettings = new HashMap<>();
            innerSettings.put(key, settings);
        }
        
        public SettingsMapImpl(Map<String, Settings> settings) {
            innerSettings = new HashMap<>(settings);
        }

        @Override
        public Map<String, Settings> getSettingsByKey() {
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
        Map<String, Settings> innerMap = new HashMap<>();
        for(Map.Entry<String, Settings> entry : settingsMap.getSettingsByKey().entrySet()) {
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
