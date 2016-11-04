package com.sap.sailing.polars.jaxrs.deserialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.serialization.ClusterSerializer;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ClusterWithSingleBoundary;

public class ClusterDeserializer<ElementType extends Serializable> implements JsonDeserializer<Cluster<ElementType>> {

    private final ClusterBoundaryDeserializer<ElementType> boundaryDeserializer;

    public ClusterDeserializer(ClusterBoundaryDeserializer<ElementType> boundaryDeserializer) {
        this.boundaryDeserializer = boundaryDeserializer;
    }

    @Override
    public Cluster<ElementType> deserialize(JSONObject object) throws JsonDeserializationException {
        Cluster<ElementType> cluster = null;
        JSONArray boundariesJSON = (JSONArray) object.get(ClusterSerializer.FIELD_BOUNDARIES);

        List<ClusterBoundary<ElementType>> clusterBoundaries = new ArrayList<>();
        for (int i = 0; i < boundariesJSON.size(); i++) {
            clusterBoundaries.add(boundaryDeserializer.deserialize((JSONObject) boundariesJSON.get(i)));
        }

        if (clusterBoundaries.size() == 1) {
            cluster = new ClusterWithSingleBoundary<>(clusterBoundaries.get(0));
        } else if (clusterBoundaries.size() == 2) {
            cluster = new ClusterWithLowerAndUpperBoundaries<>(clusterBoundaries.get(0), clusterBoundaries.get(1));
        } else {
            throw new JsonDeserializationException("Unknown Cluster type");
        }

        return cluster;
    }

}
