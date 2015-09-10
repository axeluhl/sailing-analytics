package com.sap.sse.datamining;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;

public interface StatisticQueryDefinition<DataSourceType, DataType, ExtractedType, ResultType> {

    public Class<DataSourceType> getDataSourceType();
    public Class<DataType> getDataType();
    public Class<ExtractedType> getExtractedType();
    public Class<ResultType> getResultType();
    
    public Locale getLocale();
    
    public DataRetrieverChainDefinition<DataSourceType, DataType> getDataRetrieverChainDefinition();
    public Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> getFilterSelection();
    public List<Function<?>> getDimensionsToGroupBy();
    public Function<ExtractedType> getStatisticToCalculate();
    public AggregationProcessorDefinition<ExtractedType, ResultType> getAggregatorDefinition();

}
