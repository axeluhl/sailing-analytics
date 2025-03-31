package com.sap.sse.common.settings.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.TreeMap;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.HasValueSetting;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.SettingsListSetting;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueSetting;

/**
 * {@link GenericSerializableSettings} serializer that constructs flattened String keys with associated String values from the hierarchical
 * {@link GenericSerializableSettings} structures. This is necessary for cases where hierarchical structures aren't mappable (e.g. URL
 * serialization).
 *
 */
public class SettingsToStringMapSerializer {
    
    private static final Logger LOG = Logger.getLogger(SettingsToStringMapSerializer.class.getName());
    
    private static final String ADDED_SUFFIX = GenericSerializableSettings.PATH_SEPARATOR + GenericSerializableSettings.ADDED_TOKEN;
    private static final String REMOVED_SUFFIX = GenericSerializableSettings.PATH_SEPARATOR + GenericSerializableSettings.REMOVED_TOKEN;
    
    public final Map<String, Iterable<String>> serialize(SettingsMap settingsMap) {
        final Map<String, Iterable<String>> result = new HashMap<>();
        serialize("", settingsMap, result);
        return result;
    }
    
    private void serialize(String prefix, SettingsMap settingsMap, Map<String, Iterable<String>> serialized) {
        for (Map.Entry<String, Settings> entry : settingsMap.getSettingsPerComponentId().entrySet()) {
            String key = entry.getKey();
            String childPrefix = key == null ? prefix : prefix + key + GenericSerializableSettings.PATH_SEPARATOR;
            Settings settings = entry.getValue();
            if(settings instanceof SettingsMap) {
                serialize(childPrefix, (SettingsMap) settings, serialized);
            } else if (settings instanceof GenericSerializableSettings) {
                serialize(childPrefix, (GenericSerializableSettings) settings, serialized);
            }
        }
    }

    public final Map<String, Iterable<String>> serialize(GenericSerializableSettings settings) {
        final Map<String, Iterable<String>> result = new HashMap<>();
        serialize("", settings, result);
        return result;
    }

    private void serialize(String prefix, GenericSerializableSettings settings, Map<String, Iterable<String>> serialized) {
        for (Map.Entry<String, Setting> entry : settings.getChildSettings().entrySet()) {
            final Setting setting = entry.getValue();
            if (!setting.isDefaultValue()) {
                String key = prefix + entry.getKey();
                serialize(key, setting, serialized);
            }
        }
    }

    private void serialize(String key, Setting setting, Map<String, Iterable<String>> serialized) {
        if (setting instanceof ValueSetting) {
            serialized.put(key, serializeValueSetting(key, (ValueSetting<?>) setting));
        } else if (setting instanceof ValueCollectionSetting) {
            serializeValueListSetting(key, serialized, (ValueCollectionSetting<?>) setting);
        } else {
            String prefix = key + GenericSerializableSettings.PATH_SEPARATOR;
            if (setting instanceof GenericSerializableSettings) {
                serialize(prefix, (GenericSerializableSettings) setting, serialized);
            } else if (setting instanceof SettingsListSetting) {
                serializeSettingsListSetting(prefix, (SettingsListSetting<?>) setting, serialized);
            } else {
                throw new IllegalStateException("Unknown Setting type");
            }
        }
    }

    private <T extends GenericSerializableSettings> void serializeSettingsListSetting(String prefix, SettingsListSetting<T> setting,
            Map<String, Iterable<String>> serialized) {
        int index = 0;
        for (T childSettings : setting.getValues()) {
            String nestedPrefix = prefix + index + GenericSerializableSettings.PATH_SEPARATOR;
            serialize(nestedPrefix, childSettings, serialized);
            index++;
        }
    }

    private <T> Iterable<String> serializeValueSetting(String key, ValueSetting<T> valueSetting) {
        return Collections.singleton(valueSetting.getValueConverter().toStringValue(valueSetting.getValue()));
    }

    private <T> Iterable<String> serializeValueListSetting(String key, Map<String, Iterable<String>> serialized, ValueCollectionSetting<T> valueSetting) {
        List<String> result = new ArrayList<>();
        ValueConverter<T> valueConverter = valueSetting.getValueConverter();
        serialized.put(key + ADDED_SUFFIX, serializeMultipleValuesForSetting(valueConverter, valueSetting.getAddedValues()));
        serialized.put(key + REMOVED_SUFFIX, serializeMultipleValuesForSetting(valueConverter, valueSetting.getRemovedValues()));
        return result;
    }
    
    private <T> Iterable<String> serializeMultipleValuesForSetting(ValueConverter<T> valueConverter, Iterable<T> values) {
        List<String> result = new ArrayList<>();
        for (T value : values) {
            result.add(valueConverter.toStringValue(value));
        }
        return result;
    }
    
    public final <T extends SettingsMap> T deserializeSettingsMap(T settingsMap, Map<String, Iterable<String>> values) {
        final Map<String, Map<String, Iterable<String>>> mappedInnerValues = mapNested(values);
        final Set<Entry<String, Settings>> childSettings = settingsMap.getSettingsPerComponentId().entrySet();
        for (Map.Entry<String, Settings> entry : childSettings) {
            final String key = entry.getKey();
            final Settings settings = entry.getValue();
            final Map<String, Iterable<String>> innerValues = key == null ? values : mappedInnerValues.get(key.toString());
            if(innerValues != null) {
                if(settings instanceof SettingsMap) {
                    deserializeSettingsMap((SettingsMap) settings, innerValues);
                } else if(settings instanceof GenericSerializableSettings) {
                    deserialize((GenericSerializableSettings)settings, innerValues);
                }
            }
        }
        return settingsMap;
    }

    public final <T extends GenericSerializableSettings> T deserialize(T settings, Map<String, Iterable<String>> values) {
        Map<String, Map<String, Iterable<String>>> mappedInnerValues = mapNested(values);
        for (Map.Entry<String, Setting> entry : settings.getChildSettings().entrySet()) {
            final String key = entry.getKey();
            final Setting setting = entry.getValue();
            deserialize(key, setting, values, mappedInnerValues);
        }
        return settings;
    }

    private void deserialize(String key, Setting setting, Map<String, Iterable<String>> values,
            Map<String, Map<String, Iterable<String>>> mappedInnerValues) {
        if (setting instanceof HasValueSetting) {
            final Iterable<String> settingValues = values.get(key);
            if (setting instanceof ValueSetting) {
                if (settingValues != null) {
                    deserializeValueSetting((ValueSetting<?>) setting, settingValues);
                }
            } else if (setting instanceof ValueCollectionSetting) {
                final Iterable<String> addedValues = values.get(key + ADDED_SUFFIX);
                final Iterable<String> removedValues = values.get(key + REMOVED_SUFFIX);
                deserializeValueListSetting((ValueCollectionSetting<?>) setting, settingValues, addedValues, removedValues);
            } else {
                throw new IllegalStateException("Unknown HasValueSetting type");
            }
        } else {
            final Map<String, Iterable<String>> innerValues = mappedInnerValues.get(key);
            if (innerValues != null) {
                if (setting instanceof GenericSerializableSettings) {
                    deserialize((GenericSerializableSettings) setting, innerValues);
                } else if (setting instanceof SettingsListSetting) {
                    deserializeSettingsListSetting((SettingsListSetting<?>) setting, innerValues);
                } else {
                    throw new IllegalStateException("Unknown Setting type");
                }
            }
        }
    }

    private <T> void deserializeValueSetting(ValueSetting<T> valueSetting, Iterable<String> values) {
        Iterator<String> iterator = values.iterator();
        if (iterator.hasNext()) {
            // Additional values are currently silently skipped.
            // This should only happen if somebody manipulates the data or a List setting is changed to a single setting
            // value.
            final ValueConverter<T> valueConverter = valueSetting.getValueConverter();
            final String stringValue = iterator.next();
            try {
                valueSetting.setValue(valueConverter.fromStringValue(stringValue));
            } catch(Exception e) {
                LOG.log(Level.WARNING, "Error while converting String value \"" + stringValue + "\" using converter \""
                        + valueConverter.getClass().getSimpleName() + "\"", e);
            }
        }
    }

    private <T> void deserializeValueListSetting(ValueCollectionSetting<T> valueSetting, Iterable<String> values, Iterable<String> addedValues, Iterable<String> removedValues) {
        final ValueConverter<T> valueConverter = valueSetting.getValueConverter();
        if(values != null) {
            // fallback logic to correctly deserialize values serialized before diffing was introduced
            valueSetting.setValues(deserializeMultipleValuesForSetting(valueConverter, values));
        } else {
            final Iterable<T> addedSettingValues = deserializeMultipleValuesForSetting(valueConverter, addedValues);
            final Iterable<T> removedSettingValues = deserializeMultipleValuesForSetting(valueConverter, removedValues);
            valueSetting.setDiff(removedSettingValues, addedSettingValues);
        }
    }
    
    private <T> Iterable<T> deserializeMultipleValuesForSetting(ValueConverter<T> valueConverter, Iterable<String> values) {
        List<T> deserializedValues = new ArrayList<>();
        if(values != null) {
            for (String stringValue : values) {
                try {
                    deserializedValues.add(valueConverter.fromStringValue(stringValue));
                } catch(Exception e) {
                    LOG.log(Level.WARNING, "Error while converting String value \"" + stringValue + "\" using converter \""
                            + valueConverter.getClass().getSimpleName() + "\"", e);
                }
            }
        }
        return deserializedValues;
    }

    private <T extends GenericSerializableSettings> void deserializeSettingsListSetting(SettingsListSetting<T> valueSetting,
            Map<String, Iterable<String>> values) {
        Map<String, Map<String, Iterable<String>>> mappedNestedListValues = mapNestedWithIndexOrder(values);
        List<T> deserializedValues = new ArrayList<>();
        for (Map.Entry<String, Map<String, Iterable<String>>> entry : mappedNestedListValues.entrySet()) {
            deserializedValues.add(deserialize(valueSetting.getSettingsFactory().newInstance(), entry.getValue()));
        }
        valueSetting.setValues(deserializedValues);
    }

    private Map<String, Map<String, Iterable<String>>> mapNestedWithIndexOrder(Map<String, Iterable<String>> values) {
        Map<String, Map<String, Iterable<String>>> result = new TreeMap<>(new IndexComparator());
        result.putAll(mapNested(values));
        return result;
    }

    private Map<String, Map<String, Iterable<String>>> mapNested(Map<String, Iterable<String>> values) {
        Map<String, Map<String, Iterable<String>>> result = new HashMap<>();
        for (Map.Entry<String, Iterable<String>> entry : values.entrySet()) {
            String key = entry.getKey();
            int separatorIndex = key.indexOf(GenericSerializableSettings.PATH_SEPARATOR);
            if (separatorIndex > 0) {
                String group = key.substring(0, separatorIndex);
                String nestedKey = key.substring(separatorIndex + 1);

                Map<String, Iterable<String>> inner = result.get(group);
                if (inner == null) {
                    inner = new HashMap<>();
                    result.put(group, inner);
                }
                inner.put(nestedKey, entry.getValue());
            }
        }
        return result;
    }

    private static class IndexComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            boolean o1error = false;
            boolean o2error = false;
            int o1int = 0;
            int o2int = 0;
            try {
                o1int = Integer.parseInt(o1);
            } catch (NumberFormatException e) {
                o1error = true;
            }
            try {
                o2int = Integer.parseInt(o2);
            } catch (NumberFormatException e) {
                o1error = true;
            }
            if (o1error && o2error) {
                return o1.compareTo(o2);
            }
            if (!o1error && !o2error) {
                return Integer.compare(o1int, o2int);
            }
            return o1error ? 1 : -1;
        }
    }
}
