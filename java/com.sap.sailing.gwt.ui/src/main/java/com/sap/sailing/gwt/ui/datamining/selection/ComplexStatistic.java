package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public class ComplexStatistic {
    
    private final StatisticType statisticType;
    private final HashSet<AggregatorType> possibleAggregators;
    private final Collection<DataTypes> possibleDataBases;

    public ComplexStatistic(StatisticType statistic, Collection<AggregatorType> possibleAggregators, Collection<DataTypes> possibleDataBases) {
        this.statisticType = statistic;
        this.possibleAggregators = new HashSet<AggregatorType>(possibleAggregators);
        this.possibleDataBases = new HashSet<DataTypes>(possibleDataBases);
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public HashSet<AggregatorType> getPossibleAggregators() {
        return possibleAggregators;
    }

    public Collection<DataTypes> getPossibleDataTypes() {
        return possibleDataBases;
    }

}
