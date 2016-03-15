package com.sap.sse.common.settings.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sap.sse.common.settings.HasValueSetting;
import com.sap.sse.common.settings.Setting;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.SettingsListSetting;
import com.sap.sse.common.settings.ValueConverter;
import com.sap.sse.common.settings.ValueListSetting;
import com.sap.sse.common.settings.ValueSetting;

public class SettingsToStringMapSerializer {
    private static final String PATH_SEPARATOR = ".";

    public Map<String, Iterable<String>> serialize(Settings settings) {
        final Map<String, Iterable<String>> result = new HashMap<>();
        serialize("", settings, result);
        return result;
    }

    private void serialize(String prefix, Settings settings, Map<String, Iterable<String>> serialized) {
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
        } else if (setting instanceof ValueListSetting) {
            serialized.put(key, serializeValueListSetting(key, (ValueListSetting<?>) setting));
        } else {
            String prefix = key + PATH_SEPARATOR;
            if (setting instanceof Settings) {
                serialize(prefix, (Settings) setting, serialized);
            } else if (setting instanceof SettingsListSetting) {
                serializeSettingsListSetting(prefix, (SettingsListSetting<?>) setting, serialized);
            } else {
                throw new IllegalStateException("Unknown Setting type");
            }
        }
    }

    private <T extends Settings> void serializeSettingsListSetting(String prefix, SettingsListSetting<T> setting,
            Map<String, Iterable<String>> serialized) {
        int index = 0;
        for (T childSettings : setting.getValues()) {
            String nestedPrefix = prefix + index + PATH_SEPARATOR;
            serialize(nestedPrefix, childSettings, serialized);
            index++;
        }
    }

    private <T> Iterable<String> serializeValueSetting(String key, ValueSetting<T> valueSetting) {
        return Collections.singleton(valueSetting.getValueConverter().toStringValue(valueSetting.getValue()));
    }

    private <T> Iterable<String> serializeValueListSetting(String key, ValueListSetting<T> valueSetting) {
        List<String> result = new ArrayList<>();
        ValueConverter<T> valueConverter = valueSetting.getValueConverter();
        for (T value : valueSetting.getValues()) {
            result.add(valueConverter.toStringValue(value));
        }
        return result;
    }

    public <T extends Settings> T deserialize(T settings, Map<String, Iterable<String>> values) {
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
            if (settingValues != null) {
                if (setting instanceof ValueSetting) {
                    deserializeValueSetting((ValueSetting<?>) setting, settingValues);
                } else if (setting instanceof ValueListSetting) {
                    deserializeValueListSetting((ValueListSetting<?>) setting, settingValues);
                } else {
                    throw new IllegalStateException("Unknown HasValueSetting type");
                }
            }
        } else {
            final Map<String, Iterable<String>> innerValues = mappedInnerValues.get(key);
            if (innerValues != null) {
                if (setting instanceof Settings) {
                    deserialize((Settings) setting, innerValues);
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
            valueSetting.setValue(valueSetting.getValueConverter().fromStringValue(iterator.next()));
        }
    }

    private <T> void deserializeValueListSetting(ValueListSetting<T> valueSetting, Iterable<String> values) {
        List<T> deserializedValues = new ArrayList<>();
        for (String stringValue : values) {
            deserializedValues.add(valueSetting.getValueConverter().fromStringValue(stringValue));
        }
        valueSetting.setValues(deserializedValues);
    }

    private <T extends Settings> void deserializeSettingsListSetting(SettingsListSetting<T> valueSetting,
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
            int separatorIndex = key.indexOf(PATH_SEPARATOR);
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
                o2int = Integer.parseInt(o1);
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
