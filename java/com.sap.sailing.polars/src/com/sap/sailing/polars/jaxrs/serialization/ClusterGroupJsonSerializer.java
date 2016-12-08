package com.sap.sailing.polars.jaxrs.serialization;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.polars.jaxrs.deserialization.ClusterGroupJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.data.restricted.ClusterBoundaryExtended;
import com.sap.sse.datamining.data.restricted.ClusterExtended;
import com.sap.sse.datamining.data.restricted.ClusterGroupExtended;

public class ClusterGroupJsonSerializer<ElementType extends Serializable>
        implements JsonSerializer<ClusterGroup<ElementType>> {
    @Override
    public JSONObject serialize(ClusterGroup<ElementType> clusterGroup) {
        JSONObject result = new JSONObject();
        JSONArray clusters = new JSONArray();

        ClusterGroupExtended<ElementType> groupExtended = (ClusterGroupExtended<ElementType>) clusterGroup;
        for (Cluster<ElementType> cluster : groupExtended.getClusters()) {
            JSONObject jsonCluster = new JSONObject();
            JSONArray boundaries = new JSONArray();

            ClusterExtended<ElementType> clusterExtended = (ClusterExtended<ElementType>) cluster;
            for (ClusterBoundary<ElementType> boundary : clusterExtended.getBoundaries()) {
                ClusterBoundaryExtended<ElementType> boundaryExtended = (ClusterBoundaryExtended<ElementType>) boundary;
                JSONObject jsonBoundary = new JSONObject();
                jsonBoundary.put(ClusterGroupJsonDeserializer.FIELD_STRATEGY, boundaryExtended.getStrategy());
                //toString is used to serialize value as string like "5.0°"
                jsonBoundary.put(ClusterGroupJsonDeserializer.FIELD_VALUE, boundaryExtended.getBoundaryValue().toString());
                boundaries.add(jsonBoundary);
            }
            jsonCluster.put(ClusterGroupJsonDeserializer.FIELD_BOUNDARIES, boundaries);
            clusters.add(jsonCluster);
        }
        result.put(ClusterGroupJsonDeserializer.FIELD_CLUSTERS, clusters);

        return result;
    }

}
