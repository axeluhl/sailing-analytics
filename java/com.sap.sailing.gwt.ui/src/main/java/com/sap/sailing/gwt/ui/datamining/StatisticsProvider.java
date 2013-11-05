package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.gwt.ui.datamining.selection.ResultCalculationInformation;

public interface StatisticsProvider {

    public void addStatistic(StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType);

    public ResultCalculationInformation getStatistic(StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType);
    public Collection<ResultCalculationInformation> getAllStatistics();

}
