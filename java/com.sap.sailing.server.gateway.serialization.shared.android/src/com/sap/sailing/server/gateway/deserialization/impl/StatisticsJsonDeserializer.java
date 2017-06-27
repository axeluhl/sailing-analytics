package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.domain.statistics.impl.StatisticsImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsJsonSerializer;

public class StatisticsJsonDeserializer implements JsonDeserializer<Statistics> {

    @Override
    public Statistics deserialize(JSONObject object) throws JsonDeserializationException {
        int competitors = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_COMPETITORS);
        int regattas = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_REGATTAS);
        int races = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_RACES);
        int trackedRaces = (int) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_TRACKED_RACES);
        long gpsFixes = (long) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_GPS_FIXES);
        long windFixes = (long) object.get(StatisticsJsonSerializer.FIELD_NUMBER_OF_WIND_FIXES);
        Distance distance = new MeterDistance((double) object.get(StatisticsJsonSerializer.FIELD_DISTANCE_TRAVELED));
        return new StatisticsImpl(competitors, regattas, races, trackedRaces, gpsFixes, windFixes, distance);
    }

}
