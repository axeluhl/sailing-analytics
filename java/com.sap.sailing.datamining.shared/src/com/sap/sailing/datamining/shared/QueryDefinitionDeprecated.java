package com.sap.sailing.datamining.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.components.GrouperType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface QueryDefinitionDeprecated extends Serializable {

    public String getLocaleInfoName();

    public FunctionDTO getExtractionFunction();

    public GrouperType getGrouperType();

    public AggregatorType getAggregatorType();

    public List<DimensionIdentifier> getDimensionsToGroupBy();

    public Map<DimensionIdentifier, Iterable<? extends Serializable>> getSelection();

}