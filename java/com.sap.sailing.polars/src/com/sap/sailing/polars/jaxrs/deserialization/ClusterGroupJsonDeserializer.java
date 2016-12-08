package com.sap.sailing.polars.jaxrs.deserialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.datamining.data.Cluster;
import com.sap.sse.datamining.data.ClusterBoundary;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.data.ClusterWithLowerAndUpperBoundaries;
import com.sap.sse.datamining.impl.data.ClusterWithSingleBoundary;
import com.sap.sse.datamining.impl.data.ComparatorClusterBoundary;
import com.sap.sse.datamining.impl.data.ComparisonStrategy;
import com.sap.sse.datamining.impl.data.FixClusterGroup;

public class ClusterGroupJsonDeserializer<ElementType extends Serializable>
        implements JsonDeserializer<ClusterGroup<ElementType>> {
    public static final String FIELD_CLUSTERS = "clusters";
    public static final String FIELD_BOUNDARIES = "boundaries";
    public static final String FIELD_VALUE = "boundaryValue";
    public static final String FIELD_STRATEGY = "strategy";
    
    private Comparator<ElementType> comparator;

    public ClusterGroupJsonDeserializer(Comparator<ElementType> comparator) {
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ClusterGroup<ElementType> deserialize(JSONObject object) throws JsonDeserializationException {
        Collection<Cluster<ElementType>> clusters = new ArrayList<>();

        JSONArray clustersJSON = (JSONArray) object.get(FIELD_CLUSTERS);
        for (int i = 0; i < clustersJSON.size(); i++) {
            JSONObject clusterJSON = (JSONObject) clustersJSON.get(i);

            JSONArray boundariesJSON = (JSONArray) clusterJSON.get(FIELD_BOUNDARIES);
            Cluster<ElementType> cluster;
            if (boundariesJSON.size() == 2) {
                JSONObject lowerJSON = (JSONObject) boundariesJSON.get(0);
                JSONObject upperJSON = (JSONObject) boundariesJSON.get(1);
                ClusterBoundary<ElementType> lowerBound = new ComparatorClusterBoundary<>(
                        (ElementType) lowerJSON.get(FIELD_VALUE),
                        ComparisonStrategy.valueOf((String) lowerJSON.get(FIELD_STRATEGY)), comparator);

                ClusterBoundary<ElementType> upperBound = new ComparatorClusterBoundary<>(
                        (ElementType) upperJSON.get(FIELD_VALUE),
                        ComparisonStrategy.valueOf((String) upperJSON.get(FIELD_STRATEGY)), comparator);

                cluster = new ClusterWithLowerAndUpperBoundaries<>(lowerBound, upperBound);
            } else {
                JSONObject boundaryJSON = (JSONObject) boundariesJSON.get(0);
                ClusterBoundary<ElementType> boundary = new ComparatorClusterBoundary<>(
                        (ElementType) boundaryJSON.get(FIELD_VALUE),
                        ComparisonStrategy.valueOf((String) boundaryJSON.get(FIELD_STRATEGY)), comparator);
                cluster = new ClusterWithSingleBoundary<>(boundary);
            }
            clusters.add(cluster);
        }

        return new FixClusterGroup<>(clusters);
    }
}
