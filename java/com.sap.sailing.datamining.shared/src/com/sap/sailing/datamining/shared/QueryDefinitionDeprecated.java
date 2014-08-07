package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.components.GrouperType;

public interface QueryDefinitionDeprecated extends Serializable {

    public String getLocaleInfoName();

    public GrouperType getGrouperType();

    public StatisticType getStatisticType();

    public AggregatorType getAggregatorType();

    public DataTypes getDataType();

    public List<DimensionIdentifier> getDimensionsToGroupBy();

    public Map<DimensionIdentifier, Iterable<? extends Serializable>> getSelection();

}