package com.sap.sailing.polars.jaxrs.serialization;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.impl.data.AbstractCluster;

public class ClusterSerializer<ElementType extends Serializable> implements JsonSerializer<Cluster<ElementType>> {

    public static final String FIELD_BOUNDARIES = "boundaries";
    private final ClusterBoundarySerializer<ElementType> boundarySerializer;

    public ClusterSerializer(ClusterBoundarySerializer<ElementType> boundarySerializer) {
        this.boundarySerializer = boundarySerializer;
    }

    @Override
    public JSONObject serialize(Cluster<ElementType> object) {
        JSONObject clusterJSON = new JSONObject();
        // Check reference type to avoid interface modification
        if (!(object instanceof AbstractCluster)) {
            throw new IllegalArgumentException("Unknown Cluster type");
        }
        JSONArray boundariesJSON = new JSONArray();
        AbstractCluster<ElementType> abstractCluster = (AbstractCluster<ElementType>) object;
        for (ClusterBoundary<ElementType> clusterBoundary : abstractCluster.getClusterBoundaries()) {
            boundariesJSON.add(boundarySerializer.serialize(clusterBoundary));
        }
        clusterJSON.put(FIELD_BOUNDARIES, boundariesJSON);
        return clusterJSON;
    }

}
