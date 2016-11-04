package com.sap.sailing.polars.jaxrs.serialization;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class CompoundGroupKeySerializer<M, S> implements JsonSerializer<GroupKey> {

    private final String mainKeyName;
    private final String subKeyName;

    private final JsonSerializer<M> mainKeySerializer;
    private final JsonSerializer<S> subKeySerializer;

    public CompoundGroupKeySerializer(String mainKeyName, String subKeyName, JsonSerializer<M> mainKeySerializer,
            JsonSerializer<S> subKeySerializer) {
        this.mainKeyName = mainKeyName;
        this.subKeyName = subKeyName;
        this.mainKeySerializer = mainKeySerializer;
        this.subKeySerializer = subKeySerializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject serialize(GroupKey object) {
        JSONObject keyJSON = new JSONObject();

        if (object.getKeys() == null || object.getKeys().size() != 2) {
            return keyJSON;
        }

        for (GroupKey groupKey : object.getKeys()) {
            if (!(groupKey instanceof GenericGroupKey)) {
                return keyJSON;
            }
        }

        GenericGroupKey<M> mainKey = (GenericGroupKey<M>) object.getKeys().get(0);
        GenericGroupKey<S> subKey = (GenericGroupKey<S>) object.getKeys().get(1);

        keyJSON.put(mainKeyName, mainKeySerializer.serialize(mainKey.getValue()));
        keyJSON.put(subKeyName, subKeySerializer.serialize(subKey.getValue()));

        return keyJSON;
    }

}
