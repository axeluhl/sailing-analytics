package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainBuilder;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.ParameterProvider;
import com.sap.sse.datamining.functions.ParameterizedFunction;
import com.sap.sse.datamining.impl.AdditionalDimensionValuesQueryData;
import com.sap.sse.datamining.impl.AdditionalStatisticQueryData;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.FunctionValuesFilterCriterion;
import com.sap.sse.datamining.impl.functions.LocalizationParameterProvider;
import com.sap.sse.datamining.impl.functions.SimpleParameterizedFunction;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class QueryFactory {

    public <DataSourceType, DataType, ExtractedType, ResultType> Query<ResultType> createQuery(DataSourceType dataSource, StatisticQueryDefinition<DataSourceType, DataType, ExtractedType, ResultType> queryDefinition,
                                                                            ResourceBundleStringMessages stringMessages, ExecutorService executor) {
        return new ProcessorQuery<ResultType, DataSourceType>(dataSource, stringMessages, queryDefinition.getLocale(), queryDefinition.getResultType(), new AdditionalStatisticQueryData(queryDefinition.getDataRetrieverChainDefinition().getID())) {
            @Override
            protected Processor<DataSourceType, ?> createChainAndReturnFirstProcessor(Processor<Map<GroupKey, ResultType>, Void> resultReceiver) {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Function<ExtractedType> extractionFunction = queryDefinition.getStatisticToCalculate();
                Class<DataType> dataTypeToRetrieve = queryDefinition.getDataType();
                
                Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, ResultType>> aggregationProcessor = processorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorDefinition());
                Processor<GroupedDataEntry<DataType>, GroupedDataEntry<ExtractedType>> extractionProcessor = processorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction, getParameterProviderFor(extractionFunction, stringMessages, queryDefinition.getLocale()));
                
                Processor<DataType, GroupedDataEntry<DataType>> groupingProcessor = processorFactory.createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, getParameterProvidersFor(queryDefinition.getDimensionsToGroupBy(), stringMessages, queryDefinition.getLocale()));

                DataRetrieverChainBuilder<DataSourceType> chainBuilder = queryDefinition.getDataRetrieverChainDefinition().startBuilding(executor);
                Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(queryDefinition.getFilterSelection());
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
    
    private Iterable<ParameterizedFunction<?>> getParameterProvidersFor(Iterable<Function<?>> functions, ResourceBundleStringMessages stringMessages, Locale locale) {
        Collection<ParameterizedFunction<?>> functionsWithParameterProvider = new ArrayList<>();
        for (Function<?> function : functions) {
            functionsWithParameterProvider.add(new SimpleParameterizedFunction<>(function, getParameterProviderFor(function, stringMessages, locale)));
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
    private <DataType> Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> createFilterCriteria(Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection) {
        Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> levelFilterSelection : filterSelection.entrySet()) {
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
        return criteriaMappedByRetrieverLevel;
    }

    public <DataSourceType> Query<HashSet<Object>> createDimensionValuesQuery(DataSourceType dataSource,
            final DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, final DataRetrieverLevel<?, ?> retrieverLevel,
            final Iterable<Function<?>> dimensions, final Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection, final Locale locale,
            final ResourceBundleStringMessages stringMessages, final ExecutorService executor) {
        @SuppressWarnings("unchecked")
        Class<HashSet<Object>> resultType = (Class<HashSet<Object>>)(Class<?>) HashSet.class;
        return new ProcessorQuery<HashSet<Object>, DataSourceType>(dataSource, stringMessages, locale, resultType, new AdditionalDimensionValuesQueryData(dataRetrieverChainDefinition.getID(), dimensions)) {
            @Override
            protected Processor<DataSourceType, ?> createChainAndReturnFirstProcessor(Processor<Map<GroupKey, HashSet<Object>>, Void> resultReceiver) {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Processor<GroupedDataEntry<Object>, Map<GroupKey, HashSet<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);

                Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(filterSelection);
                DataRetrieverChainBuilder<DataSourceType> chainBuilder = dataRetrieverChainDefinition.startBuilding(executor);
                while (!chainBuilder.hasBeenInitialized() || chainBuilder.getCurrentRetrieverLevel().getLevel() < retrieverLevel.getLevel()) {
                    chainBuilder.stepFurther();
                    
                    if (criteriaMappedByRetrieverLevel.containsKey(chainBuilder.getCurrentRetrieverLevel())) {
                        chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(chainBuilder.getCurrentRetrieverLevel()));
                    }
                }
                for (Processor<?, ?> groupingExtractor : processorFactory.createGroupingExtractorsForDimensions(
                        chainBuilder.getCurrentRetrievedDataType(), valueCollector, getParameterProvidersFor(dimensions, stringMessages, locale), stringMessages, locale)) {
                    chainBuilder.addResultReceiver(groupingExtractor);
                }
                
                return chainBuilder.build();
            }
        };
    }
}
