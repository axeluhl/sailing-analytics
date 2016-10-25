package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MapSerializer<K, V> implements JsonSerializer<Map<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";
    
    private MapEntrySerializer<K, V> mapEntrySerializer;
    private String mapName;

    public MapSerializer(String mapName) {
        this(mapName, null, null);
    }

    public MapSerializer(String mapName, JsonSerializer<K> keySerializer, JsonSerializer<V> valueSerializer) {
        this.mapEntrySerializer = new MapEntrySerializer<>(keySerializer, valueSerializer);
        this.mapName = mapName;
    }

    @Override
    public JSONObject serialize(Map<K, V> map) {
        JSONObject mapJSON = new JSONObject();
        
        JSONArray entriesJSON = new JSONArray();
        for (Entry<K, V> entry: map.entrySet()) {
            entriesJSON.add(mapEntrySerializer.serialize(entry));
        }
        mapJSON.put(mapName, entriesJSON);

        return mapJSON;
    }

    public void setKeySerializer(JsonSerializer<K> keySerializer) {
        mapEntrySerializer.setKeySerializer(keySerializer);
    }

    public void setValueSerializer(JsonSerializer<V> valueSerializer) {
        mapEntrySerializer.setValueSerializer(valueSerializer);
    }

    public JsonSerializer<K> getKeySerializer() {
        return mapEntrySerializer.getKeySerializer();
    }

    public JsonSerializer<V> getValueSerializer() {
        return mapEntrySerializer.getValueSerializer();
    }
    
}
