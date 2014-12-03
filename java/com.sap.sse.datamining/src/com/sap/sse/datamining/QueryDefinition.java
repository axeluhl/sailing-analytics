package com.sap.sse.datamining;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.shared.components.AggregatorType;

public interface QueryDefinition<DataSourceType, ResultType> {
    
    public Locale getLocale();
    
    public DataRetrieverChainDefinition<DataSourceType> getDataRetrieverChainDefinition();
    public Map<Integer, Map<Function<?>, Collection<?>>> getFilterSelection();
    
    public List<Function<?>> getDimensionsToGroupBy();
    
    public Function<ResultType> getStatisticToCalculate();
    public AggregatorType getAggregatorType();

}
