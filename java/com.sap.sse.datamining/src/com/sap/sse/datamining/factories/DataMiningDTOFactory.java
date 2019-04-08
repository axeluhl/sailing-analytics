package com.sap.sse.datamining.factories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.StatisticQueryDefinition;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.components.DataRetrieverLevel;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;

public class DataMiningDTOFactory {

    public StatisticQueryDefinitionDTO createQueryDefinitionDTO(StatisticQueryDefinition<?, ?, ?, ?> queryDefinition,
            ResourceBundleStringMessages stringMessages, Locale locale, String localeInfoName) {
        FunctionDTO statisticToCalculate = createFunctionDTO(queryDefinition.getStatisticToCalculate(), stringMessages,
                locale);
        AggregationProcessorDefinitionDTO aggregatorDefinition = createAggregationProcessorDefinitionDTO(
                queryDefinition.getAggregatorDefinition(), stringMessages, locale);
        DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition = createDataRetrieverChainDefinitionDTO(
                queryDefinition.getDataRetrieverChainDefinition(), stringMessages, locale);
        ModifiableStatisticQueryDefinitionDTO queryDefinitionDTO = new ModifiableStatisticQueryDefinitionDTO(
                localeInfoName, statisticToCalculate, aggregatorDefinition, dataRetrieverChainDefinition);

        Map<DataRetrieverLevel<?, ?>, SerializableSettings> retrieverSettings = queryDefinition.getRetrieverSettings();
        Map<DataRetrieverLevel<?, ?>, Map<Function<?>, Collection<?>>> filterSelection = queryDefinition
                .getFilterSelection();
        DataRetrieverChainDefinition<?, ?> retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        List<? extends DataRetrieverLevel<?, ?>> retrieverLevels = retrieverChain.getDataRetrieverLevels();
        for (int level = 0; level < retrieverLevels.size(); level++) {
            DataRetrieverLevel<?, ?> retrieverLevel = retrieverLevels.get(level);
            DataRetrieverLevelDTO retrieverLevelDTO = dataRetrieverChainDefinition.getRetrieverLevel(level);

            SerializableSettings levelSettings = retrieverSettings.get(retrieverLevel);
            if (levelSettings != null) {
                queryDefinitionDTO.setRetrieverSettings(retrieverLevelDTO, levelSettings);
            }
            
            HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = new HashMap<>();
            if (filterSelection.containsKey(retrieverLevel)) {
                for (Entry<Function<?>, Collection<?>> dimensionFilterSelection : filterSelection.get(retrieverLevel)
                        .entrySet()) {
                    @SuppressWarnings("unchecked")
                    HashSet<Serializable> filterValues = new HashSet<>(
                            (Collection<Serializable>) dimensionFilterSelection.getValue());
                    levelFilterSelection.put(
                            createFunctionDTO(dimensionFilterSelection.getKey(), stringMessages, locale), filterValues);
                }
            }
            queryDefinitionDTO.setFilterSelectionFor(retrieverLevelDTO, levelFilterSelection);
        }

        for (Function<?> dimensionToGroupBy : queryDefinition.getDimensionsToGroupBy()) {
            queryDefinitionDTO.appendDimensionToGroupBy(createFunctionDTO(dimensionToGroupBy, stringMessages, locale));
        }

        return queryDefinitionDTO;
    }

    /**
     * Creates the corresponding DTO for the given {@link Function}, without localization.<br>
     * The display name of the resulting DTO is the {@link Function#getSimpleName() simple name} of the given function.
     */
    public FunctionDTO createFunctionDTO(Function<?> function) {
        return createFunctionDTO(function, ()->function.getSimpleName());
    }
    
    /**
     * Creates the corresponding DTO for the given function, with the retrieved string message for the given locale and the
     * contained message key as display name. The message key is provided with the {@link Dimension} or {@link Connector}
     * annotation.<br>
     * If the function has no message key, the function name is used as display name.
     */
    public FunctionDTO createFunctionDTO(Function<?> function, ResourceBundleStringMessages stringMessages, Locale locale) {
        return createFunctionDTO(function, ()->function.getLocalizedName(locale, stringMessages));
    }
    
    private FunctionDTO createFunctionDTO(Function<?> function, FunctionDTO.DisplayNameProvider displayNameProvider) {
        String functionName = function.getSimpleName();
        String sourceTypeName = function.getDeclaringType().getName();
        String returnTypeName = function.getReturnType().getName();
        List<String> parameterTypeNames = getParameterTypeNames(function);
        return new FunctionDTO(function.isDimension(), functionName, sourceTypeName, returnTypeName, parameterTypeNames,
                displayNameProvider, function.getOrdinal());
    }

    private List<String> getParameterTypeNames(Function<?> function) {
        List<String> parameterTypeNames = new ArrayList<>();
        for (Class<?> parameterType : function.getParameters()) {
            parameterTypeNames.add(parameterType.getName());
        }
        return parameterTypeNames;
    }

    /**
     * Creates the corresponding DTO for the given {@link AggregationProcessorDefinition aggregator definition}, without localization.<br>
     * The display name of the resulting DTO is the message key of the given aggregation definition.
     */
    public AggregationProcessorDefinitionDTO createAggregationProcessorDefinitionDTO(AggregationProcessorDefinition<?, ?> aggregatorDefinition) {
        return createAggregationProcessorDefinitionDTO(aggregatorDefinition, ResourceBundleStringMessages.NULL, null);
    }


    /**
     * Creates the corresponding localized DTO for the given {@link AggregationProcessorDefinition aggregator definition}.
     */
    public AggregationProcessorDefinitionDTO createAggregationProcessorDefinitionDTO(AggregationProcessorDefinition<?, ?> aggregatorDefinition,
                                                                                     ResourceBundleStringMessages stringMessages, Locale locale) {
        return new AggregationProcessorDefinitionDTO(aggregatorDefinition.getAggregationNameMessageKey(),
                                                     aggregatorDefinition.getExtractedType().getName(),
                                                     aggregatorDefinition.getAggregatedType().getName(),
                                                     stringMessages.get(locale, aggregatorDefinition.getAggregationNameMessageKey()));
    }

    public DataRetrieverChainDefinitionDTO createDataRetrieverChainDefinitionDTO(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition) {
        return createDataRetrieverChainDefinitionDTO(dataRetrieverChainDefinition, ResourceBundleStringMessages.NULL, null);
    }

    public DataRetrieverChainDefinitionDTO createDataRetrieverChainDefinitionDTO(DataRetrieverChainDefinition<?, ?> dataRetrieverChainDefinition,
                                                                                 ResourceBundleStringMessages stringMessages, Locale locale) {
        ArrayList<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>();
        for (DataRetrieverLevel<?, ?> retrieverLevel : dataRetrieverChainDefinition.getDataRetrieverLevels()) {
            retrieverLevels.add(createDataRetrieverLevelDTO(retrieverLevel, stringMessages, locale));
        }
        return new DataRetrieverChainDefinitionDTO(dataRetrieverChainDefinition.getLocalizedName(locale, stringMessages),
                                                   dataRetrieverChainDefinition.getDataSourceType().getName(), retrieverLevels);
   }

    public DataRetrieverLevelDTO createDataRetrieverLevelDTO(DataRetrieverLevel<?, ?> retrieverLevel,
                                                             ResourceBundleStringMessages stringMessages, Locale locale) {
        String typeName = retrieverLevel.getRetrievedDataType().getName();
        String displayName = retrieverLevel.getRetrievedDataTypeMessageKey() != null
                && !retrieverLevel.getRetrievedDataTypeMessageKey().isEmpty() ? stringMessages
                .get(locale, retrieverLevel.getRetrievedDataTypeMessageKey()) : typeName;
        LocalizedTypeDTO localizedRetrievedDataType = new LocalizedTypeDTO(typeName, displayName);
        return new DataRetrieverLevelDTO(retrieverLevel.getLevel(),
                                         retrieverLevel.getRetrieverType().getName(),
                                         localizedRetrievedDataType, retrieverLevel.getDefaultSettings());
    }
    
    public <ResultType extends Serializable> QueryResultDTO<ResultType> createResultDTO(
            QueryResult<ResultType> result) {
        return new QueryResultDTO<ResultType>(result.getState(), result.getResultType(), result.getResults(), result.getAdditionalData());
    }

}
