package com.sap.sse.datamining.shared.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class QueryDefinitionImpl implements QueryDefinition {

    private final String localeInfoName;
    private final Map<FunctionDTO, Iterable<?>> filterSelection;
    private final List<FunctionDTO> dimensionsToGroupBy;
    private final FunctionDTO extractionFunction;
    private final AggregatorType aggregatorType;

    public QueryDefinitionImpl(String localeInfoName, FunctionDTO extractionFunction, AggregatorType aggregatorType) {
        this.localeInfoName = localeInfoName;
        this.filterSelection = new HashMap<FunctionDTO, Iterable<?>>();
        this.dimensionsToGroupBy = new ArrayList<FunctionDTO>();
        this.extractionFunction = extractionFunction;
        this.aggregatorType = aggregatorType;
    }
    
    public void setFilterSelectionFor(FunctionDTO dimension, Iterable<?> selection) {
        filterSelection.put(dimension, selection);
    }
    
    public void appendDimensionToGroupBy(FunctionDTO dimensionToGroupBy) {
        dimensionsToGroupBy.add(dimensionToGroupBy);
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
        return extractionFunction;
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

}
