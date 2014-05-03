package com.sap.sailing.datamining.shared.impl;

import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class QueryDefinitionImpl implements QueryDefinition {

    private final String localeInfoName;
    private final Map<FunctionDTO, Iterable<?>> filterSelection;
    private final List<FunctionDTO> dimensionsToGroupBy;
    private final AggregatorType aggregatorType;

    public QueryDefinitionImpl(String localeInfoName, Map<FunctionDTO, Iterable<?>> filterSelection,
            List<FunctionDTO> dimensionsToGroupBy, AggregatorType aggregatorType) {
        this.localeInfoName = localeInfoName;
        this.filterSelection = filterSelection;
        this.dimensionsToGroupBy = dimensionsToGroupBy;
        this.aggregatorType = aggregatorType;
    }

    @Override
    public String getLocaleInfoName() {
        return localeInfoName;
    }

    @Override
    public Map<FunctionDTO, Iterable<?>> getFilterSelection() {
        return filterSelection;
    }

    @Override
    public List<FunctionDTO> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public FunctionDTO getExtractionFunction() {
        return getExtractionFunction();
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

}
