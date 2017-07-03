package com.sap.sse.common.settings.util;

import java.util.Map;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public class SettingsDefaultValuesUtils {
    
    public static<T extends GenericSerializableSettings> T getDefaultSettings(T newInstance, T settingsWithContainedDefaults) {
        keepDefaults(settingsWithContainedDefaults, newInstance);
        newInstance.resetToDefault();
        return newInstance;
    }
    
    public static void keepDefaults(GenericSerializableSettings previousSettings, GenericSerializableSettings newSettings) {
        for (Map.Entry<String, Setting> entry : previousSettings.getChildSettings().entrySet()) {
            Setting previousSetting = entry.getValue();
            Setting newSetting = newSettings.getChildSettings().get(entry.getKey());
            if (previousSetting instanceof ValueSetting) {
                keepDefaults((ValueSetting<?>) previousSetting, (ValueSetting<?>) newSetting);
            } else if (newSetting instanceof ValueCollectionSetting) {
                keepDefaults((ValueCollectionSetting<?>) previousSetting, (ValueCollectionSetting<?>) newSetting);
            } else if (newSetting instanceof GenericSerializableSettings) {
                keepDefaults((GenericSerializableSettings) previousSetting, (GenericSerializableSettings) newSetting);
            } else {
                throw new IllegalStateException("Unknown Setting type");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static<T> void keepDefaults(ValueCollectionSetting<?> previousSettings, ValueCollectionSetting<T> newSettings) {
        Iterable<T> originalValues = newSettings.getValues();
        newSettings.setDefaultValues((Iterable<T>) previousSettings.getDefaultValues());
        newSettings.setValues(originalValues);
    }

    @SuppressWarnings("unchecked")
    private static<T> void keepDefaults(ValueSetting<?> previousSettings, ValueSetting<T> newSettings) {
        T originalValue = newSettings.getValue();
        newSettings.setDefaultValue((T) previousSettings.getDefaultValue());
        newSettings.setValue(originalValue);
    }

}
