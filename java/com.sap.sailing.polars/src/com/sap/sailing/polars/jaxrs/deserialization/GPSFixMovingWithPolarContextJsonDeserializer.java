package com.sap.sailing.polars.jaxrs.deserialization;

import java.io.Serializable;
import java.util.Comparator;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TrackedRaceJsonDeserializer;
import com.sap.sse.datamining.data.ClusterGroup;

public class GPSFixMovingWithPolarContextJsonDeserializer implements JsonDeserializer<GPSFixMovingWithPolarContext> {

    public static final String FIELD_FIX = "fix";
    public static final String FIELD_RACE = "race";
    public static final String FIELD_COMPETITOR = "competitor";
    public static final String FIELD_CLUSTER_GROUP = "angleClusterGroup";
    
    @Override
    public GPSFixMovingWithPolarContext deserialize(JSONObject object) throws JsonDeserializationException {
        SharedDomainFactory factory = DomainFactory.INSTANCE;

        GPSFixMoving fixMoving = new GPSFixMovingJsonDeserializer().deserialize((JSONObject) object.get(FIELD_FIX));
        TrackedRace race = new TrackedRaceJsonDeserializer().deserialize((JSONObject) object.get(FIELD_RACE));
        Competitor competitor = new CompetitorJsonDeserializer(factory.getCompetitorStore())
                .deserialize((JSONObject) object.get(FIELD_COMPETITOR));
        ClusterGroup<Bearing> angleClusterGroup = new ClusterGroupJsonDeserializer<Bearing>(new BearingComparator())
                .deserialize((JSONObject) object.get(FIELD_CLUSTER_GROUP));

        return new GPSFixMovingWithPolarContext(fixMoving, race, competitor, angleClusterGroup);
    }

    private static class BearingComparator implements Comparator<Bearing>, Serializable {
        private static final long serialVersionUID = -3773171643340188785L;

        @Override
        public int compare(Bearing left, Bearing right) {
            return new Double(left.getDegrees()).compareTo(new Double(right.getDegrees()));
        }
    };

}
