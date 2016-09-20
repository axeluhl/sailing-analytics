package com.sap.sailing.polars.jaxrs.api;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DefaultWindTrackJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixMovingJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TrackedRaceJsonSerializer;

public class GPSFixMovingWithPolarContextJsonSerializer implements JsonSerializer<GPSFixMovingWithPolarContext> {
    @Override
    public JSONObject serialize(GPSFixMovingWithPolarContext object) {
        JSONObject result = new JSONObject();

        result.put("fix", new GPSFixMovingJsonSerializer().serialize(object.getFix()));
        result.put("race", new TrackedRaceJsonSerializer(new DefaultWindTrackJsonSerializer(10000)).serialize(object.getRace()));
        result.put("competitor", new CompetitorJsonSerializer().serialize(object.getCompetitor()));
        result.put("angleClusterGroup", new ClusterGroupJsonSerializer<Bearing>().serialize(object.getAngleClusterGroup()));

        return result;
    }
    

}
