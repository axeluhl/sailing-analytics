package com.sap.sse.common.settings.util;

import java.util.List;
import java.util.Map;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public class SettingsMergeUtils {

    public static void mergeSettings(GenericSerializableSettings additiveSettings, GenericSerializableSettings settingsToPatch) {
        for (Map.Entry<String, Setting> entry : additiveSettings.getChildSettings().entrySet()) {
            Setting setting = entry.getValue();
            Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
            if (!setting.isDefaultValue()) {
                if (setting instanceof ValueSetting) {
                    mergeSettings((ValueSetting<?>) setting, (ValueSetting<?>) settingToPatch);
                } else if (setting instanceof ValueCollectionSetting) {
                    mergeSettings((ValueCollectionSetting<?>) setting, (ValueCollectionSetting<?>) settingToPatch);
                } else if (setting instanceof GenericSerializableSettings) {
                    mergeSettings((GenericSerializableSettings) setting, (GenericSerializableSettings) settingToPatch);
                } else {
                    throw new IllegalStateException("Unknown Setting type");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static<T> void mergeSettings(ValueCollectionSetting<?> setting, ValueCollectionSetting<T> settingToPatch) {
        
        List<T> newValues = Util.createList(settingToPatch.getValues());
        
        //remove values which are not contained in setting, but are contained in the default values of setting
        for (Object value : setting.getDefaultValues()) {
            if(!Util.contains(setting.getValues(), value)) {
                newValues.remove((T) value);
            }
        }
        
        for (Object value : setting.getValues()) {
            if(!Util.contains(settingToPatch.getValues(), value)) {
                newValues.add((T) value);
            }
        }
        
        settingToPatch.setValues(newValues);
        
    }

    @SuppressWarnings("unchecked")
    private static<T> void mergeSettings(ValueSetting<?> setting, ValueSetting<T> settingToPatch) {
        settingToPatch.setValue((T) setting.getValue());
    }
}
