package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.StatisticType;

public interface StatisticsProvider {

    public void addStatistic(StatisticType statisticType, AggregatorType aggregatorType);

    public StatisticAndAggregatorType getStatistic(StatisticType statisticType, AggregatorType aggregatorType);
    public Collection<StatisticAndAggregatorType> getAllStatistics();

}
