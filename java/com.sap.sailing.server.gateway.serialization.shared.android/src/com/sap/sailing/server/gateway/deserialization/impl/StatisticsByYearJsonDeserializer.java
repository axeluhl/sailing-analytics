package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.StatisticsByYearJsonSerializer;
import com.sap.sse.common.Util.Pair;

public class StatisticsByYearJsonDeserializer implements JsonDeserializer<Pair<Integer, Statistics>> {

    private final StatisticsJsonDeserializer statisticsJsonDeserializer;

    public StatisticsByYearJsonDeserializer(StatisticsJsonDeserializer statisticsJsonDeserializer) {
        this.statisticsJsonDeserializer = statisticsJsonDeserializer;
    }

    @Override
    public Pair<Integer, Statistics> deserialize(JSONObject object) throws JsonDeserializationException {
        Integer year = ((Number) object.get(StatisticsByYearJsonSerializer.FIELD_YEAR)).intValue();
        Statistics statistics = statisticsJsonDeserializer
                .deserialize((JSONObject) object.get(StatisticsByYearJsonSerializer.FIELD_STATISTICS));
        return new Pair<Integer, Statistics>(year, statistics);
    }

}
