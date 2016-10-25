package com.sap.sailing.polars.jaxrs.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.serialization.GroupKeySerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public class GroupKeyDeserializer<T> implements JsonDeserializer<GroupKey> {

    @SuppressWarnings("unchecked")
    @Override
    public GroupKey deserialize(JSONObject object) throws JsonDeserializationException {
        return new GenericGroupKey<T>((T) object.get(GroupKeySerializer.FIELD_GROUP_KEY));
    }

}
