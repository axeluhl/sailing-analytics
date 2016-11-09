package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class CompoundGroupKeySerializer<M, S> implements JsonSerializer<GroupKey> {

    private static final int COMPOUND_KEY_DEPTH = 2;
    private static final int MAIN_KEY_INDEX = 0;
    private static final int SUB_KEY_INDEX = 1;
    
    private final String mainKeyName;
    private final String subKeyName;

    private final JsonSerializer<M> mainKeySerializer;
    private final JsonSerializer<S> subKeySerializer;

    public CompoundGroupKeySerializer(String mainKeyName, String subKeyName, JsonSerializer<M> mainKeySerializer,
            JsonSerializer<S> subKeySerializer) {
        assert mainKeyName != null;
        assert subKeyName != null;
        assert mainKeySerializer != null;
        assert subKeySerializer != null;

        this.mainKeyName = mainKeyName;
        this.subKeyName = subKeyName;
        this.mainKeySerializer = mainKeySerializer;
        this.subKeySerializer = subKeySerializer;
    }

    @Override
    public JSONObject serialize(GroupKey object) {
        final JSONObject keyJSON = new JSONObject();
        if (object.getKeys() == null || object.getKeys().size() != COMPOUND_KEY_DEPTH) {
            return keyJSON;
        }
        for (GroupKey groupKey : object.getKeys()) {
            if (!(groupKey instanceof GenericGroupKey)) {
                return keyJSON;
            }
        }
        @SuppressWarnings("unchecked")
        GenericGroupKey<M> mainKey = (GenericGroupKey<M>) object.getKeys().get(MAIN_KEY_INDEX);
        @SuppressWarnings("unchecked")
        GenericGroupKey<S> subKey = (GenericGroupKey<S>) object.getKeys().get(SUB_KEY_INDEX);
        keyJSON.put(mainKeyName, mainKeySerializer.serialize(mainKey.getValue()));
        keyJSON.put(subKeyName, subKeySerializer.serialize(subKey.getValue()));
        return keyJSON;
    }

}
