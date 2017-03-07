package com.sap.sse.common.settings.generic.support;

import java.util.Map;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public final class SettingsUtil {
    
    private SettingsUtil() {
    }
    
    private interface DefaultValuesExtractor {
        <V> V getDefaultValue(ValueSetting<V> setting);
        <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting);
    }

    public static <T extends GenericSerializableSettings> T copyDefaults(T settingsWithDefaults, T settingsToSetDefaults) {
        return copyDefaultsInternal(settingsWithDefaults, settingsToSetDefaults, new DefaultValuesExtractor() {
            @Override
            public <V> V getDefaultValue(ValueSetting<V> setting) {
                return setting.getDefaultValue();
            }

            @Override
            public <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting) {
                return setting.getDefaultValues();
            }
        });
    }
    
    public static <T extends GenericSerializableSettings> T copyDefaultsFromValues(T settingsToUseAsDefaults, T settingsToSetDefaults) {
        return copyDefaultsInternal(settingsToUseAsDefaults, settingsToSetDefaults, new DefaultValuesExtractor() {
            @Override
            public <V> V getDefaultValue(ValueSetting<V> setting) {
                return setting.getValue();
            }
            
            @Override
            public <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting) {
                return setting.getValues();
            }
        });
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T extends GenericSerializableSettings> T copyDefaultsInternal(T settingsWithDefaults, T settingsToSetDefaults,
            DefaultValuesExtractor defaultValuesExtractor) {
        Map<String, Setting> childSettingsWithDefaults = settingsWithDefaults.getChildSettings();
        Map<String, Setting> childSettingsToSetDefaults = settingsToSetDefaults.getChildSettings();
        for (Map.Entry<String, Setting> entry : childSettingsToSetDefaults.entrySet()) {
            String settingKey = entry.getKey();
            Setting settingToSetDefault = entry.getValue();
            if (settingToSetDefault instanceof ValueSetting) {
                ValueSetting valueSettingToSetDefault = (ValueSetting) settingToSetDefault;
                ValueSetting valueSettingWithDefault = (ValueSetting) childSettingsWithDefaults.get(settingKey);
                valueSettingToSetDefault
                        .setDefaultValue(defaultValuesExtractor.getDefaultValue(valueSettingWithDefault));
            } else if (settingToSetDefault instanceof ValueCollectionSetting) {
                ValueCollectionSetting valueCollectionSettingToSetDefault = (ValueCollectionSetting) settingToSetDefault;
                ValueCollectionSetting valueCollectionSettingWithDefault = (ValueCollectionSetting) childSettingsWithDefaults
                        .get(settingKey);
                valueCollectionSettingToSetDefault
                        .setDefaultValues(defaultValuesExtractor.getDefaultValue(valueCollectionSettingWithDefault));
            } else if (settingToSetDefault instanceof GenericSerializableSettings) {
                GenericSerializableSettings settingsToSetDefault = (GenericSerializableSettings) settingToSetDefault;
                GenericSerializableSettings settingsWithDefault = (GenericSerializableSettings) childSettingsWithDefaults
                        .get(settingKey);
                copyDefaultsInternal(settingsWithDefault, settingsToSetDefault, defaultValuesExtractor);
            } else {
                // SettingsListSetting will be ignored
            }
        }
        return settingsToSetDefaults;
    }
}
