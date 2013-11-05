package com.sap.sailing.gwt.ui.datamining.selection;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;

public class ResultCalculationInformation {
    
    private StatisticType statisticType;
    private AggregatorType aggregatorType;
    private DataTypes dataType;
    
    public ResultCalculationInformation(StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType) {
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;
        this.dataType = dataType;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    public DataTypes getDataType() {
        return dataType;
    }
    
    @Override
    public String toString() {
        return getStatisticType().toString() + " (" + getAggregatorType().toString() + ")";
    }

}
