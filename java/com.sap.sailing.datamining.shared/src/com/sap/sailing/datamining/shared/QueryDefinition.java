package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public interface QueryDefinition extends Serializable {

    public String getLocaleName();

    public GrouperType getGrouperType();

    public StatisticType getStatisticType();

    public AggregatorType getAggregatorType();

    public DataTypes getDataType();

    public List<DimensionIdentifier> getDimensionsToGroupBy();

    public Map<DimensionIdentifier, Iterable<?>> getSelection();

}