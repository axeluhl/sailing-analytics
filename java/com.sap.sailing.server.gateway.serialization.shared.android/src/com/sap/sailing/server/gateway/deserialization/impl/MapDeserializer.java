package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonArrayDeserializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class MapDeserializer<K, V> implements JsonArrayDeserializer<Map<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";
    
    private final MapEntryDeserializer<K, V> mapEntryDeserializer;

    public MapDeserializer() {
        this(null, null);
    }

    public MapDeserializer(JsonDeserializer<K> keyDeserializer, JsonDeserializer<V> valueDeserializer) {
        this.mapEntryDeserializer = new MapEntryDeserializer<>(keyDeserializer, valueDeserializer);
    }

    @Override
    public Map<K, V> deserialize(JSONArray arrayJSON) throws JsonDeserializationException {
        Map<K, V> map = new HashMap<>();
        
        for (int i = 0; i < arrayJSON.size(); i++) {
            Entry<K, V> entry = mapEntryDeserializer.deserialize((JSONObject) arrayJSON.get(i));
            map.put(entry.getKey(), entry.getValue());
        }
        
        return map;
    }

    public void setKeyDeserializer(JsonDeserializer<K> keyDeserializer) {
        mapEntryDeserializer.setKeyDeserializer(keyDeserializer);
    }

    public void setValueDeserializer(JsonDeserializer<V> valueDeserializer) {
        mapEntryDeserializer.setValueDeserializer(valueDeserializer);
    }

    public JsonDeserializer<K> getKeyDeserializer() {
        return mapEntryDeserializer.getKeyDeserializer();
    }

    public JsonDeserializer<V> getValueDeserializer() {
        return mapEntryDeserializer.getValueDeserializer();
    }

}
