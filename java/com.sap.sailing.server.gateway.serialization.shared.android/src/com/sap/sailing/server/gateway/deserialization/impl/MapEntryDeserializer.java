package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class MapEntryDeserializer<K, V> implements JsonDeserializer<Entry<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";

    private final JsonDeserializer<K> keyDeserializer;
    private final JsonDeserializer<V> valueDeserializer;

    /**
     * @param keyDeserializer must not be {@code null}
     * @param valueDeserializer must not be {@code null}
     */
    public MapEntryDeserializer(JsonDeserializer<K> keyDeserializer, JsonDeserializer<V> valueDeserializer) {
        assert keyDeserializer != null;
        assert valueDeserializer != null;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    @Override
    public Entry<K, V> deserialize(JSONObject object) throws JsonDeserializationException {
        K key = keyDeserializer.deserialize((JSONObject) object.get(FIELD_KEY));
        V value = valueDeserializer.deserialize((JSONObject) object.get(FIELD_VALUE));

        return new AbstractMap.SimpleEntry<K, V>(key, value);
    }
}
