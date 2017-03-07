package com.sap.sse.common.settings.generic.support;

import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.SettingsListSetting;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public final class SettingsUtil {

    private SettingsUtil() {
    }

    private interface DefaultValuesExtractor {
        <V> V getDefaultValue(ValueSetting<V> setting);

        <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting);
    }

    /**
     * Copies the defaults of the given settingsWithDefaults to the defaults of the given settingsToSetDefaults. Be
     * aware that this works for nested {@link SettingsMap}, {@link GenericSerializableSettings}, {@link ValueSetting}
     * or {@link ValueCollectionSetting} instances, but not {@link SettingsListSetting}.
     * 
     * @param <T>
     *            the type of settings, must be either a {@link SettingsMap} or {@link GenericSerializableSettings}
     */
    public static <T extends Settings> T copyDefaults(T settingsWithDefaults, T settingsToSetDefaults) {
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

    /**
     * Copies the values of the given settingsWithDefaults to the defaults of the given settingsToSetDefaults. Be aware
     * that this works for nested {@link GenericSerializableSettings}, {@link ValueSetting} or
     * {@link ValueCollectionSetting}, but not {@link SettingsListSetting}.
     * 
     * @param <T>
     *            the type of settings, must be either a {@link SettingsMap} or {@link GenericSerializableSettings}
     */
    public static <T extends Settings> T copyDefaultsFromValues(T settingsToUseAsDefaults, T settingsToSetDefaults) {
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

    private static <T extends Settings> T copyDefaultsInternal(T settingsWithDefaults, T settingsToSetDefaults,
            DefaultValuesExtractor defaultValuesExtractor) {
        if (settingsToSetDefaults instanceof GenericSerializableSettings) {
            copyDefaultsForGenericSerializableSettings((GenericSerializableSettings) settingsWithDefaults,
                    (GenericSerializableSettings) settingsToSetDefaults, defaultValuesExtractor);
        } else if (settingsToSetDefaults instanceof SettingsMap) {
            SettingsMap settingsMapWithDefaults = (SettingsMap) settingsWithDefaults;
            SettingsMap settingsMapToSetDefaults = (SettingsMap) settingsToSetDefaults;
            for (Map.Entry<String, Settings> entry : settingsMapToSetDefaults.getSettingsPerComponentId().entrySet()) {
                final Settings childSettingsToSetDefaults = entry.getValue();
                final Settings childSettingsWithDefaults = settingsMapWithDefaults.getSettingsPerComponentId()
                        .get(entry.getKey());
                copyDefaultsInternal(childSettingsWithDefaults, childSettingsToSetDefaults, defaultValuesExtractor);
            }
        } else {
            // any hand-coded non-serializable settings instance is ignored
        }
        return settingsToSetDefaults;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T extends GenericSerializableSettings> T copyDefaultsForGenericSerializableSettings(
            T settingsWithDefaults, T settingsToSetDefaults, DefaultValuesExtractor defaultValuesExtractor) {
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
                copyDefaultsForGenericSerializableSettings(settingsWithDefault, settingsToSetDefault,
                        defaultValuesExtractor);
            } else {
                // SettingsListSetting will be ignored
            }
        }
        return settingsToSetDefaults;
    }
}
