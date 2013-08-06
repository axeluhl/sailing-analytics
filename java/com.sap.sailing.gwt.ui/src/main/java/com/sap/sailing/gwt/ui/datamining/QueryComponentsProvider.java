package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.StatisticType;

public interface QueryComponentsProvider {

    public Map<Dimension, Collection<?>> getSelection();

    public Dimension getDimensionToGroupBy();

    public StatisticType getStatisticToCalculate();

    public AggregatorType getAggregationType();

}
