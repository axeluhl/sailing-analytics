package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.gwt.ui.datamining.selection.ComplexStatistic;

public interface StatisticsManager {

    public void addStatistic(ComplexStatistic statistic);

    public ComplexStatistic getStatistic(StatisticType statisticType);
    public Collection<ComplexStatistic> getAllStatistics();

    public Collection<StatisticType> getRegisteredStatisticTypes();

}
