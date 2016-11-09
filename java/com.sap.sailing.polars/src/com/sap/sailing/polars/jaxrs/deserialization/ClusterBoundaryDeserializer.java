package com.sap.sailing.polars.jaxrs.deserialization;

import java.io.Serializable;
import java.util.Comparator;

import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.serialization.ClusterBoundarySerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparatorClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;

public class ClusterBoundaryDeserializer<ElementType extends Serializable>
        implements JsonDeserializer<ClusterBoundary<ElementType>> {

    private final JsonDeserializer<ElementType> valueDeserializer;
    private final Comparator<ElementType> comparator;

    public ClusterBoundaryDeserializer(JsonDeserializer<ElementType> valueDeserializer,
            Comparator<ElementType> comparator) {
        this.valueDeserializer = valueDeserializer;
        this.comparator = comparator;
    }

    @Override
    public ClusterBoundary<ElementType> deserialize(JSONObject object) throws JsonDeserializationException {
        ElementType value = valueDeserializer.deserialize((JSONObject) object.get(ClusterBoundarySerializer.FIELD_VALUE));
        ComparisonStrategy strategy = ComparisonStrategy.valueOf((String) object.get(ClusterBoundarySerializer.FIELD_STRATEGY));
        return new ComparatorClusterBoundary<>(value, strategy, comparator);
    }

}
