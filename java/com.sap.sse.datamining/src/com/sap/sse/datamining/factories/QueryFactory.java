package com.sap.sse.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import com.sap.sse.common.settings.SerializableSettings;
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

    public <DataSourceType, DataType, ExtractedType, ResultType> Query<ResultType> createQuery(
            DataSourceType dataSource,
            StatisticQueryDefinition<DataSourceType, DataType, ExtractedType, ResultType> queryDefinition,
            ResourceBundleStringMessages stringMessages, ExecutorService executor) {
        final Locale locale = queryDefinition.getLocale();
        return new ProcessorQuery<ResultType, DataSourceType>(dataSource, stringMessages, locale,
                queryDefinition.getResultType(), new AdditionalStatisticQueryData()) {
            @Override
            protected Processor<DataSourceType, ?> createChainAndReturnFirstProcessor(Processor<Map<GroupKey, ResultType>, Void> resultReceiver) {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                
                Function<ExtractedType> extractionFunction = queryDefinition.getStatisticToCalculate();
                Class<DataType> dataTypeToRetrieve = queryDefinition.getDataType();
                
                Processor<GroupedDataEntry<ExtractedType>, Map<GroupKey, ResultType>> aggregationProcessor = processorFactory
                        .createAggregationProcessor(/* query */ this, queryDefinition.getAggregatorDefinition());
                Processor<GroupedDataEntry<DataType>, GroupedDataEntry<ExtractedType>> extractionProcessor = processorFactory
                        .createExtractionProcessor(aggregationProcessor, extractionFunction, getParameterProviderFor(
                                extractionFunction, stringMessages, locale));
                Processor<DataType, GroupedDataEntry<DataType>> groupingProcessor = processorFactory
                        .createGroupingProcessor(dataTypeToRetrieve, extractionProcessor, getParameterProvidersFor(
                                queryDefinition.getDimensionsToGroupBy(), stringMessages, locale));
                DataRetrieverChainBuilder<DataSourceType> chainBuilder = queryDefinition.getDataRetrieverChainDefinition().startBuilding(executor);
                Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(
                        queryDefinition.getFilterSelection(), stringMessages, locale);
                Map<DataRetrieverLevel<?, ?>, SerializableSettings> settingsMappedByRetrieverLevel = queryDefinition.getRetrieverSettings();
                while (chainBuilder.canStepFurther()) {
                    chainBuilder.stepFurther();
                    
                    DataRetrieverLevel<?, ?> currentLevel = chainBuilder.getCurrentRetrieverLevel();
                    if (settingsMappedByRetrieverLevel.containsKey(currentLevel)) {
                        chainBuilder.setSettings(settingsMappedByRetrieverLevel.get(currentLevel));
                    }
                    if (criteriaMappedByRetrieverLevel.containsKey(currentLevel)) {
                        chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(currentLevel));
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
        return function.needsLocalizationParameters();
    }

    @SuppressWarnings("unchecked")
    private <DataType> Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> createFilterCriteria(
            Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection,
            ResourceBundleStringMessages stringMessages, Locale locale) {
        Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = new HashMap<>();
        for (Entry<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> levelFilterSelection : filterSelection.entrySet()) {
            for (Entry<Function<?>, Collection<?>> levelFilterSelectionEntry : levelFilterSelection.getValue().entrySet()) {
                Function<?> function = levelFilterSelectionEntry.getKey();
                Class<DataType> dataType = (Class<DataType>) function.getDeclaringType();
                
                if (!criteriaMappedByRetrieverLevel.containsKey(levelFilterSelection.getKey())) {
                    criteriaMappedByRetrieverLevel.put(levelFilterSelection.getKey(), new AndCompoundFilterCriterion<>(dataType));
                }

                Collection<Object> filterValues = new ArrayList<>(levelFilterSelectionEntry.getValue());
                ((CompoundFilterCriterion<DataType>) criteriaMappedByRetrieverLevel.get(levelFilterSelection.getKey())).addCriteria(
                        new FunctionValuesFilterCriterion<>(dataType, function, getParameterProviderFor(function, stringMessages, locale), filterValues));
            }
        }
        return criteriaMappedByRetrieverLevel;
    }

    public <DataSourceType> Query<HashSet<Object>> createDimensionValuesQuery(DataSourceType dataSource,
            final DataRetrieverChainDefinition<DataSourceType, ?> dataRetrieverChainDefinition, final DataRetrieverLevel<?, ?> retrieverLevel,
            final Iterable<Function<?>> dimensions, final Map<DataRetrieverLevel<?, ?>, ? extends SerializableSettings> settings,
            final Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection, final Locale locale,
            final ResourceBundleStringMessages stringMessages, final ExecutorService executor) {
        @SuppressWarnings("unchecked")
        Class<HashSet<Object>> resultType = (Class<HashSet<Object>>)(Class<?>) HashSet.class;
        return new ProcessorQuery<HashSet<Object>, DataSourceType>(dataSource, stringMessages, locale, resultType, new AdditionalDimensionValuesQueryData(dimensions)) {
            @Override
            protected Processor<DataSourceType, ?> createChainAndReturnFirstProcessor(Processor<Map<GroupKey, HashSet<Object>>, Void> resultReceiver) {
                ProcessorFactory processorFactory = new ProcessorFactory(executor);
                Processor<GroupedDataEntry<Object>, Map<GroupKey, HashSet<Object>>> valueCollector = processorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);
                Map<DataRetrieverLevel<?, ?>, FilterCriterion<?>> criteriaMappedByRetrieverLevel = createFilterCriteria(filterSelection, stringMessages, locale);
                DataRetrieverChainBuilder<DataSourceType> chainBuilder = dataRetrieverChainDefinition.startBuilding(executor);
                while (!chainBuilder.hasBeenInitialized() || chainBuilder.getCurrentRetrieverLevel().getLevel() < retrieverLevel.getLevel()) {
                    chainBuilder.stepFurther();
                    DataRetrieverLevel<?, ?> currentLevel = chainBuilder.getCurrentRetrieverLevel();
                    if (settings.containsKey(currentLevel)) {
                        chainBuilder.setSettings(settings.get(currentLevel));
                    }
                    if (criteriaMappedByRetrieverLevel.containsKey(currentLevel)) {
                        chainBuilder.setFilter(criteriaMappedByRetrieverLevel.get(currentLevel));
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
