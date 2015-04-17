package com.sap.sse.common.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SettingsToStringSerializer {
    private final static String TYPE_PROPERTY_SUFFIX = "___TYPE";
    
    public JSONObject serialize(Settings settings) {
        final Map<String, Setting> settingsToSerialize = settings.getNonDefaultSettings();
        JSONObject jsonObject = new JSONObject();
        for (Entry<String, Setting> e : settingsToSerialize.entrySet()) {
            Object settingAsJson = serialize(e.getValue());
            jsonObject.put(escapePropertyName(e.getKey()), settingAsJson);
            jsonObject.put(getTypePropertyName(e.getKey()), e.getValue().getType().name());
        }
        return jsonObject;
    }

    private String getTypePropertyName(String unescapedKey) {
        return unescapedKey+TYPE_PROPERTY_SUFFIX;
    }

    private boolean isTypePropertyName(String unescapedKey) {
        return unescapedKey.endsWith(TYPE_PROPERTY_SUFFIX);
    }
    
    /**
     * If <code>key</code> looks like a result of {@link #getTypePropertyName(String)}, the string is escaped such that
     * it does not more and that {@link #unescapePropertyName} will return <code>key</code>.<p>
     */
    String escapePropertyName(String key) {
        final String result;
        if (key.endsWith(TYPE_PROPERTY_SUFFIX)) {
            result = key + TYPE_PROPERTY_SUFFIX; // duplicate the suffix; unescaping will remove one occurrence again
        } else {
            result = key;
        }
        return result;
    }
    
    String unescapePropertyName(String escapedKey) {
        final String result;
        if (escapedKey.endsWith(TYPE_PROPERTY_SUFFIX+TYPE_PROPERTY_SUFFIX)) {
            result = escapedKey.substring(0, escapedKey.length()-TYPE_PROPERTY_SUFFIX.length());
        } else {
            result = escapedKey;
        }
        return result;
    }
    
    private Object serialize(Setting setting) {
        switch (setting.getType()) {
        case ENUM:
            return serialize((EnumSetting<?>) setting);
        case LIST:
            return serialize((ListSetting<?>) setting);
        case MAP:
            return serialize((Settings) setting);
        case NUMBER:
            return serialize((NumberSetting) setting);
        case STRING:
            return serialize((StringSetting) setting);
        default:
            throw new IllegalArgumentException("Don't know setting of type "+setting.getType());
        }
    }
    
    private <T extends Enum<T>> String serialize(EnumSetting<T> enumSetting) {
        return enumSetting.getValue().getClass().getName()+"/"+enumSetting.getValue().name();
    }

    private <T extends Setting> JSONArray serialize(ListSetting<T> listSetting) {
        JSONArray result = new JSONArray();
        for (T t : listSetting) {
            result.add(serialize(t));
        }
        return result;
    }

    private Number serialize(NumberSetting numberSetting) {
        return numberSetting.getNumber();
    }

    private String serialize(StringSetting stringSetting) {
        return stringSetting.getString();
    }

    public Settings deserialize(JSONObject json) throws ClassNotFoundException {
        final Map<String, Setting> settings = new HashMap<>();
        for (Entry<Object, Object> e : json.entrySet()) {
            String escapedKey = (String) e.getKey();
            String unescapedKey = unescapePropertyName(escapedKey);
            if (!isTypePropertyName(unescapedKey)) {
                final Object value = e.getValue();
                String typePropertyName = getTypePropertyName(unescapedKey);
                // all properties must specify their type unless they are of type String, JSONArray or Number;
                // however, properties of type enum will be represented as strings
                final SettingType type;
                if (json.containsKey(typePropertyName)) {
                    type = SettingType.valueOf((String) json.get(typePropertyName));
                } else {
                    type = getSettingType(value);
                }
                Setting setting = createSettingFromObjectAndType(value, type);
                settings.put(unescapePropertyName(escapedKey), setting);
            }
        }
        return new AbstractSettings() {
            @Override
            public Map<String, Setting> getNonDefaultSettings() {
                return settings;
            }
        };
    }

    private SettingType getSettingType(final Object value) {
        final SettingType type;
        if (value instanceof String) {
            type = SettingType.STRING;
        } else if (value instanceof Number){
            type = SettingType.NUMBER;
        } else if (value instanceof JSONArray) {
            type = SettingType.LIST;
        } else if (value instanceof JSONObject) {
            type = SettingType.MAP;
        } else {
            throw new IllegalArgumentException("Don't know how to de-serialize setting "+value+" of type "+value.getClass());
        }
        return type;
    }

    private Setting createSettingFromObjectAndType(Object obj, final SettingType type)
            throws ClassNotFoundException {
        final Setting setting;
        switch (type) {
        case ENUM:
            String[] enumClassNameAndLiteral = ((String) obj).split("/");
            Enum<?> value = null;
            @SuppressWarnings("unchecked")
            Class<Enum<?>> enumClass = (Class<Enum<?>>) Class.forName(enumClassNameAndLiteral[0]);
            for (Enum<?> literal : enumClass.getEnumConstants()) {
                if (literal.name().equals(enumClassNameAndLiteral[1])) {
                    value = literal;
                }
            }
            setting = new EnumSetting<>(value);
            break;
        case LIST:
            JSONArray array = (JSONArray) obj;
            List<Setting> settingList = new ArrayList<>(); 
            for (Object o : array) {
                settingList.add(createSettingFromObjectAndType(o, getSettingType(o)));
            }
            setting = new ListSetting<>(settingList);
            break;
        case MAP:
            setting = deserialize((JSONObject) obj);
            break;
        case NUMBER:
            setting = new NumberSetting((Number) obj);
            break;
        case STRING:
            setting = new StringSetting((String) obj);
            break;
        default:
            throw new IllegalArgumentException("Don't know how to de-serialize setting "+obj+" of type "+obj.getClass());
        }
        return setting;
    }
}
