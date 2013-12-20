package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.datamining.StatisticsManager;

public class SimpleStatisticsManager implements StatisticsManager {
    
    private HashMap<Triple<StatisticType, AggregatorType, DataTypes>, ResultCalculationInformation> statisticsMap;

    public SimpleStatisticsManager() {
        statisticsMap = new HashMap<Triple<StatisticType, AggregatorType, DataTypes>, ResultCalculationInformation>();
    }

    @Override
    public void addStatistic(StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType) {
        Triple<StatisticType, AggregatorType, DataTypes> key = new Triple<StatisticType, AggregatorType, DataTypes>(statisticType, aggregatorType, dataType);
        if (!statisticsMap.containsKey(key)) {
            ResultCalculationInformation statistic = new ResultCalculationInformation(statisticType, aggregatorType, dataType);
            statisticsMap.put(key, statistic);
        }
    }

    @Override
    public ResultCalculationInformation getStatistic(StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType) {
        Triple<StatisticType, AggregatorType, DataTypes> key = new Triple<StatisticType, AggregatorType, DataTypes>(statisticType, aggregatorType, dataType);
        return statisticsMap.get(key);
    }

    @Override
    public Collection<ResultCalculationInformation> getAllStatistics() {
        return statisticsMap.values();
    }

}
