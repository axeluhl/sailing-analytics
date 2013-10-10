package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class SimpleStatisticsProvider implements StatisticsProvider {
    
    private HashMap<Pair<StatisticType, AggregatorType>, StatisticAndAggregatorType> statisticsMap;

    public SimpleStatisticsProvider() {
        statisticsMap = new HashMap<Pair<StatisticType, AggregatorType>, StatisticAndAggregatorType>();
    }

    @Override
    public void addStatistic(StatisticType statisticType, AggregatorType aggregatorType) {
        Pair<StatisticType, AggregatorType> key = new Pair<StatisticType, AggregatorType>(statisticType, aggregatorType);
        if (!statisticsMap.containsKey(key)) {
            StatisticAndAggregatorType statistic = new StatisticAndAggregatorType(statisticType, aggregatorType);
            statisticsMap.put(key, statistic);
        }
    }

    @Override
    public StatisticAndAggregatorType getStatistic(StatisticType statisticType, AggregatorType aggregatorType) {
        Pair<StatisticType, AggregatorType> key = new Pair<StatisticType, AggregatorType>(statisticType, aggregatorType);
        return statisticsMap.get(key);
    }

    @Override
    public Collection<StatisticAndAggregatorType> getAllStatistics() {
        return statisticsMap.values();
    }

}
