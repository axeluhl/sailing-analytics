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
        int competitors = getIntValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_COMPETITORS);
        int regattas = getIntValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_REGATTAS);
        int races = getIntValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_RACES);
        int trackedRaces = getIntValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_TRACKED_RACES);
        long gpsFixes = getLongValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_GPS_FIXES);
        long windFixes = getLongValue(object, StatisticsJsonSerializer.FIELD_NUMBER_OF_WIND_FIXES);
        Distance distance = new MeterDistance(getDoubleValue(object, StatisticsJsonSerializer.FIELD_DISTANCE_TRAVELED));
        return new StatisticsImpl(competitors, regattas, races, trackedRaces, gpsFixes, windFixes, distance);
    }

    private int getIntValue(JSONObject object, String field) {
        return ((Number) object.get(field)).intValue();
    }
    
    private long getLongValue(JSONObject object, String field) {
        return ((Number) object.get(field)).longValue();
    }
    
    private double getDoubleValue(JSONObject object, String field) {
        return ((Number) object.get(field)).doubleValue();
    }
}
