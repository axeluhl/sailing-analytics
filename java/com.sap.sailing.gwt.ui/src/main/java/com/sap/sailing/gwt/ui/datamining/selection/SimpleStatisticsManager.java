package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.gwt.ui.datamining.StatisticsManager;

public class SimpleStatisticsManager implements StatisticsManager {
    
    private HashMap<DataTypes, ComplexStatistic> statisticsMap;

    public SimpleStatisticsManager() {
        statisticsMap = new HashMap<DataTypes, ComplexStatistic>();
    }

    @Override
    public void addStatistic(ComplexStatistic statistic) {
        statisticsMap.put(statistic.getBaseDataType(), statistic);
    }

    @Override
    public ComplexStatistic getStatistic(DataTypes dataType) {
        return statisticsMap.get(dataType);
    }

    @Override
    public Collection<ComplexStatistic> getAllStatistics() {
        return statisticsMap.values();
    }
    
    @Override
    public Set<DataTypes> getRegisteredBaseDataTypes() {
        return statisticsMap.keySet();
    }
    
    public static StatisticsManager createManagerWithStandardStatistics() {
        StatisticsManager manager = new SimpleStatisticsManager();
        
        manager.addStatistic(new ComplexStatistic(DataTypes.GPSFix,
                                                  Arrays.asList(StatisticType.Speed),
                                                  Arrays.asList(AggregatorType.Average)));
        manager.addStatistic(new ComplexStatistic(DataTypes.TrackedLegOfCompetitor,
                                                  Arrays.asList(StatisticType.Distance),
                                                  Arrays.asList(AggregatorType.Average, AggregatorType.Sum)));
        
        return manager;
    }

}
