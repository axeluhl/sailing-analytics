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

    private interface ValuesExtractor {
        <V> V getDefaultValue(ValueSetting<V> setting);

        <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting);
    }
    
    private interface ValuesSetter {
        <V> void setDefaultValue(ValueSetting<V> setting, V value);
        
        <V> void setDefaultValue(ValueCollectionSetting<V> setting, Iterable<V> values);
    }
    
    private static final ValuesExtractor defaultValuesExtractor = new ValuesExtractor() {
        @Override
        public <V> V getDefaultValue(ValueSetting<V> setting) {
            return setting.getDefaultValue();
        }
        @Override
        public <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting) {
            return setting.getDefaultValues();
        }
    };
    private static final ValuesSetter defaultValuesSetter = new ValuesSetter() {
        @Override
        public <V> void setDefaultValue(ValueSetting<V> setting, V value) {
            setting.setDefaultValue(value);
        }
        @Override
        public <V> void setDefaultValue(ValueCollectionSetting<V> setting, Iterable<V> values) {
            setting.setDefaultValues(values);
        }
    };
    private static final ValuesExtractor valuesExtractor = new ValuesExtractor() {
        @Override
        public <V> V getDefaultValue(ValueSetting<V> setting) {
            return setting.getValue();
        }

        @Override
        public <V> Iterable<V> getDefaultValue(ValueCollectionSetting<V> setting) {
            return setting.getValues();
        }
    };
    private static final ValuesSetter valuesSetter = new ValuesSetter() {
        @Override
        public <V> void setDefaultValue(ValueSetting<V> setting, V value) {
            setting.setValue(value);
        }
        @Override
        public <V> void setDefaultValue(ValueCollectionSetting<V> setting, Iterable<V> values) {
            setting.setValues(values);
        }
    };

    /**
     * Copies the defaults of the given settingsWithDefaults to the defaults of the given settingsToSetDefaults. Be
     * aware that this works for nested {@link SettingsMap}, {@link GenericSerializableSettings}, {@link ValueSetting}
     * or {@link ValueCollectionSetting} instances, but not {@link SettingsListSetting}.
     * 
     * @param <T>
     *            the type of settings, must be either a {@link SettingsMap} or {@link GenericSerializableSettings}
     */
    public static <T extends Settings> T copyDefaults(T settingsWithDefaults, T settingsToSetDefaults) {
        return copyDefaultsInternal(settingsWithDefaults, settingsToSetDefaults, defaultValuesExtractor, defaultValuesSetter);
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
        return copyDefaultsInternal(settingsToUseAsDefaults, settingsToSetDefaults, valuesExtractor, defaultValuesSetter);
    }
    
    /**
     * Copies the values of the given settingsWithValues to the values of the given settingsToSetValues. Be aware
     * that this works for nested {@link GenericSerializableSettings}, {@link ValueSetting} or
     * {@link ValueCollectionSetting}, but not {@link SettingsListSetting}.
     * 
     * @param <T>
     *            the type of settings, must be either a {@link SettingsMap} or {@link GenericSerializableSettings}
     */
    public static <T extends Settings> T copyValues(T settingsWithValues, T settingsToSetValues) {
        return copyDefaultsInternal(settingsWithValues, settingsToSetValues, valuesExtractor, valuesSetter);
    }
    
    /**
     * Copies the values of the given settingsWithDefaults to the values of the given settingsToSetValuesAndDefaults and
     * copies the values of the given settingsWithValues to the values of the given settingsToSetValuesAndDefaults. Be
     * aware that this works for nested {@link GenericSerializableSettings}, {@link ValueSetting} or
     * {@link ValueCollectionSetting}, but not {@link SettingsListSetting}.
     * 
     * @param <T>
     *            the type of settings, must be either a {@link SettingsMap} or {@link GenericSerializableSettings}
     */
    public static <T extends Settings> T copyValuesAndDefaults(T settingsWithValues, T settingsWithDefaults, T settingsToSetValuesAndDefaults) {
        copyDefaultsInternal(settingsWithDefaults, settingsToSetValuesAndDefaults, defaultValuesExtractor, defaultValuesSetter);
        copyDefaultsInternal(settingsWithValues, settingsToSetValuesAndDefaults, valuesExtractor, valuesSetter);
        return settingsToSetValuesAndDefaults;
    }

    private static <T extends Settings> T copyDefaultsInternal(T settingsWithDefaults, T settingsToSetDefaults,
            ValuesExtractor defaultValuesExtractor, ValuesSetter valuesSetter) {
        if (settingsToSetDefaults instanceof GenericSerializableSettings) {
            copyDefaultsForGenericSerializableSettings((GenericSerializableSettings) settingsWithDefaults,
                    (GenericSerializableSettings) settingsToSetDefaults, defaultValuesExtractor, valuesSetter);
        } else if (settingsToSetDefaults instanceof SettingsMap) {
            SettingsMap settingsMapWithDefaults = (SettingsMap) settingsWithDefaults;
            SettingsMap settingsMapToSetDefaults = (SettingsMap) settingsToSetDefaults;
            for (Map.Entry<String, Settings> entry : settingsMapToSetDefaults.getSettingsPerComponentId().entrySet()) {
                final Settings childSettingsToSetDefaults = entry.getValue();
                final Settings childSettingsWithDefaults = settingsMapWithDefaults.getSettingsPerComponentId()
                        .get(entry.getKey());
                copyDefaultsInternal(childSettingsWithDefaults, childSettingsToSetDefaults, defaultValuesExtractor, valuesSetter);
            }
        } else {
            // any hand-coded non-serializable settings instance is ignored
        }
        return settingsToSetDefaults;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T extends GenericSerializableSettings> T copyDefaultsForGenericSerializableSettings(
            T settingsWithDefaults, T settingsToSetDefaults, ValuesExtractor defaultValuesExtractor, ValuesSetter valuesSetter) {
        Map<String, Setting> childSettingsWithDefaults = settingsWithDefaults.getChildSettings();
        Map<String, Setting> childSettingsToSetDefaults = settingsToSetDefaults.getChildSettings();
        for (Map.Entry<String, Setting> entry : childSettingsToSetDefaults.entrySet()) {
            String settingKey = entry.getKey();
            Setting settingToSetDefault = entry.getValue();
            if (settingToSetDefault instanceof ValueSetting) {
                ValueSetting valueSettingToSetDefault = (ValueSetting) settingToSetDefault;
                ValueSetting valueSettingWithDefault = (ValueSetting) childSettingsWithDefaults.get(settingKey);
                valuesSetter.setDefaultValue(valueSettingToSetDefault, defaultValuesExtractor.getDefaultValue(valueSettingWithDefault));
            } else if (settingToSetDefault instanceof ValueCollectionSetting) {
                ValueCollectionSetting valueCollectionSettingToSetDefault = (ValueCollectionSetting) settingToSetDefault;
                ValueCollectionSetting valueCollectionSettingWithDefault = (ValueCollectionSetting) childSettingsWithDefaults
                        .get(settingKey);
                valuesSetter.setDefaultValue(valueCollectionSettingToSetDefault, defaultValuesExtractor.getDefaultValue(valueCollectionSettingWithDefault));
            } else if (settingToSetDefault instanceof GenericSerializableSettings) {
                GenericSerializableSettings settingsToSetDefault = (GenericSerializableSettings) settingToSetDefault;
                GenericSerializableSettings settingsWithDefault = (GenericSerializableSettings) childSettingsWithDefaults
                        .get(settingKey);
                copyDefaultsForGenericSerializableSettings(settingsWithDefault, settingsToSetDefault,
                        defaultValuesExtractor, valuesSetter);
            } else {
                // SettingsListSetting will be ignored
            }
        }
        return settingsToSetDefaults;
    }
}
