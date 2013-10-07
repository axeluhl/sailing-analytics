package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.StatisticType;

public interface QueryComponentsProvider<DimensionType> {
    
    public enum GrouperType { Dimensions, Custom }

    public Iterable<String> validateComponents();

    public Map<DimensionType, Collection<?>> getSelection();

    public GrouperType getGrouperType();
    public Collection<DimensionType> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();

    public StatisticType getStatisticToCalculate();
    public AggregatorType getAggregatorType();

    public void addListener(QueryComponentsChangedListener<DimensionType> listener);
    public void removeListener(QueryComponentsChangedListener<DimensionType> listener);

}
