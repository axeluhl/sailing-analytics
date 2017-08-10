package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class StatisticsByYearJsonSerializer implements JsonSerializer<Pair<Integer, Statistics>> {

    public static final String FIELD_YEAR = "year";
    public static final String FIELD_STATISTICS = "statistics";

    private final StatisticsJsonSerializer statisticsJsonSerializer;

    public StatisticsByYearJsonSerializer(StatisticsJsonSerializer statisticsJsonSerializer) {
        this.statisticsJsonSerializer = statisticsJsonSerializer;
    }

    @Override
    public JSONObject serialize(Pair<Integer, Statistics> object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_YEAR, object.getA());
        result.put(FIELD_STATISTICS, statisticsJsonSerializer.serialize(object.getB()));
        return result;
    }

}
