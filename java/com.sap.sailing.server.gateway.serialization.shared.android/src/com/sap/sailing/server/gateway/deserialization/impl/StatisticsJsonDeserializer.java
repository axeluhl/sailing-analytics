package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsJsonSerializer;

public class StatisticsJsonDeserializer implements JsonDeserializer<Statistics> {

    @Override
    public Statistics deserialize(JSONObject object) throws JsonDeserializationException {
        int numberOfCompetitors = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_COMPETITORS);
        int numberOfRegattas = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_REGATTAS);
        int numberOfRaces = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_RACES);
        int numberOfTrackedRaces = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_TRACKED_RACES);
        long numberOfGPSFixes = (long) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_GPS_FIXES);
        long numberOfWindFixes = (long) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_WIND_FIXES);
        Distance distanceTraveled = (Distance) object.get(StatisticsJsonSerializer.FIELD_DISTANCE_TRAVELED);
        return new StatisticsImpl(numberOfCompetitors, numberOfRegattas, numberOfRaces, numberOfTrackedRaces,
                numberOfGPSFixes, numberOfWindFixes, distanceTraveled);
    }

}
