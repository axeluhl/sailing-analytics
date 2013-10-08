package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.Components.GrouperType;

public interface QueryDefinition<DimensionType> extends Serializable {

    public GrouperType getGrouperType();

    public StatisticType getStatisticType();

    public AggregatorType getAggregatorType();

    public String getCustomGrouperScriptText();

    public List<DimensionType> getDimensionsToGroupBy();

    public Map<DimensionType, Iterable<?>> getSelection();

}