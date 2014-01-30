package com.sap.sailing.gwt.ui.datamining.selection;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;

public class SimpleStatistic {

    private DataTypes dataType;
    private StatisticType statisticType;
    private AggregatorType aggregatorType;

    public SimpleStatistic(DataTypes dataType, StatisticType statisticType, AggregatorType aggregatorType) {
        this.dataType = dataType;
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;
    }

    public DataTypes getDataType() {
        return dataType;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

}
