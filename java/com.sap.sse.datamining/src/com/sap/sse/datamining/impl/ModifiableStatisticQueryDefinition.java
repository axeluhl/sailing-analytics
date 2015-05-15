package com.sap.sse.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.components.AggregatorType;

public class ModifiableStatisticQueryDefinition<DataSourceType, DataType, ResultType> implements StatisticQueryDefinition<DataSourceType, DataType, ResultType> {

    private final Locale locale;
    
    private final DataRetrieverChainDefinition<DataSourceType, DataType> retrieverChain;
    private final Map<Integer, Map<Function<?>, Collection<?>>> filterSelection;
    
    private final List<Function<?>> dimensionsToGroupBy;
    
    private final Function<ResultType> statisticToCalculate;
    private final AggregatorType aggregatorType;

    public ModifiableStatisticQueryDefinition(Locale locale, DataRetrieverChainDefinition<DataSourceType, DataType> retrieverChain, Function<ResultType> statisticToCalculate, AggregatorType aggregatorType) {
        this.locale = locale;
        
        this.retrieverChain = retrieverChain;
        filterSelection = new HashMap<>();
        
        dimensionsToGroupBy = new ArrayList<>();
        
        this.statisticToCalculate = statisticToCalculate;
        this.aggregatorType = aggregatorType;
    }
    
    @Override
    public Class<DataSourceType> getDataSourceType() {
        return retrieverChain.getDataSourceType();
    }
    
    @Override
    public Class<DataType> getDataType() {
        return retrieverChain.getRetrievedDataType();
    }
    
    @Override
    public Class<ResultType> getResultType() {
        return statisticToCalculate.getReturnType();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinition() {
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
