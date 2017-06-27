package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsByYearJsonSerializer;

public class StatisticsByYearJsonDeserializer implements JsonDeserializer<Map<Integer, Statistics>> {

    private final StatisticsJsonDeserializer statisticsJsonDeserializer;

    public StatisticsByYearJsonDeserializer(StatisticsJsonDeserializer statisticsJsonDeserializer) {
        this.statisticsJsonDeserializer = statisticsJsonDeserializer;
    }

    @Override
    public Map<Integer, Statistics> deserialize(JSONObject object) throws JsonDeserializationException {
        Map<Integer, Statistics> result = new HashMap<>();
        for (Object entry : (JSONArray) object.get(StatisticsByYearJsonSerializer.FIELD_ARRAY)) {
            JSONObject jsonEntry = (JSONObject) entry;
            Integer year = (Integer) jsonEntry.get(StatisticsByYearJsonSerializer.FIELD_YEAR);
            Statistics statistics = statisticsJsonDeserializer
                    .deserialize((JSONObject) jsonEntry.get(StatisticsByYearJsonSerializer.FIELD_STATISTICS));
            result.put(year, statistics);
        }
        return result;
    }

}
