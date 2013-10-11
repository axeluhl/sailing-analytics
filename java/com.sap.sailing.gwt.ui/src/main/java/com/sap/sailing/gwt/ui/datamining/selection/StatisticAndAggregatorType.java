package com.sap.sailing.gwt.ui.datamining.selection;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public class StatisticAndAggregatorType {
    
    private StatisticType statisticType;
    private AggregatorType aggregatorType;
    
    public StatisticAndAggregatorType(StatisticType statisticType, AggregatorType aggregatorType) {
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }
    
    @Override
    public String toString() {
        return getStatisticType().toString() + " (" + getAggregatorType().toString() + ")";
    }

}
