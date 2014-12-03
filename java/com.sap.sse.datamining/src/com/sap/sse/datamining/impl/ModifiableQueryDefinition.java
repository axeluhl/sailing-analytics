package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.components.AggregatorType;

public class ModifiableQueryDefinition<DataSourceType, ResultType> implements QueryDefinition<DataSourceType, ResultType> {

    private final Locale locale;
    
    private final DataRetrieverChainDefinition<DataSourceType> retrieverChain;
    private final Map<Integer, Map<Function<?>, Collection<?>>> filterSelection;
    
    private final List<Function<?>> dimensionsToGroupBy;
    
    private final Function<ResultType> statisticToCalculate;
    private final AggregatorType aggregatorType;

    public ModifiableQueryDefinition(Locale locale, DataRetrieverChainDefinition<DataSourceType> retrieverChain, Function<ResultType> statisticToCalculate, AggregatorType aggregatorType) {
        this.locale = locale;
        
        this.retrieverChain = retrieverChain;
        filterSelection = new HashMap<>();
        
        dimensionsToGroupBy = new ArrayList<>();
        
        this.statisticToCalculate = statisticToCalculate;
        this.aggregatorType = aggregatorType;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition() {
        return retrieverChain;
    }

    @Override
    public Map<Integer, Map<Function<?>, Collection<?>>> getFilterSelection() {
        return filterSelection;
    }

    @Override
    public List<Function<?>> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public Function<ResultType> getStatisticToCalculate() {
        return statisticToCalculate;
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    public void setFilterSelection(Integer retrieverLevel, Function<?> dimensionToFilterBy, Collection<?> filterSelection) {
        if (!this.filterSelection.containsKey(retrieverLevel)) {
            this.filterSelection.put(retrieverLevel, new HashMap<Function<?>, Collection<?>>());
        }
        
        this.filterSelection.get(retrieverLevel).put(dimensionToFilterBy, filterSelection);
    }

    public void addDimensionToGroupBy(Function<?> dimensionToGroupBy) {
        dimensionsToGroupBy.add(dimensionToGroupBy);
    }

}
