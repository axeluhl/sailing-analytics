package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public interface QueryDefinition extends Serializable {

    public GrouperType getGrouperType();

    public StatisticType getStatisticType();

    public AggregatorType getAggregatorType();

    public String getCustomGrouperScriptText();

    public List<SharedDimension> getDimensionsToGroupBy();

    public Map<SharedDimension, Iterable<?>> getSelection();

}