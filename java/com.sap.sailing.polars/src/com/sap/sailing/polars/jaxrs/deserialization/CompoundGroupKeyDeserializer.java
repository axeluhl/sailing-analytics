package com.sap.sailing.polars.jaxrs.deserialization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.CompoundGroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class CompoundGroupKeyDeserializer implements JsonDeserializer<GroupKey> {
    private final LinkedHashMap<String, JsonDeserializer<?>> deserializers;
    
    public CompoundGroupKeyDeserializer(LinkedHashMap<String, JsonDeserializer<?>> deserializers) {
        this.deserializers = deserializers;
    }

    @Override
    public GroupKey deserialize(JSONObject object) throws JsonDeserializationException {
        final List<GroupKey> keys = new ArrayList<>();
        assert object.size() == deserializers.size();
        for (final Entry<String, JsonDeserializer<?>> keyNameAndDeserializer : deserializers.entrySet()) {
            Object key = keyNameAndDeserializer.getValue().deserialize((JSONObject) object.get(keyNameAndDeserializer.getKey()));
            if (key instanceof GroupKey) {
                keys.add((GroupKey) key);
            } else {
                keys.add(new GenericGroupKey<Object>(key));
            }
        }
        return new CompoundGroupKey(keys);
    }

}
