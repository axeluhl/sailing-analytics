package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NullaryFunctionValuesFilterCriterion;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.i18n.ServerStringMessages;

public class QueryFactory {

    public <DataSourceType, DataType, ResultType> Query<ResultType> createQuery(DataSourceType dataSource, QueryDefinition<DataSourceType, DataType, ResultType> queryDefinition,
                                                                            ServerStringMessages stringMessages, ExecutorService executor) {
        return new ProcessorQuery<ResultType, DataSourceType>(dataSource, stringMessages, queryDefinition.getLocale()) {
            @Override
            protected Processor<DataSourceType, ?> createFirstProcessor() {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Function<ResultType> extractionFunction = queryDefinition.getStatisticToCalculate();
                Class<DataType> dataTypeToRetrieve = queryDefinition.getDataType();
                
                Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>> aggregationProcessor = processorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType(), queryDefinition.getResultType());
                Processor<GroupedDataEntry<DataType>, GroupedDataEntry<ResultType>> extractionProcessor = processorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                Processor<DataType, GroupedDataEntry<DataType>> groupingProcessor = processorFactory.createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, queryDefinition.getDimensionsToGroupBy());

                DataRetrieverChainBuilder<DataSourceType> chainBuilder = queryDefinition.getDataRetrieverChainDefinition().startBuilding(executor);
                Map<Integer, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(queryDefinition.getFilterSelection());
                while (chainBuilder.canStepFurther()) {
                    chainBuilder.stepFurther();
                    
                    if (criteriaMappedByRetrieverLevel.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
                        chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(chainBuilder.getCurrentRetrieverLevel()));
                    }
                }
                chainBuilder.addResultReceiver(groupingProcessor);
                
                return chainBuilder.build();
            }
            
        };
    }
    
    @SuppressWarnings("unchecked")
    private <DataType> Map<Integer, FilterCriterion<?>> createFilterCriteria(Map<Integer, Map<Function<?>, Collection<?>>> filterSelection) {
        Map<Integer, CompoundFilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<Integer, Map<Function<?>, Collection<?>>> levelFilterSelection : filterSelection.entrySet()) {
            for (Entry<Function<?>, Collection<?>> levelFilterSelectionEntry : levelFilterSelection.getValue().entrySet()) {
                Function<?> function = levelFilterSelectionEntry.getKey();
                Class<DataType> dataType = (Class<DataType>) function.getDeclaringType();
                
                if (!criteriaMappedByRetrieverLevel.containsKey(levelFilterSelection.getKey())) {
                    criteriaMappedByRetrieverLevel.put(levelFilterSelection.getKey(), new AndCompoundFilterCriterion<>(dataType));
                }

                Collection<Object> filterValues = new ArrayList<>(levelFilterSelectionEntry.getValue());
                ((CompoundFilterCriterion<DataType>) criteriaMappedByRetrieverLevel.get(levelFilterSelection.getKey())).addCriteria(new NullaryFunctionValuesFilterCriterion<>(dataType, function, filterValues));
            }
        }
        return (Map<Integer, FilterCriterion<?>>)(Map<Integer, ?>) criteriaMappedByRetrieverLevel;
    }

    public <DataSource> Query<Set<Object>> createDimensionValuesQuery(DataSource dataSource,
            DataRetrieverChainDefinition<DataSource, ?> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Locale locale,
            ServerStringMessages stringMessages, ExecutorService executor) {
        return new ProcessorQuery<Set<Object>, DataSource>(dataSource, stringMessages, locale) {
            @Override
            protected Processor<DataSource, ?> createFirstProcessor() {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);

                DataRetrieverChainBuilder<DataSource> chainBuilder = dataRetrieverChainDefinition.startBuilding(executor);
                chainBuilder.stepFurther(); //Initialization
                for (int level = 0; level < retrieverLevel; level++) {
                    chainBuilder.stepFurther();
                }
                for (Processor<?, ?> resultReceiver : processorFactory.createGroupingExtractorsForDimensions(
                        chainBuilder.getCurrentRetrievedDataType(), valueCollector, dimensions, stringMessages, locale)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                return chainBuilder.build();
            }
        };
    }
}
