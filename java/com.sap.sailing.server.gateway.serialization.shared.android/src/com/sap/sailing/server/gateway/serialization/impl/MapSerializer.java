package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import com.sap.sailing.server.gateway.serialization.JsonArraySerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;


public class MapSerializer<K, V> implements JsonArraySerializer<Map<K, V>> {

    public static final String FIELD_VALUE = "value";
    public static final String FIELD_KEY = "key";
    
    private MapEntrySerializer<K, V> mapEntrySerializer;

    public MapSerializer() {
        this(null, null);
    }
    /**
     * @param keySerializer if {@code null}, {@link Object#toString()} will be used to serialize the keys.
     * @param keySerializer if {@code null}, {@link Object#toString()} will be used to serialize the values.
     */
    public MapSerializer(JsonSerializer<K> keySerializer, JsonSerializer<V> valueSerializer) {
        this.mapEntrySerializer = new MapEntrySerializer<>(keySerializer, valueSerializer);
    }

    @Override

    public JSONArray serialize(Map<K, V> map) {
        JSONArray entriesJSON = new JSONArray();
        for (Entry<K, V> entry: map.entrySet()) {
            entriesJSON.add(mapEntrySerializer.serialize(entry));
        }

        return entriesJSON;
    }


    public JsonSerializer<K> getKeySerializer() {
        return mapEntrySerializer.getKeySerializer();
    }

    public JsonSerializer<V> getValueSerializer() {
        return mapEntrySerializer.getValueSerializer();
    }
    
}
