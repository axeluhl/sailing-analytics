package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.gwt.ui.datamining.StatisticsManager;

public class SimpleStatisticsManager implements StatisticsManager {
    
    private HashMap<StatisticType, ComplexStatistic> statisticsMap;

    public SimpleStatisticsManager() {
        statisticsMap = new HashMap<StatisticType, ComplexStatistic>();
    }

    @Override
    public void addStatistic(ComplexStatistic statistic) {
        statisticsMap.put(statistic.getStatisticType(), statistic);
    }

    @Override
    public ComplexStatistic getStatistic(StatisticType statisticType) {
        return statisticsMap.get(statisticType);
    }

    @Override
    public Collection<ComplexStatistic> getAllStatistics() {
        return statisticsMap.values();
    }
    
    @Override
    public Collection<StatisticType> getRegisteredStatisticTypes() {
        return statisticsMap.keySet();
    }
    
    public static StatisticsManager createManagerWithStandardStatistics() {
        StatisticsManager manager = new SimpleStatisticsManager();
        
        manager.addStatistic(new ComplexStatistic(StatisticType.Speed,
                                                  Arrays.asList(AggregatorType.Average),
                                                  Arrays.asList(DataTypes.GPSFix)));
        manager.addStatistic(new ComplexStatistic(StatisticType.Distance,
                                                  Arrays.asList(AggregatorType.Average, AggregatorType.Sum),
                                                  Arrays.asList(DataTypes.TrackedLegOfCompetitor)));
        
        return manager;
    }

}
