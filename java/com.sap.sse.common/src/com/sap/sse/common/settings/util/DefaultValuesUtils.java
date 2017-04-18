package com.sap.sse.common.settings.util;

import java.util.Map;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public class DefaultValuesUtils {

    public static void setDefaults(GenericSerializableSettings defaultSettings, GenericSerializableSettings settingsToPatch) {
        for (Map.Entry<String, Setting> entry : defaultSettings.getChildSettings().entrySet()) {
            Setting defaultSetting = entry.getValue();
            Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
            if (defaultSetting instanceof ValueSetting) {
                setDefaults((ValueSetting<?>) defaultSetting, (ValueSetting<?>) settingToPatch);
            } else if (defaultSetting instanceof ValueCollectionSetting) {
                setDefaults((ValueCollectionSetting<?>) defaultSetting, (ValueCollectionSetting<?>) settingToPatch);
            } else if (defaultSetting instanceof GenericSerializableSettings) {
                setDefaults((GenericSerializableSettings) defaultSetting, (GenericSerializableSettings) settingToPatch);
            } else {
                throw new IllegalStateException("Unknown Setting type");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static<T> void setDefaults(ValueCollectionSetting<?> defaultSetting, ValueCollectionSetting<T> settingToPatch) {
        Iterable<T> originalValues = settingToPatch.getValues();
        settingToPatch.setDefaultValues((Iterable<T>) defaultSetting.getValues());
        settingToPatch.setValues(originalValues);
    }

    @SuppressWarnings("unchecked")
    private static<T> void setDefaults(ValueSetting<?> defaultSetting, ValueSetting<T> settingToPatch) {
        T originalValue = settingToPatch.getValue();
        settingToPatch.setDefaultValue((T) defaultSetting.getValue());
        settingToPatch.setValue(originalValue);
    }

}
