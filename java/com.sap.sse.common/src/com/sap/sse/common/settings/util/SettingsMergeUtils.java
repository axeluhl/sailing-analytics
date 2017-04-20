package com.sap.sse.common.settings.util;

import java.util.List;
import java.util.Map;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;

public class SettingsMergeUtils {
    
    public static void setValues(GenericSerializableSettings settingsWithValues, GenericSerializableSettings settingsToPatch) {
        for (Map.Entry<String, Setting> entry : settingsWithValues.getChildSettings().entrySet()) {
            Setting settingWithValue = entry.getValue();
            Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
            if (settingWithValue instanceof ValueSetting) {
                setValues((ValueSetting<?>) settingWithValue, (ValueSetting<?>) settingToPatch);
            } else if (settingWithValue instanceof ValueCollectionSetting) {
                setValues((ValueCollectionSetting<?>) settingWithValue, (ValueCollectionSetting<?>) settingToPatch);
            } else if (settingWithValue instanceof GenericSerializableSettings) {
                setValues((GenericSerializableSettings) settingWithValue, (GenericSerializableSettings) settingToPatch);
            } else {
                throw new IllegalStateException("Unknown Setting type");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static<T> void setValues(ValueCollectionSetting<?> settingWithValue, ValueCollectionSetting<T> settingToPatch) {
        settingToPatch.setValues((Iterable<T>) settingWithValue.getValues());
    }

    @SuppressWarnings("unchecked")
    private static<T> void setValues(ValueSetting<?> settingWithValue, ValueSetting<T> settingToPatch) {
        settingToPatch.setValue((T) settingWithValue.getValue());
    }

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
    
    public static void mergeDefaults(GenericSerializableSettings defaultSettings, GenericSerializableSettings settingsToPatch) {
        for (Map.Entry<String, Setting> entry : defaultSettings.getChildSettings().entrySet()) {
            Setting defaultSetting = entry.getValue();
            Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
            if (!defaultSetting.isDefaultValue()) {
                if (defaultSetting instanceof ValueSetting) {
                    mergeDefaults((ValueSetting<?>) defaultSetting, (ValueSetting<?>) settingToPatch);
                } else if (defaultSetting instanceof ValueCollectionSetting) {
                    mergeDefaults((ValueCollectionSetting<?>) defaultSetting, (ValueCollectionSetting<?>) settingToPatch);
                } else if (defaultSetting instanceof GenericSerializableSettings) {
                    mergeDefaults((GenericSerializableSettings) defaultSetting, (GenericSerializableSettings) settingToPatch);
                } else {
                    throw new IllegalStateException("Unknown Setting type");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static<T> void mergeDefaults(ValueCollectionSetting<?> defaultSetting, ValueCollectionSetting<T> settingToPatch) {
        
        List<T> newDefaultValues = Util.createList(settingToPatch.getDefaultValues());
        
        //remove values which are not contained in setting, but are contained in the default values of setting
        for (Object value : defaultSetting.getDefaultValues()) {
            if(!Util.contains(defaultSetting.getValues(), value)) {
                newDefaultValues.remove((T) value);
            }
        }
        
        for (Object value : defaultSetting.getValues()) {
            if(!Util.contains(settingToPatch.getDefaultValues(), value)) {
                newDefaultValues.add((T) value);
            }
        }
        
        Iterable<T> originalValues = settingToPatch.getValues();
        settingToPatch.setDefaultValues(newDefaultValues);
        settingToPatch.setValues(originalValues);
    }

    @SuppressWarnings("unchecked")
    private static<T> void mergeDefaults(ValueSetting<?> defaultSetting, ValueSetting<T> settingToPatch) {
        T originalValue = settingToPatch.getValue();
        settingToPatch.setDefaultValue((T) defaultSetting.getValue());
        settingToPatch.setValue(originalValue);
    }
}
