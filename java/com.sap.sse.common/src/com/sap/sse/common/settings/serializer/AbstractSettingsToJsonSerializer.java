package com.sap.sse.common.settings.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.CollectionSetting;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.SettingsListSetting;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.generic.ValueSetting;

/**
 * Base class for {@link Settings} serializers that produce JSON objects using some generic object model. This base
 * class ensures that the produced structure is equivalent for different JSON APIs so the setrialized JSON Strings
 * should be compatible and sharable between all those implementations.
 *
 * @param <OBJECT>
 *            JSON object type
 * @param <ARRAY>
 *            JSON Array type
 */
public abstract class AbstractSettingsToJsonSerializer<OBJECT, ARRAY> {
    
    private static final Logger LOG = Logger.getLogger(AbstractSettingsToJsonSerializer.class.getName());

    protected abstract OBJECT newOBJECT();

    protected abstract void set(OBJECT jsonObject, String property, Object value);

    protected abstract Object get(OBJECT jsonObject, String property);
    
    protected abstract boolean hasProperty(OBJECT jsonObject, String property);

    protected abstract ARRAY ToJsonArray(Iterable<Object> values);

    protected abstract Iterable<Object> fromJsonArray(ARRAY jsonArray);
    
    protected abstract OBJECT parseStringToJsonObject(String jsonString) throws Exception;
    
    public abstract String jsonObjectToString(OBJECT jsonObject);
    
    public abstract boolean isArray(Object jsonValue);
    
    protected OBJECT parseStringToJsonObjectWithExceptionHandling(String jsonString) {
        try {
            return parseStringToJsonObject(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse settings as JSON: " + jsonString, e);
        }
    }
    
    public String serializeToString(SettingsMap settings) {
        return jsonObjectToString(serialize(settings));
    }
    
    public <T extends SettingsMap> T deserialize(T settings, String jsonString) {
        if (jsonString != null && !jsonString.isEmpty()) {
            OBJECT jsonObject = parseStringToJsonObjectWithExceptionHandling(jsonString);
            if(jsonObject != null) {
                deserialize(settings, jsonObject);
            }
        }
        return settings;
    }
    
    public String serializeToString(GenericSerializableSettings settings) {
        return jsonObjectToString(serialize(settings));
    }

    public <T extends GenericSerializableSettings> T deserialize(T settings, String jsonString) {
        if (jsonString != null && !jsonString.isEmpty()) {
            OBJECT jsonObject = parseStringToJsonObjectWithExceptionHandling(jsonString);
            if(jsonObject != null) {
                deserialize(settings, jsonObject);
            }
        }
        return settings;
    }
    
    public OBJECT serialize(SettingsMap settingsMap) {
        final OBJECT jsonObject = newOBJECT();
        serializeToObject(settingsMap, jsonObject);
        return jsonObject;
    }

    private void serializeToObject(SettingsMap settingsMap, final OBJECT jsonObject) {
        for (Map.Entry<String, Settings> entry : settingsMap.getSettingsPerComponentId().entrySet()) {
            String key = entry.getKey();
            Settings settings = entry.getValue();
            if(key == null) {
                if(settings instanceof SettingsMap) {
                    serializeToObject((SettingsMap)settings, jsonObject);
                } else if(settings instanceof GenericSerializableSettings) {
                    serializeToObject((GenericSerializableSettings)settings, jsonObject);
                }
            } else {
                final OBJECT serializedObject;
                if(settings instanceof SettingsMap) {
                    serializedObject = serialize((SettingsMap)settings);
                } else if(settings instanceof GenericSerializableSettings) {
                    serializedObject = serialize((GenericSerializableSettings)settings);
                } else {
                    serializedObject = null;
                }
                if(serializedObject != null) {
                    set(jsonObject, key.toString(), serializedObject);
                }
            }
        }
    }

    public OBJECT serialize(GenericSerializableSettings settings) {
        final OBJECT jsonObject = newOBJECT();
        serializeToObject(settings, jsonObject);
        return jsonObject;
    }

    private void serializeToObject(GenericSerializableSettings settings, final OBJECT jsonObject) {
        for (Map.Entry<String, Setting> entry : settings.getChildSettings().entrySet()) {
            Setting setting = entry.getValue();
            if (!setting.isDefaultValue()) {
                set(jsonObject, entry.getKey(), serialize(setting));
            }
        }
    }

    private Object serialize(Setting setting) {
        if (setting instanceof ValueSetting) {
            return serializeValueSetting((ValueSetting<?>) setting);
        } else if (setting instanceof CollectionSetting) {
            return serializeListSetting((CollectionSetting<?>) setting);
        } else if (setting instanceof GenericSerializableSettings) {
            return serialize((GenericSerializableSettings) setting);
        } else {
            throw new IllegalStateException("Unknown Setting type");
        }
    }

    private <T> Object serializeValueSetting(ValueSetting<T> valueSetting) {
        return valueSetting.getValueConverter().toJSONValue(valueSetting.getValue());
    }

    private <T> Object serializeListSetting(CollectionSetting<T> listSetting) {
        if (listSetting instanceof ValueCollectionSetting) {
            final ValueCollectionSetting<T> valueListSetting = (ValueCollectionSetting<T>) listSetting;
            return serializeValueCollection(valueListSetting);
        } else if (listSetting instanceof SettingsListSetting) {
            List<Object> jsonValues = new ArrayList<>();
            SettingsListSetting<?> settingsListSetting = (SettingsListSetting<?>) listSetting;
            for (GenericSerializableSettings value : settingsListSetting.getValues()) {
                jsonValues.add(serialize(value));
            }
            return ToJsonArray(jsonValues);
        } else {
            throw new IllegalStateException("Unknown ListSetting type");
        }
    }

    private <T> Object serializeValueCollection(ValueCollectionSetting<T> valueListSetting) {
        final ValueConverter<T> converter = valueListSetting.getValueConverter();
        final OBJECT diffDataObject = newOBJECT();
        Iterable<T> addedValues = valueListSetting.getAddedValues();
        if(!Util.isEmpty(addedValues)) {
            set(diffDataObject, AbstractGenericSerializableSettings.ADDED_TOKEN, serializeMultipleValues(converter, addedValues));
        }
        Iterable<T> removedValues = valueListSetting.getRemovedValues();
        if(!Util.isEmpty(removedValues)) {
            set(diffDataObject, AbstractGenericSerializableSettings.REMOVED_TOKEN, serializeMultipleValues(converter, removedValues));
        }
        return diffDataObject;
    }

    private <T> Object serializeMultipleValues(ValueConverter<T> converter, Iterable<T> values) {
        List<Object> jsonValues = new ArrayList<>();
        for (T value : values) {
            jsonValues.add(converter.toJSONValue(value));
        }
        return ToJsonArray(jsonValues);
    }
    
    public <T extends SettingsMap> T deserialize(T settingsMap, OBJECT json) {
        if (json != null) {
            for (Map.Entry<String, Settings> entry : settingsMap.getSettingsPerComponentId().entrySet()) {
                String key = entry.getKey();
                Settings settings = entry.getValue();
                if(key == null) {
                    if(settings instanceof SettingsMap) {
                        deserialize((SettingsMap)settings, json);
                    } else if(settings instanceof GenericSerializableSettings) {
                        deserializeObject((GenericSerializableSettings)settings, json);
                    }
                } else if(hasProperty(json, key)) {
                    Object serializedObject = get(json, key);
                    if(settings instanceof SettingsMap) {
                        @SuppressWarnings("unchecked")
                        OBJECT serializedChildObject = (OBJECT) serializedObject;
                        deserialize((SettingsMap)settings, serializedChildObject);
                    } else if(settings instanceof GenericSerializableSettings) {
                        deserializeObject((GenericSerializableSettings)settings, serializedObject);
                    }
                }
            }
        }
        return settingsMap;
    }

    public <T extends GenericSerializableSettings> T deserialize(T settings, OBJECT json) {
        if (json != null) {
            for (Map.Entry<String, Setting> entry : settings.getChildSettings().entrySet()) {
                Setting setting = entry.getValue();
                if(hasProperty(json, entry.getKey())) {
                    Object jsonValue = get(json, entry.getKey());
                    deserializeSetting(setting, jsonValue);
                }
            }
        }
        return settings;
    }

    private void deserializeSetting(Setting setting, Object jsonValue) {
        if (setting instanceof ValueSetting) {
            deserializeValueSetting(jsonValue, (ValueSetting<?>) setting);
        } else if (setting instanceof CollectionSetting) {
            deserializeListSetting(jsonValue, (CollectionSetting<?>) setting);
        } else if (setting instanceof GenericSerializableSettings) {
            deserializeObject((GenericSerializableSettings) setting, jsonValue);
        } else {
            throw new IllegalStateException("Unknown Setting type");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends GenericSerializableSettings> T deserializeObject(T setting, Object jsonValue) {
        return deserialize(setting, (OBJECT) jsonValue);
    }

    private <T> void deserializeValueSetting(Object jsonValue, ValueSetting<T> valueSetting) {
        final ValueConverter<T> converter = valueSetting.getValueConverter();
        try {
            valueSetting.setValue(converter.fromJSONValue(jsonValue));
        } catch(Exception e) {
            LOG.log(Level.WARNING, "Error while converting JSON value \"" + jsonValue + "\" using converter \""
                    + converter.getClass().getSimpleName() + "\"", e);
        }
    }

    private <T> void deserializeListSetting(Object jsonValue, CollectionSetting<T> listSetting) {
        if (listSetting instanceof ValueCollectionSetting) {
            deserializeValueListSetting(listSetting, jsonValue);
        } else {
            @SuppressWarnings("unchecked")
            ARRAY jsonArray = (ARRAY) jsonValue;
            if (listSetting instanceof SettingsListSetting) {
                deserializeSettingsListSetting(jsonArray, (SettingsListSetting<?>) listSetting);
            } else {
                throw new IllegalStateException("Unknown ListSetting type");
            }
        }
    }

    private <T> void deserializeValueListSetting(CollectionSetting<T> listSetting, Object jsonValue) {
        ValueCollectionSetting<T> valueListSetting = (ValueCollectionSetting<T>) listSetting;
        ValueConverter<T> converter = valueListSetting.getValueConverter();
        if(isArray(jsonValue)) {
            // fallback logic to correctly deserialize values serialized before diffing was introduced
            @SuppressWarnings("unchecked")
            final ARRAY jsonArray = (ARRAY) jsonValue;
            final List<T> values = deserializeMultipleValues(jsonArray, converter);
            valueListSetting.setValues(values);
        } else {
            @SuppressWarnings("unchecked")
            final OBJECT diffDataObject = (OBJECT) jsonValue;
            final List<T> addedValues;
            if (hasProperty(diffDataObject, AbstractGenericSerializableSettings.ADDED_TOKEN)) {
                @SuppressWarnings("unchecked")
                final ARRAY addedValuesArray = (ARRAY) get(diffDataObject, AbstractGenericSerializableSettings.ADDED_TOKEN);
                addedValues = deserializeMultipleValues(addedValuesArray, converter);
            } else {
                addedValues = Collections.emptyList();
            }
            final List<T> removedValues;
            if (hasProperty(diffDataObject, AbstractGenericSerializableSettings.REMOVED_TOKEN)) {
                @SuppressWarnings("unchecked")
                final ARRAY removedValuesArray = (ARRAY) get(diffDataObject, AbstractGenericSerializableSettings.REMOVED_TOKEN);
                removedValues = deserializeMultipleValues(removedValuesArray, converter);
            } else {
                removedValues = Collections.emptyList();
            }
            valueListSetting.setDiff(removedValues, addedValues);
            
        }
    }

    private <T> List<T> deserializeMultipleValues(ARRAY jsonArray, ValueConverter<T> converter) {
        List<T> values = new ArrayList<>();
        if(jsonArray != null) {
            for (Object value : fromJsonArray(jsonArray)) {
                try {
                    values.add(converter.fromJSONValue(value));
                } catch(Exception e) {
                    LOG.log(Level.WARNING, "Error while converting JSON value \"" + value + "\" using converter \""
                            + converter.getClass().getSimpleName() + "\"", e);
                }
            }
        }
        return values;
    }

    private <T extends GenericSerializableSettings> void deserializeSettingsListSetting(ARRAY jsonArray, SettingsListSetting<T> settingsListSetting) {
        List<T> values = new ArrayList<>();
        for (Object value : fromJsonArray(jsonArray)) {
            values.add(deserializeObject(settingsListSetting.getSettingsFactory().newInstance(), value));
        }
        settingsListSetting.setValues(values);
    }
}
