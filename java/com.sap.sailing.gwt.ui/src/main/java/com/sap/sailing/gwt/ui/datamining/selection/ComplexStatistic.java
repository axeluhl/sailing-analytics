package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public class ComplexStatistic {

    private final DataTypes baseDataType;
    private final Set<StatisticType> possibleStatistics;
    private final Set<AggregatorType> possibleAggregators;

    public ComplexStatistic(DataTypes baseDataType, Collection<StatisticType> possibleStatistics, Collection<AggregatorType> possibleAggregators) {
        this.baseDataType = baseDataType;
        this.possibleAggregators = new HashSet<AggregatorType>(possibleAggregators);
        this.possibleStatistics = new HashSet<StatisticType>(possibleStatistics);
    }

    public DataTypes getBaseDataType() {
        return baseDataType;
    }

    public Set<StatisticType> getPossibleStatistics() {
        return possibleStatistics;
    }

    public Set<AggregatorType> getPossibleAggregators() {
        return possibleAggregators;
    }

}
