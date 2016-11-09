package com.sap.sailing.polars.jaxrs.serialization;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class CompoundGroupKeySerializer implements JsonSerializer<GroupKey> {
    private final LinkedHashMap<String, JsonSerializer<?>> subKeySerializers;

    /**
     * The order of serializers must match with the order of the sub-keys they are responsible to
     * de-serialize
     * 
     * @param subKeySerializers keys are the key names used as key in the JSON object constructed
     */
    public CompoundGroupKeySerializer(LinkedHashMap<String, JsonSerializer<?>> subKeySerializers) {
        this.subKeySerializers = subKeySerializers;
    }

    @Override
    public JSONObject serialize(GroupKey object) {
        final JSONObject keyJSON = new JSONObject();
        if (object.getKeys() == null || object.getKeys().size() != subKeySerializers.size()) {
            return keyJSON;
        }
        final Iterator<? extends GroupKey> keyIter = object.getKeys().iterator();
        for (final Entry<String, JsonSerializer<?>> keyNameAndSerializer : subKeySerializers.entrySet()) {
            GroupKey key = keyIter.next();
            @SuppressWarnings("unchecked")
            JsonSerializer<Object> groupKeySerializer = (JsonSerializer<Object>) keyNameAndSerializer.getValue();
            final Object keyObject;
            if (key instanceof GenericGroupKey<?>) {
                keyObject = ((GenericGroupKey<?>) key).getValue();
            } else {
                keyObject = key;
            }
            addToJson(keyJSON, keyNameAndSerializer.getKey(), groupKeySerializer, keyObject);
        }
        return keyJSON;
    }

    protected <K> void addToJson(final JSONObject keyJSON, final String keyName, final JsonSerializer<K> keySerializer, K key) {
        keyJSON.put(keyName, keySerializer.serialize(key));
    }

}
