package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.StatisticType;

public interface QueryComponentsProvider<DimensionType> {

    public Map<DimensionType, Collection<?>> getSelection();

    public DimensionType getDimensionToGroupBy();

    public StatisticType getStatisticToCalculate();

    public AggregatorType getAggregationType();

}
