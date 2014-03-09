package com.sap.sse.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;

public abstract class AbstractFilteringRetrievalProcessor<InputType, WorkingType, ResultType> 
             extends AbstractPartitioningParallelProcessor<InputType, WorkingType, ResultType> {

    private final FilterCriteria<WorkingType> criteria;
    
    private int retrievedDataAmount;
    private int filteredDataAmount;

    public AbstractFilteringRetrievalProcessor(ExecutorService executor, Collection<Processor<ResultType>> resultReceivers, FilterCriteria<WorkingType> criteria) {
        super(executor, resultReceivers);
        this.criteria = criteria;
    }

    @Override
    protected Iterable<WorkingType> partitionElement(InputType element) {
        Collection<WorkingType> filteredData = new ArrayList<>();
        
        Iterable<WorkingType> retrievedData = retrieveData(element);
        for (WorkingType retrievedDataEntry : retrievedData) {
            retrievedDataAmount++;
            if (criteria.matches(retrievedDataEntry)) {
                filteredData.add(retrievedDataEntry);
            }
        }
        filteredDataAmount = filteredData.size();
        return filteredData;
    }

    protected abstract Iterable<WorkingType> retrieveData(InputType element);
    
    //Redefinition of the method to set the parameter name to element instead of partial element.
    //This makes the implementation of sub classes more fluent.
    @Override
    protected abstract Callable<ResultType> createInstruction(WorkingType filteredPartialElement);
    
    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        additionalDataBuilder.setRetrievedDataAmount(retrievedDataAmount);
        additionalDataBuilder.setFilteredDataAmount(filteredDataAmount);
    }

}
