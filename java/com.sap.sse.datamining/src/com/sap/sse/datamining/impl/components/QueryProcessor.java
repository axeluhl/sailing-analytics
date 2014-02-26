package com.sap.sse.datamining.impl.components;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

public class QueryProcessor<DataSourceType, AggregatedType> implements Query<AggregatedType>, Processor<Map<GroupKey, AggregatedType>> {
    
    private final DataSourceType dataSource;
    private Processor<DataSourceType> firstProcessor;
    
    private QueryResult<AggregatedType> result;

    public QueryProcessor(DataSourceType dataSource) {
        this.dataSource = dataSource;
    }
    
    public void setFirstProcessor(Processor<DataSourceType> firstProcessor) {
        this.firstProcessor = firstProcessor;
    }

    @Override
    public QueryResult<AggregatedType> run() throws InterruptedException, ExecutionException {
        firstProcessor.onElement(dataSource);
        wait();
        return result;
    }

    @Override
    public void onElement(Map<GroupKey, AggregatedType> element) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finish() throws InterruptedException {
        // TODO Auto-generated method stub
        
    }

}
