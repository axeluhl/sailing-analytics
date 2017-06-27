package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class StatisticsByYearJsonSerializer implements JsonSerializer<Map<Integer, Statistics>> {

    public static final String FIELD_ARRAY = "array";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_STATISTICS = "statistics";

    private final StatisticsJsonSerializer statisticsJsonSerializer;

    public StatisticsByYearJsonSerializer(StatisticsJsonSerializer statisticsJsonSerializer) {
        this.statisticsJsonSerializer = statisticsJsonSerializer;
    }

    @Override
    public JSONObject serialize(Map<Integer, Statistics> object) {
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        result.put(FIELD_ARRAY, array);
        for (Entry<Integer, Statistics> entry : object.entrySet()) {
            JSONObject jsonEntry = new JSONObject();
            jsonEntry.put(FIELD_YEAR, entry.getKey());
            jsonEntry.put(FIELD_STATISTICS, statisticsJsonSerializer.serialize(entry.getValue()));
            array.add(jsonEntry);
        }
        return result;
    }

}
