package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MapEntrySerializer<K, V> implements JsonSerializer<Entry<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";

    private final JsonSerializer<K> keySerializer;
    private final JsonSerializer<V> valueSerializer;

    /**
     * @param keySerializer must not be {@code null}
     * @param keySerializer must not be {@code null}
     */
    public MapEntrySerializer(JsonSerializer<K> keySerializer, JsonSerializer<V> valueSerializer) {
        assert keySerializer != null;
        assert valueSerializer != null;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public JSONObject serialize(Entry<K, V> entry) {
        JSONObject entryJSON = new JSONObject();

        if (keySerializer != null) {
            entryJSON.put(FIELD_KEY, keySerializer.serialize(entry.getKey()));
        } else {
            entryJSON.put(FIELD_KEY, entry.getKey());
        }

        if (valueSerializer != null) {
            entryJSON.put(FIELD_VALUE, valueSerializer.serialize(entry.getValue()));
        } else {
            entryJSON.put(FIELD_VALUE, entry.getValue());
        }
        return entryJSON;
    }

    public JsonSerializer<K> getKeySerializer() {
        return keySerializer;
    }

    public JsonSerializer<V> getValueSerializer() {
        return valueSerializer;
    }
}
