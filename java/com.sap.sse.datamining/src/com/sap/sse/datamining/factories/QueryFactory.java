package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryDefinition;
import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.SimpleAdditionalQueryData;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.FunctionValuesFilterCriterion;
import com.sap.sse.datamining.impl.functions.LocalizationParameterProvider;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class QueryFactory {

    public <DataSourceType, DataType, ResultType> Query<ResultType> createQuery(DataSourceType dataSource, QueryDefinition<DataSourceType, DataType, ResultType> queryDefinition,
                                                                            ResourceBundleStringMessages stringMessages, ExecutorService executor) {
        return new ProcessorQuery<ResultType, DataSourceType>(dataSource, stringMessages, queryDefinition.getLocale(), new SimpleAdditionalQueryData(QueryType.STATISTIC, queryDefinition.getDataRetrieverChainDefinition().getID())) {
            @Override
            protected Processor<DataSourceType, ?> createFirstProcessor() {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Function<ResultType> extractionFunction = queryDefinition.getStatisticToCalculate();
                Class<DataType> dataTypeToRetrieve = queryDefinition.getDataType();
                
                Processor<GroupedDataEntry<ResultType>, Map<GroupKey, ResultType>> aggregationProcessor = processorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType(), queryDefinition.getResultType());
                Processor<GroupedDataEntry<DataType>, GroupedDataEntry<ResultType>> extractionProcessor = processorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction, getParameterProviderFor(extractionFunction, stringMessages, queryDefinition.getLocale()));
                
                Processor<DataType, GroupedDataEntry<DataType>> groupingProcessor = processorFactory.createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, getParameterProvidersFor(queryDefinition.getDimensionsToGroupBy(), stringMessages, queryDefinition.getLocale()));

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
    
    private Iterable<Pair<Function<?>, ParameterProvider>> getParameterProvidersFor(Iterable<Function<?>> functions, ResourceBundleStringMessages stringMessages, Locale locale) {
        Collection<Pair<Function<?>, ParameterProvider>> functionsWithParameterProvider = new ArrayList<>();
        for (Function<?> function : functions) {
            functionsWithParameterProvider.add(new Pair<>(function, getParameterProviderFor(function, stringMessages, locale)));
        }
        return functionsWithParameterProvider;
    }

    private ParameterProvider getParameterProviderFor(Function<?> function, ResourceBundleStringMessages stringMessages, Locale locale) {
        if (functionNeedsLocalizationParameters(function)) {
            return new LocalizationParameterProvider(locale, stringMessages);
        }
        return ParameterProvider.NULL;
    }
    
    private boolean functionNeedsLocalizationParameters(Function<?> function) {
        Iterator<Class<?>> parameterTypesIterator = function.getParameters().iterator();
        int parameterCount = 0;
        while (parameterTypesIterator.hasNext()) {
            parameterCount++;
            parameterTypesIterator.next();
        }
        if (parameterCount != 2) {
            return false;
        }
        
        parameterTypesIterator = function.getParameters().iterator();
        Class<?> firstParameter = parameterTypesIterator.next();
        Class<?> secondParameter = parameterTypesIterator.next();
        return firstParameter.isAssignableFrom(Locale.class) && secondParameter.isAssignableFrom(ResourceBundleStringMessages.class);
    }

    @SuppressWarnings("unchecked")
    private <DataType> Map<Integer, FilterCriterion<?>> createFilterCriteria(Map<Integer, Map<Function<?>, Collection<?>>> filterSelection) {
        Map<Integer, CompoundFilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<Integer, Map<Function<?>, Collection<?>>> levelFilterSelection : filterSelection.entrySet()) {
            for (Entry<Function<?>, Collection<?>> levelFilterSelectionEntry : levelFilterSelection.getValue().entrySet()) {
                Function<?> function = levelFilterSelectionEntry.getKey();
                ParameterProvider parameterProvider = ParameterProvider.NULL;
                Class<DataType> dataType = (Class<DataType>) function.getDeclaringType();
                
                if (!criteriaMappedByRetrieverLevel.containsKey(levelFilterSelection.getKey())) {
                    criteriaMappedByRetrieverLevel.put(levelFilterSelection.getKey(), new AndCompoundFilterCriterion<>(dataType));
                }

                Collection<Object> filterValues = new ArrayList<>(levelFilterSelectionEntry.getValue());
                ((CompoundFilterCriterion<DataType>) criteriaMappedByRetrieverLevel.get(levelFilterSelection.getKey())).addCriteria(new FunctionValuesFilterCriterion<>(dataType, function, parameterProvider, filterValues));
            }
        }
        return (Map<Integer, FilterCriterion<?>>)(Map<Integer, ?>) criteriaMappedByRetrieverLevel;
    }

    public <DataSource> Query<Set<Object>> createDimensionValuesQuery(DataSource dataSource,
            DataRetrieverChainDefinition<DataSource, ?> dataRetrieverChainDefinition, int retrieverLevel,
            Iterable<Function<?>> dimensions, Locale locale,
            ResourceBundleStringMessages stringMessages, ExecutorService executor) {
        return new ProcessorQuery<Set<Object>, DataSource>(dataSource, stringMessages, locale, new SimpleAdditionalQueryData(QueryType.DIMENSION_VALUES, dataRetrieverChainDefinition.getID())) {
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
                        chainBuilder.getCurrentRetrievedDataType(), valueCollector, getParameterProvidersFor(dimensions, stringMessages, locale), stringMessages, locale)) {
                    chainBuilder.addResultReceiver(resultReceiver);
                }
                
                return chainBuilder.build();
            }
        };
    }
}
