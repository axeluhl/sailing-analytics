package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class MapEntryDeserializer<K, V> implements JsonDeserializer<Entry<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";

    private JsonDeserializer<K> keyDeserializer;
    private JsonDeserializer<V> valueDeserializer;

    public MapEntryDeserializer() {
        this(null, null);
    }

    public MapEntryDeserializer(JsonDeserializer<K> keyDeserializer, JsonDeserializer<V> valueDeserializer) {
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V> deserialize(JSONObject object) throws JsonDeserializationException {
        K key;
        if (keyDeserializer != null) {
            key = keyDeserializer.deserialize((JSONObject) object.get(FIELD_KEY));
        } else {
            key = (K) object.get(FIELD_KEY);
        }
        
        V value;
        if (valueDeserializer != null) {
            value = valueDeserializer.deserialize((JSONObject) object.get(FIELD_VALUE));
        } else {
            value = (V) object.get(FIELD_VALUE);
        }
        
        return new AbstractMap.SimpleEntry<K, V>(key, value);
    }

    public JsonDeserializer<K> getKeyDeserializer() {
        return keyDeserializer;
    }

    public void setKeyDeserializer(JsonDeserializer<K> keyDeserializer) {
        this.keyDeserializer = keyDeserializer;
    }

    public JsonDeserializer<V> getValueDeserializer() {
        return valueDeserializer;
    }

    public void setValueDeserializer(JsonDeserializer<V> valueDeserializer) {
        this.valueDeserializer = valueDeserializer;
    }
    
}
