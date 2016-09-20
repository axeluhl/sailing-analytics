package com.sap.sse.shared.android.test;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;

public abstract class AbstractSettingsSerializationTestWithSettingsMap<SOT> extends AbstractSettingsSerializationTest<SOT> {

    
    private static class SettingsMapImpl implements SettingsMap {
        private final Map<Serializable, Settings> innerSettings;
        
        public SettingsMapImpl(String key, Settings settings) {
            innerSettings = new HashMap<>(Collections.singletonMap(key, settings));
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
    
    @Test
    public void testSettingsMapWithOneSerializableSettingsChild() {
        final GenericSerializableSettings settings = createTestSettingsWithValues();
        
        SettingsMap settingsToSave = new SettingsMapImpl("aaa", settings);

        final SettingsMap deserializedSettings = serializeAndDeserialize(settingsToSave);
        assertEquals(settingsToSave, deserializedSettings);
    }
    
    protected abstract SettingsMap deserialize(SOT serializedObject, SettingsMap settingsMap) throws Exception;
    
    protected abstract SOT serialize(SettingsMap settings) throws Exception;
    
    private SettingsMap serializeAndDeserialize(SettingsMap settingsMap) {
        try {
            SOT serialized = serialize(settingsMap);
            SettingsMap reconstructedSettingsMap = reconstructSettingsMap(settingsMap);
            return deserialize(serialized, reconstructedSettingsMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SettingsMap reconstructSettingsMap(SettingsMap settingsMap) {
        Map<Serializable, Settings> innerMap = new HashMap<>();
        for(Map.Entry<Serializable, Settings> entry : settingsMap.getSettingsByKey().entrySet()) {
            Settings innerSettings = entry.getValue();
            Settings copiedSettings;
            if(innerSettings instanceof SettingsMap) {
                copiedSettings = reconstructSettingsMap((SettingsMap)innerSettings);
            } else {
                try {
                    copiedSettings = innerSettings.getClass().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            innerMap.put(entry.getKey(), copiedSettings);
        }
        return new SettingsMapImpl(innerMap);
    }
}
