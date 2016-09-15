package com.sap.sailing.polars.jaxrs.api;

import java.io.Serializable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;

public class FixClusterGroupJsonSerializer<ElementType extends Serializable> implements JsonSerializer<ClusterGroup<ElementType>> {
    @Override
    public JSONObject serialize(ClusterGroup<ElementType> clusterGroup) {
        JSONObject result = new JSONObject();
        JSONArray clusters = new JSONArray();
        
        for(Cluster<ElementType> cluster: clusterGroup.getClusters()) {
            JSONObject jsonCluster = new JSONObject();
            JSONArray boundaries = new JSONArray();
            
            for (ClusterBoundary<ElementType> boundary: cluster.getBoundaries()) {
                JSONObject jsonBoundary = new JSONObject();
                jsonBoundary.put("strategy", boundary.getStrategy());
                jsonBoundary.put("boundaryValue", boundary.getBoundaryValue());
                boundaries.add(jsonBoundary);
            }
            jsonCluster.put("boundaries", boundaries);
            clusters.add(jsonCluster);
        }
        result.put("clusters", clusters);

        return result;
    }
    

}
