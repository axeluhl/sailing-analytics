package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import com.sap.sailing.server.gateway.serialization.JsonArraySerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MapSerializer<K, V> implements JsonArraySerializer<Map<K, V>> {
    private final MapEntrySerializer<K, V> mapEntrySerializer;

    /**
     * @param keySerializer must not be {@code null}
     * @param valueSerializer must not be {@code null}
     */
    public MapSerializer(JsonSerializer<K> keySerializer, JsonSerializer<V> valueSerializer) {
        assert keySerializer != null;
        assert valueSerializer != null;
        this.mapEntrySerializer = new MapEntrySerializer<>(keySerializer, valueSerializer);
    }

    @Override
    public JSONArray serialize(Map<K, V> map) {
        JSONArray entriesJSON = new JSONArray();
        for (Entry<K, V> entry : map.entrySet()) {
            entriesJSON.add(mapEntrySerializer.serialize(entry));
        }
        return entriesJSON;
    }
}
