package com.sap.sailing.polars.jaxrs.deserialization;

import java.util.Arrays;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class CompoundGroupKeyDeserializer<M, S> implements JsonDeserializer<GroupKey> {
    
    private final String mainKeyName;
    private final String subKeyName;
    
    private final JsonDeserializer<M> mainKeyDeserializer;
    private final JsonDeserializer<S> subKeyDeserializer;
    
    public CompoundGroupKeyDeserializer(String mainKeyName, String subKeyName, JsonDeserializer<M> mainKeyDeserializer,
            JsonDeserializer<S> subKeyDeserializer) {
        this.mainKeyName = mainKeyName;
        this.subKeyName = subKeyName;
        this.mainKeyDeserializer = mainKeyDeserializer;
        this.subKeyDeserializer = subKeyDeserializer;
    }

    @Override
    public GroupKey deserialize(JSONObject object) throws JsonDeserializationException {
        GenericGroupKey<M> mainKey = new GenericGroupKey<M>(mainKeyDeserializer.deserialize((JSONObject) object.get(mainKeyName)));
        GenericGroupKey<S> subKey = new GenericGroupKey<S>(subKeyDeserializer.deserialize((JSONObject) object.get(subKeyName)));
        return new CompoundGroupKey(Arrays.asList(mainKey, subKey));
    }

}
