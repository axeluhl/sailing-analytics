package com.sap.sailing.polars.jaxrs.serialization;

import java.io.Serializable;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.AbstractClusterBoundary;

public class ClusterBoundarySerializer<ElementType extends Serializable>
        implements JsonSerializer<ClusterBoundary<ElementType>> {

    public static final String FIELD_STRATEGY = "strategy";
    public static final String FIELD_VALUE = "value";
    private final JsonSerializer<ElementType> valueSerializer;

    public ClusterBoundarySerializer(JsonSerializer<ElementType> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    @Override
    public JSONObject serialize(ClusterBoundary<ElementType> object) {
        JSONObject boundaryJSON = new JSONObject();

        if (!(object instanceof AbstractClusterBoundary)) {
            throw new IllegalStateException("Unknown ClusterBoundary type");
        }

        AbstractClusterBoundary<ElementType> abstractBoundary = (AbstractClusterBoundary<ElementType>) object;
        boundaryJSON.put(FIELD_VALUE, valueSerializer.serialize(abstractBoundary.getBoundaryValue()));
        boundaryJSON.put(FIELD_STRATEGY, object.getStrategy().toString());

        return boundaryJSON;
    }

}
